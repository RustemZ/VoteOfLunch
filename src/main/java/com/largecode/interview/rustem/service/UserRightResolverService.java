package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.SpringUser;



/**
 * Created by r.zhunusov on 20.12.2015.
 */
public interface UserRightResolverService {

    boolean canAccessToUser(SpringUser currentUser, Long userId);

}
