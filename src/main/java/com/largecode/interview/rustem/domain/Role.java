/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.domain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
public enum Role {
    REGULAR( ((UserDto userDto, User userInDb) -> {
        generalUserModification(userDto, userInDb);
        }), Arrays.asList( StateOfMenu.PUBLISHED ) ),
    ADMIN ( ((UserDto userDto, User userInDb) -> {
        generalUserModification(userDto, userInDb);
        userInDb.setRole(userDto.getRole() );
        } ), Arrays.asList( StateOfMenu.PUBLISHED, StateOfMenu.CREATED, StateOfMenu.CANCELED )) ;

    private final ModifyUserProperties userModifier;
    private final Collection<StateOfMenu> allowedStates;

    Role(ModifyUserProperties userModifier, Collection<StateOfMenu>  allowedStates){
        this.userModifier= userModifier;
        this.allowedStates = allowedStates;
    }

    private static void generalUserModification(UserDto userDto, User userInDb){
        if ( !userDto.getPassword().equals(UserDto.NO_PASSWORD) ) {
            userInDb.setPasswordHash(new BCryptPasswordEncoder().encode(userDto.getPassword()));
        }
        userInDb.setEmail(userDto.getEmail());
    }

    public void modifyUserProperties( UserDto userDto, User userInDb ){
       userModifier.modify( userDto, userInDb );
    }

    public Collection<StateOfMenu> getAllowedStates() {
        return new ArrayList<StateOfMenu>(allowedStates) ;
    }
}

interface ModifyUserProperties{
    void  modify( UserDto userDto, User userInDb );
}
