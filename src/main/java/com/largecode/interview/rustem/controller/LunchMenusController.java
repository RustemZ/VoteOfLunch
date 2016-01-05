package com.largecode.interview.rustem.controller;

import com.largecode.interview.rustem.domain.*;
import com.largecode.interview.rustem.service.LunchMenusService;
import com.largecode.interview.rustem.service.UsersService;
import com.largecode.interview.rustem.service.VoteService;
import com.largecode.interview.rustem.service.validator.DishDtoValidator;
import com.largecode.interview.rustem.service.validator.LunchMenuDtoValidator;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
@RestController
@RequestMapping("/menus")
@Api(value = "/menus", description = "All kind of operations under lunch menus and voting on them")
public class LunchMenusController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantsController.class);
    private final LunchMenusService lunchMenusService;
    private final LunchMenuDtoValidator lunchMenuDtoValidator;
    private final VoteService voteService;
    private final UsersService usersService;

    static final String DEFAULT_PAGE_SIZE = "100";
    static final String DEFAULT_PAGE_NUM = "0";

    @Autowired
    public LunchMenusController(LunchMenusService lunchMenusService , LunchMenuDtoValidator lunchMenuDtoValidator,
                                VoteService voteService, UsersService usersService) {
        this.lunchMenusService = lunchMenusService;
        this.lunchMenuDtoValidator= lunchMenuDtoValidator;
        this.voteService = voteService;
        this.usersService= usersService;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(lunchMenuDtoValidator);
    }


    @RequestMapping( value = "",  method = RequestMethod.GET  )
    @ApiOperation(
            value = "Get All LunchMenus.", notes = "Returns list of all existed LunchMenus by page."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
    }
    )
    public Page<LunchMenu> getAllLunchMenus(
                @RequestParam(value = "page", required = true, defaultValue = DEFAULT_PAGE_NUM)
                @ApiParam(value = "Page number of LunchMenus list",  required = false)
                Integer page,
                @ApiParam(value = "Size of Page of LunchMenus list. ", allowableValues ="range[1,1000]" , required = false)
                @RequestParam(value = "size", required = true, defaultValue = DEFAULT_PAGE_SIZE) Integer size,
                @ApiParam(value ="", hidden = true)
                        Authentication authentication) {
        SpringUser springUser = (SpringUser) authentication.getPrincipal();
        LOGGER.debug("Get all LunchMenu on page {} with size {} by {}", page, size, springUser.getUsername() );

        size = Math.min(1000,size);
        return  lunchMenusService.getAllLunchMenus(page, size, springUser.getRole());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping( value = "", method = RequestMethod.POST )
    @ApiOperation(
            value = "Create new LunchMenu.", notes = "Returns a new LunchMenu and persisted it to DB.",
            response = LunchMenu.class
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 400, message = "Reasons:\n1:Properties 'theDay' and 'theRestaurantId' must have value.\n" +
                    "2:lunchMenuAsDto.idLunchMenu set to other value than 0.\n" +
                    "3:Other combination of lunchMenuAsDto.theDay and lunchMenuAsDto.theRestaurantId already exists.")
    }
    )
    public LunchMenu  createLunchMenu(
            @ApiParam(value = "new properties of LunchMenu",  required = true)
            @Valid @RequestBody LunchMenuDto lunchMenuAsDto) {
        LOGGER.debug("Create new LunchMenu {}", lunchMenuAsDto);
        checkIdLunchMenuEmpty(lunchMenuAsDto);

        LunchMenu restaurant = lunchMenusService.createLunchMenu(lunchMenuAsDto);
        return restaurant;

    }

    private void checkIdLunchMenuEmpty(LunchMenuDto lunchMenuAsDto) {
        if (!lunchMenuAsDto.getIdLunchMenu().equals(DomainUtils.NO_ID)){
            throw new ExceptionCannotCreateLunchMenuWithId(
                    String.format("IdLunchMenu(%s) must be equal to %s when creating new LunchMenu.", lunchMenuAsDto.getIdLunchMenu(), DomainUtils.NO_ID ) );
        }
    }


    @ApiOperation(
            value = "Find LunchMenu by ID.", notes = "Returns a LunchMenu if found it.",
            response = Restaurant.class
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found.")}
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LunchMenu getLunchMenu(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "authentication",  hidden = true)
            Authentication authentication) throws ExceptionLunchMenuNotFound {
        SpringUser springUser = (SpringUser) authentication.getPrincipal();
        LOGGER.debug("Get LunchMenu (id={}) by {}", id, springUser.getUsername() );
        return lunchMenusService.getLunchMenuById(id , springUser.getRole() ).orElseThrow( () ->
                new ExceptionLunchMenuNotFound(String.format("LunchMenu=%s not found.", id)) );
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(
            value = "Update new LunchMenu.", notes = "Returns NO_CONTENT if update was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found."),
            @ApiResponse(code = 400, message = "Reasons:\n1:Properties 'theDay' and 'theRestaurantId' must have value.\n" +
                    "2:value of ID different between Id in URL and lunchMenuDto .\n" +
                    "3:Other combination of lunchMenuAsDto.theDay and lunchMenuAsDto.theRestaurantId already exists.")
    }
    )
    public void updateLunchMenu(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "new properties of LunchMenu",  required = true)
            @Valid @RequestBody LunchMenuDto lunchMenuDto) {
        LOGGER.debug("Update LunchMenu {} ", lunchMenuDto);
        checkUrlAndBodyForId(id, lunchMenuDto);
        try{
            lunchMenusService.updateLunchMenu(id, lunchMenuDto);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionLunchMenuNotFound( exception.getMessage() );
        }

    }


    @ApiOperation(
            value = "Publish LunchMenu.", notes = "Returns NO_CONTENT if publication was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found."),
    }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishLunchMenu(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id) {
        LOGGER.debug("Publish LunchMenu {} ", id);
        try{
            lunchMenusService.publishLunchMenu(id);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionLunchMenuNotFound( exception.getMessage() );
        }

    }

    @ApiOperation(
            value = "Cancel LunchMenu.", notes = "Returns NO_CONTENT if cancel was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found."),
    }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelLunchMenu(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id) {
        LOGGER.debug("Cancel LunchMenu {} ", id);
        try{
            lunchMenusService.cancelLunchMenu(id);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionLunchMenuNotFound( exception.getMessage() );
        }

    }

    @ApiOperation(
            value = "Vote for LunchMenu.", notes = "Returns NO_CONTENT if voting was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found."),
    }
    )
    @RequestMapping(value = "/{id}/vote", method = RequestMethod.POST)
    public Long vote(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "authentication ",  hidden = true)
            Authentication authentication ) {
        SpringUser springUser = (SpringUser) authentication.getPrincipal();
        LOGGER.debug("Voting on LunchMenu (id={}) by {}", id, springUser.getUsername() );
        Optional<LunchMenu> menuSearch =  lunchMenusService.getLunchMenuById(id, springUser.getRole());
        menuSearch.orElseThrow(() ->
                new ExceptionLunchMenuNotFound(String.format("LunchMenu=%s not found.", id)));
        Optional<User> userSearch =   usersService.getUserById( springUser.getId() );
        userSearch.orElseThrow(() ->
                new UsersController.ExceptionUserNotFound(String.format("User=%s not found.", id)));

        Long result = voteService.vote( menuSearch.get(), userSearch.get() );
        return result;
    }

    @ApiOperation(
            value = "UnVote for LunchMenu.", notes = "Returns NO_CONTENT if unVoting was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 404, message = "LunchMenu with such Id not found."),
    }
    )
    @RequestMapping(value = "/{id}/unvote", method = RequestMethod.POST)
    public Long unVote(
            @ApiParam(value = "ID of LunchMenu from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "authentication ",  hidden = true)
            Authentication authentication ) {
        SpringUser springUser = (SpringUser) authentication.getPrincipal();
        LOGGER.debug("Voting on LunchMenu (id={}) by {}", id, springUser.getUsername() );
        Optional<LunchMenu> menuSearch =  lunchMenusService.getLunchMenuById(id, springUser.getRole());
        menuSearch.orElseThrow(() ->
                new ExceptionLunchMenuNotFound(String.format("LunchMenu=%s not found.", id)));
        Optional<User> userSearch =   usersService.getUserById( springUser.getId() );
        userSearch.orElseThrow(() ->
                new UsersController.ExceptionUserNotFound(String.format("User=%s not found.", id)));

        Long result = voteService.unVote( menuSearch.get(), userSearch.get() );
        return result;
    }


    private void checkUrlAndBodyForId(Long id, LunchMenuDto lunchMenuDto) {
        if (!id.equals(lunchMenuDto.getIdLunchMenu()) ) {
            throw new ExceptionDifferentIdBetweenUrlAndBody(
                    String.format("'idLunchMenu' in URL (%s) must be as same as in request body (%s).", id, lunchMenuDto.getIdLunchMenu()));
        }
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteLunchMenu(@PathVariable Long id) {
//        LOGGER.debug("Delete LunchMenu by id={}", id);
//        try {
//            lunchMenusService.deleteLunchMenu(id);
//        }
//        catch (NoSuchElementException exception){
//            throw new ExceptionLunchMenuNotFound(exception.getMessage());
//        }
//    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ExceptionLunchMenuNotFound extends RuntimeException {
        public ExceptionLunchMenuNotFound(String message){
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
    static class ExceptionCannotCreateLunchMenuWithId extends RuntimeException {
        public ExceptionCannotCreateLunchMenuWithId(String message) {
            super(message);
        }
    }


}
