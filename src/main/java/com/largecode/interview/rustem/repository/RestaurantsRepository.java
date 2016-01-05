package com.largecode.interview.rustem.repository;

import com.largecode.interview.rustem.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by r.zhunusov on 20.12.2015.
 */
public interface RestaurantsRepository extends JpaRepository<Restaurant, Long> {
    Page findAllByOrderByTitleAsc(Pageable pageable);
    Optional<Restaurant> findOneByIdByAuthorities(String IdByAuthorities);



}
