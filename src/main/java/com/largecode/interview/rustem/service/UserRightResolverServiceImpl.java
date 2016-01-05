package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.Role;
import com.largecode.interview.rustem.domain.SpringUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
@Service
public class UserRightResolverServiceImpl implements UserRightResolverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRightResolverServiceImpl.class);

    @Override
    public boolean canAccessToUser(SpringUser currentUser, Long userId) {
        LOGGER.debug("Checking if user={} has access to user={}", currentUser, userId);
        return (currentUser != null)
                && (currentUser.getRole() == Role.ADMIN || currentUser.getId().equals(userId));
    }


}
