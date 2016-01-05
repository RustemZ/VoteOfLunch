package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.LunchMenu;
import com.largecode.interview.rustem.domain.LunchMenuDto;
import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.domain.Role;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.Optional;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
public interface LunchMenusService {
    Optional<LunchMenu> getLunchMenuById(long id , Role forRole);
    public Page<LunchMenu> getAllLunchMenus(Integer page, Integer size, Role forRole) ;

    LunchMenu createLunchMenu(LunchMenuDto lunchMenuAsDto);

    void updateLunchMenu(Long id, LunchMenuDto lunchMenuAsDto);

    void deleteLunchMenu(Long id);

    Optional<LunchMenu> getLunchMenuByTheRestaurantAndTheDay(Restaurant theRestaurant, Date onDay);

    void publishLunchMenu(Long id);
    void cancelLunchMenu(Long id);
}
