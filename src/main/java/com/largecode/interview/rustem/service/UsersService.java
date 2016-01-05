/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.Role;
import com.largecode.interview.rustem.domain.User;
import java.util.Optional;

import com.largecode.interview.rustem.domain.UserDto;
import org.springframework.data.domain.Page;

/**
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
public interface UsersService {
        Optional<User> getUserById(long id);
        public Page<User> getAllUsers(Integer page, Integer size) ;

        User createUser(UserDto userNew);

        Optional<User> getUserByEmail(String email);

        void updateUser(Long id, UserDto userNew, Role roleOfCreator);

        void deleteUser(Long id);
}
