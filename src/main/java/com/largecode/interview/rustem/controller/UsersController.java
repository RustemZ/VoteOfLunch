/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.controller;

import com.largecode.interview.rustem.domain.DomainUtils;
import com.largecode.interview.rustem.domain.SpringUser;
import com.largecode.interview.rustem.domain.User;
import com.largecode.interview.rustem.domain.UserDto;
import com.largecode.interview.rustem.service.UsersService;
import com.largecode.interview.rustem.service.validator.UserDtoValidator;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
@RestController
@RequestMapping(value = "/users",  produces = {APPLICATION_JSON_VALUE} )
@Api(value = "/users", description = "All kind of operations under users")
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);
    private final UsersService usersService;
    private final UserDtoValidator userDtoValidator;
 //   private Authentication authentication;
    static final String  DEFAULT_PAGE_SIZE = "100";
    static final String DEFAULT_PAGE_NUM = "0";

    @Autowired
    public UsersController(UsersService userService, UserDtoValidator userCreateFormValidator) {
        this.usersService = userService;
        this.userDtoValidator = userCreateFormValidator;
   //     this.authentication= authentication;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userDtoValidator);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping( value = "",  method = RequestMethod.GET  )
    @ApiOperation(
            value = "Get All Users.", notes = "Returns list of all existed Users by page."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it.")
        }
    )
    public Page<User> getAllUsers(
            @ApiParam(value = "Page number of User's list",  required = false)
            @RequestParam(value = "page", required = true, defaultValue = DEFAULT_PAGE_NUM) Integer page,
            @ApiParam(value = "Size of Page of User's list. ", allowableValues ="range[1,1000]" , required = false)
            @RequestParam(value = "size", required = true, defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        size = Math.min(1000,size);
        return  usersService.getAllUsers(page, size);
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ApiOperation(
            value = "Create new User.", notes = "Returns a new User and persisted it to DB.",
            response = User.class
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role can have access to it."),
            @ApiResponse(code = 400, message = "Reasons:" +
                    "\n1:Passwords not same or too short." +
                    "\n2:userDto.idUser set to other value than 0." +
                    "\n3:userDto.email already exists." +
                    "\n4:Bad role name.")
    }
    )
    public User  createUser(@Valid @RequestBody UserDto userDto) {
        LOGGER.debug("Create new user {}", userDto);
        checkPasswordFilled(userDto);
        checkIdUserEmpty(userDto);
        checkEmailNotExists(userDto, "create new user");

        User user = usersService.createUser(userDto);
        return user;

    }

    private void checkIdUserEmpty(UserDto userDto) {
        if ( !userDto.getIdUser().equals( DomainUtils.NO_ID ) ) {
            throw new ExceptionIdUserMustBeEmpty(
                String.format("IdUser(%s) must be equal to %s when creating new User.", userDto.getIdUser(), DomainUtils.NO_ID  ));
        }
    }

    private void checkPasswordFilled(UserDto userDto) {
        if ( userDto.getPassword().equals(UserDto.NO_PASSWORD ) ) {
            throw new ExceptionUserWithoutPassword();
        }
    }

    @PreAuthorize("@userRightResolverServiceImpl.canAccessToUser(principal, #id)")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Find User by ID.", notes = "Returns a User if found him.",
            response = User.class
        )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role or User has authenticated with this Id can have access."),
            @ApiResponse(code = 404, message = "User with such Id not found.")}
    )
    public User getUser(
            @ApiParam(value = "ID of User from DB",  required = true)
            @PathVariable Long id) throws ExceptionUserNotFound {
        LOGGER.debug("Get user by id={}", id);
        return usersService.getUserById(id).orElseThrow( () ->
                new ExceptionUserNotFound(String.format("User=%s not found", id)) );
    }


    @PreAuthorize("@userRightResolverServiceImpl.canAccessToUser(principal, #id)")
    @ApiOperation(
            value = "Update User.", notes = "Returns NO_CONTENT if update was successful. Regular user can not change his Role."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role or User has authenticated with this Id can have access."),
            @ApiResponse(code = 404, message = "User with such Id not found."),
            @ApiResponse(code = 400, message = "Reasons:\n" +
                    "1:Passwords not same or too short.\n" +
                    "2:Other userDto.email already exists.\n" +
                    "3:Bad role name.\n" +
                    "3:value of ID different between Id in URL and userDto \n")
        }
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(
            @ApiParam(value = "ID of User from DB",  required = true)
            @PathVariable Long id,
            @ApiParam(value = "new properties for User by userDto",  required = true)
            @Valid @RequestBody UserDto userDto,
            @ApiParam(value = "Authentication",  hidden = true)
            Authentication authentication) {
        SpringUser springUser = (SpringUser) authentication.getPrincipal();
        LOGGER.debug("Update user {} by user '{}'", userDto, springUser.getUsername());
        checkUrlAndBodyForId(id, userDto);
        checkEmailNotExists(userDto, "update user");
        try{
            usersService.updateUser(id, userDto, springUser.getRole());
        }
        catch (NoSuchElementException exception){
            throw new ExceptionUserNotFound(exception.getMessage());
        }
    }

    private void checkUrlAndBodyForId(Long id, UserDto userDto) {
        if (!id.equals( userDto.getIdUser()) ) {
            throw new ExceptionDifferentIdBetweenUrlAndBody(
                    String.format("'idUser' in URL (%s) must be as same as in request body (%s)", id, userDto.getIdUser()));
        }
    }

    private void checkEmailNotExists(UserDto userDto, String operationName) {
        Optional<User> user = usersService.getUserByEmail(userDto.getEmail());
        if ( user.isPresent() ) {
            if (!user.get().getIdUser().equals(userDto.getIdUser())){
                throw new ExceptionOtherUserWithSameEmail(
                   String.format("Can not %s because other User (%s) already exists with the same email '%s'.",
                           operationName, user.get().getIdUser(), userDto.getEmail() ));
            }
        }
    }


    @PreAuthorize("@userRightResolverServiceImpl.canAccessToUser(principal, #id)")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(
            value = "Delete User by ID.", notes = "Returns NO_CONTENT if deletion was successful."
    )
    @ApiResponses( value = {
            @ApiResponse(code = 401, message = "Only authenticated access allowed."),
            @ApiResponse(code = 403, message = "Only user of ADMIN role or User has authenticated with this Id can have access."),
            @ApiResponse(code = 404, message = "User with such Id not found."),
    }
    )
    public void deleteUser(@PathVariable Long id) {
        LOGGER.debug("Delete user by id={}", id);
        try {
            usersService.deleteUser(id);
        }
        catch (NoSuchElementException exception){
            throw new ExceptionUserNotFound(exception.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ExceptionUserNotFound extends RuntimeException {
        public ExceptionUserNotFound(String message){
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
    static class ExceptionUserWithoutPassword extends RuntimeException {
        public ExceptionUserWithoutPassword(){
            super("Can not create new User without a password.");
        }
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionOtherUserWithSameEmail extends RuntimeException {
        public ExceptionOtherUserWithSameEmail(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionIdUserMustBeEmpty extends RuntimeException {
        public ExceptionIdUserMustBeEmpty(String message) {
            super(message);
        }
    }
}
