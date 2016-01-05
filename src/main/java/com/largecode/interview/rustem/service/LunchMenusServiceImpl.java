package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.*;
import com.largecode.interview.rustem.repository.LunchMenusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.*;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
@Service
public class LunchMenusServiceImpl implements LunchMenusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LunchMenusServiceImpl.class);
    private final LunchMenusRepository lunchMenusRepository;

    @Autowired
    public LunchMenusServiceImpl(LunchMenusRepository lunchMenusRepository) {
        this.lunchMenusRepository = lunchMenusRepository;
    }

    @Override
    public Optional<LunchMenu> getLunchMenuById(long id, Role forRole) {
        LOGGER.debug("Getting lunchMenu={}", id);
        return lunchMenusRepository.findOneByIdLunchMenuAndStateIn(id, forRole.getAllowedStates() );
//        return Optional.ofNullable(lunchMenusRepository.findOne(id));

    }

    @Override
    public Page<LunchMenu> getAllLunchMenus(Integer page, Integer size, Role forRole) {
        LOGGER.debug("Get all lunchMenus in page {} and size {}", page, size);
        //userRepository.findAllByOrderByEmailAsc();

        Page pageOflunchMenus = lunchMenusRepository.findAllByStateInOrderByTheDayAsc(new PageRequest(page, size),
                forRole.getAllowedStates() );
        return pageOflunchMenus;
    }


    @Override
    public LunchMenu createLunchMenu(LunchMenuDto lunchMenuAsDto) {
        LOGGER.debug("Create new lunchMenu ={}", lunchMenuAsDto );
        LunchMenu lunchMenuInDb = new LunchMenu(lunchMenuAsDto.getValidatedTheRestaurant(),
                lunchMenuAsDto.getTheDay() );
        lunchMenuInDb.setState( lunchMenuAsDto.getState() );
        for(DishDto dishDto :  lunchMenuAsDto.getTheDishesDto()){
            Dish dish = new Dish( dishDto.getName(),  dishDto.getValidatedPrice() );
            lunchMenuInDb.addDish(dish);
        }

        lunchMenusRepository.saveAndFlush(lunchMenuInDb);
        return lunchMenuInDb;
    }

    @Override
    public void updateLunchMenu(Long id, LunchMenuDto lunchMenuAsDto) {
        LOGGER.debug("Update lunchMenu id = {} with data {}", id, lunchMenuAsDto);
        Optional<LunchMenu> lunchMenuSeek  = Optional.ofNullable(lunchMenusRepository.findOne(id)) ;
        lunchMenuSeek.ifPresent((lunchMenu) -> {
            if (lunchMenu.getState().equals(StateOfMenu.CREATED)) {
                lunchMenu.setTheDay(lunchMenuAsDto.getTheDay());
                lunchMenu.setState(lunchMenuAsDto.getState());
                lunchMenu.setTheRestaurant(lunchMenuAsDto.getValidatedTheRestaurant());
                List<Long> dishIdsFromClient = new ArrayList<Long>();
                for (DishDto dishDto : lunchMenuAsDto.getTheDishesDto()) {
                    if (dishDto.getIdDish().equals(DomainUtils.NO_ID) ) {
                        Dish dish = new Dish(dishDto.getName(), dishDto.getValidatedPrice());
                        lunchMenu.addDish(dish);
                    } else {
                        Dish dish = lunchMenu.findDishById( dishDto.getIdDish() );
                        if (dish ==null) {
                            dish = new Dish(dishDto.getName(), dishDto.getValidatedPrice());
                            lunchMenu.addDish(dish);
                        } else {
                            dish.setName( dishDto.getName() );
                            dish.setPrice(dishDto.getValidatedPrice());
                        }
                        dishIdsFromClient.add( dishDto.getIdDish() );
                    }
                }
                lunchMenu.removeAllThatNotInList(dishIdsFromClient);

                lunchMenusRepository.saveAndFlush(lunchMenu);
            } else {
                throw new ExceptionCanUpdateOnlyCreatedLunchMenu(id, lunchMenu.getState());
            }
        });
        lunchMenuSeek.orElseThrow(() -> new NoSuchElementException(String.format("LunchMenu=%s not found for updating.", id)));

    }

    @Override
    public void publishLunchMenu(Long id) {
        LOGGER.debug("Publish lunchMenu id = {}", id);
        Optional<LunchMenu> lunchMenuInDb  = Optional.ofNullable(lunchMenusRepository.findOne(id)) ;
        lunchMenuInDb.ifPresent( (lunchMenu) ->  {
            lunchMenu.setState( StateOfMenu.PUBLISHED );
            lunchMenusRepository.saveAndFlush(lunchMenu );
        });
        lunchMenuInDb.orElseThrow(() -> new NoSuchElementException(String.format("LunchMenu=%s not found for publishing.", id) ) );
    }

    @Override
    public void cancelLunchMenu(Long id) {
        LOGGER.debug("Cancel lunchMenu id = {}", id);
        Optional<LunchMenu> lunchMenuInDb  = Optional.ofNullable(lunchMenusRepository.findOne(id)) ;
        lunchMenuInDb.ifPresent( (lunchMenu) ->  {
            lunchMenu.setState( StateOfMenu.CANCELED );
            lunchMenusRepository.saveAndFlush(lunchMenu );
        });
        lunchMenuInDb.orElseThrow(() -> new NoSuchElementException(String.format("LunchMenu=%s not found for canceling.", id) ) );
    }

    @Override
    public void deleteLunchMenu(Long id) {

    }

    @Override
    public Optional<LunchMenu> getLunchMenuByTheRestaurantAndTheDay(Restaurant theRestaurant, Date onDay) {
        LOGGER.debug("Getting LunchMenu by theRestaurant ={} and OnDay= {}", theRestaurant, onDay);
        return  lunchMenusRepository.findOneByTheRestaurantAndTheDay(theRestaurant, onDay);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionCanUpdateOnlyCreatedLunchMenu extends RuntimeException {
        public ExceptionCanUpdateOnlyCreatedLunchMenu(Long id, StateOfMenu state) {
            super( String.format("Business rule violation: Can not update " +
                    "LunchMenu(%s) because its state isn't StateOfMenu.CREATED but %s.", id, state) ) ;
        }
    }

}
