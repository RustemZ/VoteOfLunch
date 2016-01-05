package com.largecode.interview.rustem.service.validator;

import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.domain.UserDto;
import com.largecode.interview.rustem.service.RestaurantsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
@Component
public class RestaurantValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantValidator.class);
    private final RestaurantsService restaurantsService;

    @Autowired
    public RestaurantValidator(RestaurantsService restaurantsService) {
        this.restaurantsService = restaurantsService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Restaurant.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOGGER.debug("Validating {}", target);
        Restaurant restaurant = (Restaurant) target;
        validateIdByAuthorities(errors, restaurant);
    }

    private void validateIdByAuthorities(Errors errors, Restaurant restaurantNew) {

        Optional<Restaurant> restaurantInDb = restaurantsService.getRestaurantByIdByAuthorities(restaurantNew.getIdByAuthorities());
        if (restaurantInDb.isPresent()) {
            if (!restaurantInDb.get().getIdRestaurant().equals(restaurantNew.getIdRestaurant())){
                String errorMessage = String.format("Other restaurant with this IdByAuthorities (%s) already exists", restaurantNew.getIdByAuthorities());
                errors.reject("idByAuthorities.exists", errorMessage);
            }
        }

    }


}
