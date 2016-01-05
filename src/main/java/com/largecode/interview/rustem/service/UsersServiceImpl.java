/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.Role;
import com.largecode.interview.rustem.domain.User;
import com.largecode.interview.rustem.domain.UserDto;
import com.largecode.interview.rustem.repository.UsersRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
@Service
public class UsersServiceImpl implements UsersService{

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);
    private final UsersRepository userRepository;

    @Autowired
    public UsersServiceImpl(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    @Override
    public Optional<User> getUserById(long id) {
        LOGGER.debug("Getting user={}", id);
        return Optional.ofNullable(userRepository.findOne(id));
    }

    @Override
    public Page<User> getAllUsers(Integer page, Integer size) {
        LOGGER.debug("Getting all users in page {} and size {}", page, size);
        //userRepository.findAllByOrderByEmailAsc();
        Page pageOfUsers = userRepository.findAllByOrderByEmailAsc(new PageRequest(page, size));
        return pageOfUsers;
    }

    @Override
    public User createUser(UserDto userDto) {
        LOGGER.debug("Create new user ={}", userDto );
        User userNew = new User(userDto.getEmail(),
                new BCryptPasswordEncoder().encode(userDto.getPassword()),
                userDto.getRole() );
        //userNew.setPasswordHash(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        userRepository.saveAndFlush(userNew);
        return userNew;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        LOGGER.debug("Getting user by email={}", email );
        return userRepository.findOneByEmail(email);
    }

    @Override
    public void updateUser(Long id, UserDto userDto, Role roleOfCreator) {
        LOGGER.debug("Update user id = {} with data {}", id, userDto);
        Optional<User> userInDb  = Optional.ofNullable( userRepository.findOne(id) ) ;
        userInDb.ifPresent( (user) ->  {
                roleOfCreator.modifyUserProperties( userDto, user  );
                userRepository.saveAndFlush( user );
            });
        userInDb.orElseThrow(() -> new NoSuchElementException(String.format("User=%s not found for updating.", id) ) );

    }

    @Override
    public void deleteUser(Long id) {
        LOGGER.debug("Delete user by id ={}", id);
        Optional<User> userInDb  = Optional.ofNullable( userRepository.findOne(id) ) ;
        userInDb.ifPresent( (user) ->  {
            userRepository.delete( user );
        });
        userInDb.orElseThrow(() -> new NoSuchElementException(String.format("User=%s not found for deleting.", id)));

    }
}
