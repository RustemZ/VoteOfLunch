package com.largecode.interview.rustem.repository;

import com.largecode.interview.rustem.domain.User;
import com.largecode.interview.rustem.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.Optional;

/**
 * Created by r.zhunusov on 27.12.2015.
 */
public interface VotesRepository extends JpaRepository<Vote, Long> {

    @Query("select v from Vote v where v.theMenu.theDay= ?2 and v.theUser=?1 ")
    Vote findOneByUserAndDay(User user, Date day );

}
