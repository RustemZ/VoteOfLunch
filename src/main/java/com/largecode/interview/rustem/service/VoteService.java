package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.LunchMenu;
import com.largecode.interview.rustem.domain.User;

/**
 * Created by r.zhunusov on 27.12.2015.
 */
public interface VoteService {
    Long vote (LunchMenu menu , User user);

    Long unVote(LunchMenu lunchMenu, User user);
}
