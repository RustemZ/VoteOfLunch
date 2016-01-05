package com.largecode.interview.rustem.service.validator;

import com.largecode.interview.rustem.domain.DishDto;
import com.largecode.interview.rustem.domain.LunchMenu;
import com.largecode.interview.rustem.domain.LunchMenuDto;
import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.service.LunchMenusService;
import com.largecode.interview.rustem.service.RestaurantsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
@Component
public class LunchMenuDtoValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LunchMenuDtoValidator.class);

    private final RestaurantsService restaurantsService;
    private final LunchMenusService lunchMenusService;


    @Autowired
    public LunchMenuDtoValidator(RestaurantsService restaurantsService, LunchMenusService lunchMenusService) {
        this.restaurantsService = restaurantsService;
        this.lunchMenusService = lunchMenusService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(LunchMenuDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOGGER.debug("Validating {}", target);
        LunchMenuDto lunchDto = (LunchMenuDto) target;
        boolean checkLogicalKey =   validateTheRestaurant(errors, lunchDto);
        if (checkLogicalKey) {
            validateNoOtherLogicalKeyAsThisExists(errors, lunchDto);
        }
        validatePrice(errors, lunchDto);
    }

    private boolean validateTheRestaurant(Errors errors, LunchMenuDto lunchDto) {
        if ( lunchDto.getTheRestaurantId() != null ){
           Optional<Restaurant> restaurant = restaurantsService.getRestaurantById(lunchDto.getTheRestaurantId());
           if (restaurant.isPresent()) {
               lunchDto.setValidatedTheRestaurant(restaurant.get() );
               return true;
           } else {
               String errorMessage = String.format(
                       "Not found a restaurant with idRestaurant=%s.", lunchDto.getIdLunchMenu());
               errors.reject("theRestaurantId.not_exists", errorMessage );
               return false;
           }
        } else {
            return false;
        }
    }

    void validateNoOtherLogicalKeyAsThisExists(Errors errors, LunchMenuDto lunchDto){
        Optional<LunchMenu> lunchMenuInDb = lunchMenusService.getLunchMenuByTheRestaurantAndTheDay(
                lunchDto.getValidatedTheRestaurant(), lunchDto.getTheDay() );
        if ( lunchMenuInDb.isPresent() ) {
            if (!lunchMenuInDb.get().getIdLunchMenu().equals(lunchDto.getIdLunchMenu()) ){
                String errorMessage = String.format("Can not create or update LunchMenu (%s) because other LunchMenu (%s)" +
                                " already exists with the same logical key '%s'.",
                    lunchDto.getIdLunchMenu(), lunchMenuInDb.get().getIdLunchMenu(), lunchDto.logicalKeyDescription() ) ;
                errors.reject("other_lunch.logical_key.exists", errorMessage );
            }
        }

    }

    private void validatePrice(Errors errors, LunchMenuDto lunchDto ) {
        if (lunchDto.getTheDishesDto()==null) {return;}
        for ( DishDto dishDto : lunchDto.getTheDishesDto()) {
            try {
                BigDecimal price = new BigDecimal( dishDto.getPriceStr() );
                dishDto.setValidatedPrice( price );
            }
            catch (Exception ex) {
                String errorMessage = String.format(
                        "Can not convert string '%s' to BigDecimal. " +
                                "Use only number and '.' in Price value. Error message: '%s'.",
                        dishDto.getPriceStr(), ex.getMessage());
                errors.reject("the_dishes_dto.price.not_big_decimal", errorMessage );
            }
        }
    }

}
