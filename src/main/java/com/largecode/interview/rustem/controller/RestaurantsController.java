package com.largecode.interview.rustem.controller;

import com.largecode.interview.rustem.domain.DomainUtils;
import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.domain.User;
import com.largecode.interview.rustem.service.RestaurantsService;
import com.largecode.interview.rustem.service.validator.RestaurantValidator;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
@RestController
@RequestMapping("/restaurants")
@Api(value = "/restaurants", description = "All kind of operations under Restaurants")
public class RestaurantsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantsController.class);
    private final RestaurantsService restaurantsService;

    static final String DEFAULT_PAGE_SIZE = "100";
    static final String DEFAULT_PAGE_NUM = "0";

    @Autowired
    public RestaurantsController(RestaurantsService restaurantsService ) {
        this.restaurantsService = restaurantsService;
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(
            value = "Get All Restaurants.", notes = "Returns list of all existed Restaurant by page."

    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
        }
    )
    @RequestMapping( value = "",  method = RequestMethod.GET  )
    public Page<Restaurant> getAllRestaurants(
                @ApiParam(value = "Page number of Restaurant's list",  required = false)
                @RequestParam(value = "page", required = true, defaultValue = DEFAULT_PAGE_NUM) Integer page,
                @ApiParam(value = "Size of Page of Restaurant's list. ", allowableValues ="range[1,1000]" , required = false)
                @RequestParam(value = "size", required = true, defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        LOGGER.debug("Get all restaurants on page {} with size {}", page, size );
        size = Math.min(1000,size);
        return  restaurantsService.getAllRestaurants(page, size);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping( value = "", method = RequestMethod.POST )
    @ApiOperation(
            value = "Create new Restaurant.", notes = "Returns a new Restaurant and persisted it to DB.",
            response = Restaurant.class
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 400, message = "Reasons:\n1:Properties \"address\", \"lunchEndHour\", \"idByAuthorities\", \"title\" must have value.\n" +
                    "2:restaurantNew.IdRestaurant set to other value than 0.\n" +
                    "3:Other restaurantNew.IdByAuthorities already exists.")
    }
    )
    public Restaurant  createRestaurant(@Valid @RequestBody Restaurant restaurantNew) {
        LOGGER.debug("Create new restaurant {}", restaurantNew);
        checkIdRestaurantEmpty(restaurantNew);
        checkIdByAuthoritiesNotExists(restaurantNew, "create new Restaurant");

        Restaurant restaurant = restaurantsService.createRestaurant(restaurantNew);
        return restaurant;

    }

    private void checkIdRestaurantEmpty(Restaurant restaurantNew) {
        if (!restaurantNew.getIdRestaurant().equals(DomainUtils.NO_ID)){
            throw new ExceptionCannotCreateRestaurantWithId(
                  String.format("IdRestaurant(%s) must be equal to %s when creating new Restaurant.", restaurantNew.getIdRestaurant(), DomainUtils.NO_ID ) );
        }
    }

    private void checkIdByAuthoritiesNotExists(Restaurant restaurantNew, String operationName) {
        Optional<Restaurant> restaurant = restaurantsService.getRestaurantByIdByAuthorities(restaurantNew.getIdByAuthorities());
        if ( restaurant.isPresent() ) {
            if (!restaurant.get().getIdRestaurant().equals(restaurantNew.getIdRestaurant())){
                throw new ExceptionOtherRestaurantWithSameIdByAuthorities(
                        String.format("Can not %s because other Restaurant (%s) already exists with the same IdByAuthorities '%s'.",
                                operationName, restaurant.get().getIdRestaurant(), restaurantNew.getIdByAuthorities() ));
            }
        }
    }

    @ApiOperation(
            value = "Find Restaurant by ID.", notes = "Returns a Restaurant if found it.",
            response = Restaurant.class
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 404, message = "Restaurant with such Id not found.")}
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Restaurant getRestaurant(
            @ApiParam(value = "ID of Restaurant from DB",  required = true)
            @PathVariable Long id) throws ExceptionRestaurantNotFound {
        LOGGER.debug("Get Restaurant by id={}", id);
        return restaurantsService.getRestaurantById(id).orElseThrow( () ->
                new ExceptionRestaurantNotFound(String.format("Restaurant=%s not found.", id)) );
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseStatus( HttpStatus.NO_CONTENT )
    @ApiOperation(
            value = "Update Restaurant.", notes = "Returns NO_CONTENT if update was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access."),
            @ApiResponse(code = 404, message = "Restaurant with such Id not found."),
            @ApiResponse(code = 400, message = "Reasons:\n" +
                    "1:Properties 'address', 'lunchEndHour', 'idByAuthorities', 'title' must have value.\n"+
                    "2:Other Restaurant.IdByAuthorities already exists.\n" +
                    "3:value of ID different between Id in URL and restaurant \n")
    }
    )
    public void updateRestaurant(
            @ApiParam(value = "ID of Restaurant from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "new properties of Restaurant",  required = true)
            @Valid @RequestBody Restaurant restaurant) {
        LOGGER.debug("Update Restaurant {} ", restaurant);
        checkUrlAndBodyForId(id, restaurant);
        checkIdByAuthoritiesNotExists(restaurant, "update Restaurant");
        try{
            restaurantsService.updateRestaurant(id, restaurant);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionRestaurantNotFound( exception.getMessage() );
        }

    }

    private void checkUrlAndBodyForId(Long id, Restaurant restaurantDto) {
        if (!id.equals(restaurantDto.getIdRestaurant()) ) {
            throw new ExceptionDifferentIdBetweenUrlAndBody(
                    String.format("'idRestaurant' in URL (%s) must be as same as in request body (%s).", id, restaurantDto.getIdRestaurant()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(
            value = "Delete Restaurant by ID.", notes = "Returns NO_CONTENT if deletion was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access."),
            @ApiResponse(code = 404, message = "Restaurant with such Id not found."),
    }
    )
    public void deleteRestaurant(@PathVariable Long id) {
        LOGGER.debug("Delete Restaurant by id={}", id);
        try {
            restaurantsService.deleteRestaurant(id);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionRestaurantNotFound(exception.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ExceptionRestaurantNotFound extends RuntimeException {
        public ExceptionRestaurantNotFound(String message){
            super(message);
        }
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionDifferentIdBetweenUrlAndBody extends RuntimeException {
        public ExceptionDifferentIdBetweenUrlAndBody(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionOtherRestaurantWithSameIdByAuthorities extends RuntimeException {
        public ExceptionOtherRestaurantWithSameIdByAuthorities(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionCannotCreateRestaurantWithId extends RuntimeException {
        public ExceptionCannotCreateRestaurantWithId(String message) {
            super(message);
        }
    }


}
