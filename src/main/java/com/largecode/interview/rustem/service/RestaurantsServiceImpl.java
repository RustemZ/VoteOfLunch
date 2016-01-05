package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.repository.RestaurantsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
@Service
public class RestaurantsServiceImpl implements  RestaurantsService{

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);
    private final RestaurantsRepository restaurantsRepository;

    @Autowired
    public RestaurantsServiceImpl(RestaurantsRepository restaurantsRepository) {
        this.restaurantsRepository = restaurantsRepository;
    }

    @Override
    public Optional<Restaurant> getRestaurantById(long id) {
        LOGGER.debug("Getting restaurant={}", id);
        return Optional.ofNullable(restaurantsRepository.findOne(id));
    }

    @Override
    public Optional<Restaurant> getRestaurantByIdByAuthorities(String idByAuthorities) {
        LOGGER.debug("Getting restaurant by idByAuthorities ={}", idByAuthorities);
        return restaurantsRepository.findOneByIdByAuthorities(idByAuthorities) ;

    }

    @Override
    public Page<Restaurant> getAllRestaurants(Integer page, Integer size) {
        LOGGER.debug("Get all restaurants in page {} and size {}", page, size);
        //userRepository.findAllByOrderByEmailAsc();
        Page pageOfRestaurants = restaurantsRepository.findAllByOrderByTitleAsc( new PageRequest(page, size) );
        return pageOfRestaurants;
    }

    @Override
    public Restaurant createRestaurant(Restaurant restaurantNew) {
        LOGGER.debug("Create new restaurant ={}", restaurantNew );
//        Restaurant restaurantInDb = new Restaurant(userDto.getEmail(),
//                userDto.getPassword(),
//                userDto.getRole() );
        //restaurantInDb.setPasswordHash(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        Restaurant restaurantInDb = restaurantsRepository.saveAndFlush(restaurantNew);
        return restaurantInDb;
    }

    @Override
    public void updateRestaurant(Long id, Restaurant restaurantNew) {
        LOGGER.debug("Update restaurant id = {} with data {}", id, restaurantNew);
        Optional<Restaurant> restaurantInDb  = Optional.ofNullable(restaurantsRepository.findOne(id)) ;
        restaurantInDb.ifPresent( (restaurant) ->  {
            restaurant.setAddress( restaurantNew.getAddress() );
            restaurant.setLunchEndHour(restaurantNew.getLunchEndHour() );
            restaurant.setPhone(restaurantNew.getPhone() );
            restaurant.setTitle(restaurantNew.getTitle() );
            restaurant.setIdByAuthorities(restaurantNew.getIdByAuthorities() );
            restaurantsRepository.saveAndFlush(restaurant );
        });
        restaurantInDb.orElseThrow(() -> new NoSuchElementException(String.format("Restaurant=%s not found for updating.", id) ) );

    }

    @Override
    public void deleteRestaurant(Long id) {
        LOGGER.debug("Delete Restaurant by id ={}", id);
        Optional<Restaurant> userInDb  = Optional.ofNullable(restaurantsRepository.findOne(id)) ;
        userInDb.ifPresent( (user) ->  {
            restaurantsRepository.delete(user);
        });
        userInDb.orElseThrow(() -> new NoSuchElementException(String.format("Restaurant=%s not found for deleting.", id)));

    }
}
