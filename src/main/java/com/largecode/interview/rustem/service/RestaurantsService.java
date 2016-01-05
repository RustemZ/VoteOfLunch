package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.Restaurant;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
public interface RestaurantsService {
    Optional<Restaurant> getRestaurantById(long id);
    public Page<Restaurant> getAllRestaurants(Integer page, Integer size) ;

    Optional<Restaurant> getRestaurantByIdByAuthorities(String idByAuthorities );
    Restaurant createRestaurant(Restaurant restaurantNew);

    void updateRestaurant(Long id, Restaurant restaurantNew);

    void deleteRestaurant(Long id);

}
