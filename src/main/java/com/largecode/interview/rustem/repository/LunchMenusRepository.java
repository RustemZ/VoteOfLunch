package com.largecode.interview.rustem.repository;

import com.largecode.interview.rustem.domain.LunchMenu;
import com.largecode.interview.rustem.domain.Restaurant;
import com.largecode.interview.rustem.domain.StateOfMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * Created by r.zhunusov on 25.12.2015.
 */
public interface LunchMenusRepository extends JpaRepository<LunchMenu, Long> {

    Page findAllByStateInOrderByTheDayAsc(Pageable pageRequest, Collection<StateOfMenu> allowedStates );
    Optional<LunchMenu> findOneByTheRestaurantAndTheDay(Restaurant restaurant, Date onDay);
    Optional<LunchMenu> findOneByIdLunchMenuAndStateIn(Long id, Collection<StateOfMenu> allowedStates);

}
