package com.largecode.interview.rustem.domain;

import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Created by r.zhunusov on 18.12.2015.
 */

public class SpringUser extends org.springframework.security.core.userdetails.User  {


    private User user;

    public SpringUser(User user) {
        super(user.getEmail(), user.getPasswordHash(), AuthorityUtils.createAuthorityList(user.getRole().toString()));
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getIdUser();
    }

    public Role getRole() {
        return user.getRole();
    }

    @Override
    public String toString() {
        return "SpringUser{" +
                "user=" + user.toString() +
                '}';
    }
}
