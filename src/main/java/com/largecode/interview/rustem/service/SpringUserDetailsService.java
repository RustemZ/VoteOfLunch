package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.SpringUser;
import com.largecode.interview.rustem.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SpringUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringUserDetailsService.class);
    private final UsersService usersService;

    @Autowired
    public SpringUserDetailsService(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public SpringUser loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Authenticating user with email={}", email);
        User user = usersService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email=%s was not found", email)));
        return new SpringUser(user);
    }

}
