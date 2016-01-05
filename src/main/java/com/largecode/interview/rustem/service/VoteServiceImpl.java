package com.largecode.interview.rustem.service;

import com.largecode.interview.rustem.domain.*;
import com.largecode.interview.rustem.repository.VotesRepository;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by r.zhunusov on 27.12.2015.
 */
@Service
public class VoteServiceImpl implements VoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);
    private final VotesRepository votesRepository;
    private Clock clock;

    @Autowired
    public VoteServiceImpl(VotesRepository votesRepository) {
        clock = Clock.systemUTC();

        this.votesRepository = votesRepository;
    }

    @Override
    public Long vote(LunchMenu menu, User user) {
        LOGGER.debug("Vote by user ={} for menu = {} ", user, menu );
        //menu.getTheDay()
        Vote voteOld = votesRepository.findOneByUserAndDay(user, menu.getTheDay());
        checkVotingRulesAtThisMoment(menu.getTheDay(), menu.getTheRestaurant(), voteOld);

        if ( voteOld == null ) {
            Vote voteNew  = new Vote(menu, user);
            votesRepository.saveAndFlush(voteNew);
        } else {
            if (!voteOld.isSame( menu, user )) {
                voteOld.unLink();
                votesRepository.delete(voteOld );
                Vote voteNew  = new Vote(menu, user);
                votesRepository.saveAndFlush(voteNew);
            }
        }
        return menu.getCountOfVotes();
    }

    static final Integer HOUR_OF_STOP_CHANGING_VOTE = 11;
    private void checkVotingRulesAtThisMoment(Date theDay, Restaurant restaurant, Vote voteOld) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat formatterOnlyDay = new SimpleDateFormat("yyyy-MM-dd");
        Date nowTime = Date.from( getClock().instant() );
        LOGGER.debug("checkVotingRulesAtThisMoment : Now is {}", formatter.format(nowTime));
        Date nowDay = DateUtils.truncate(nowTime, Calendar.DAY_OF_MONTH);
        Date menuDay = DateUtils.truncate(theDay, Calendar.DAY_OF_MONTH);
        if (menuDay.before(nowDay)) {
            throw new ExceptionTooLateForVoting(String.format("Can not vote because Day of Voting (%s) is gone. Now is (%s).",
                    formatterOnlyDay.format(menuDay), formatterOnlyDay.format(nowDay)));
        }
        if (menuDay.equals(nowDay)) {
            Date lunchEndTime = DateUtils.addHours(menuDay , restaurant.getLunchEndHour() );
            if ( nowTime.equals( lunchEndTime ) || nowTime.after( lunchEndTime ) ) {
                throw new ExceptionTooLateForVoting(String.format( "Can not vote because Lunch time is gone at (%s) in (%s). Now is (%s).",
                        formatter.format( lunchEndTime ), restaurant.getTitle(),  formatter.format( nowTime)  ));
            };
            if (voteOld != null) {
                Date stopChangingVote = DateUtils.addHours(menuDay , HOUR_OF_STOP_CHANGING_VOTE );
                if ( nowTime.equals( stopChangingVote ) || nowTime.after( stopChangingVote ) ) {
                    throw new ExceptionTooLateForVoting(String.format( "Can not vote because You already " +
                         "vote for LunchMenu (%s) and can not change decision after (%s). Now is (%s).",
                         voteOld.getTheMenu().getIdLunchMenu(), formatter.format( stopChangingVote ),  formatter.format( nowTime ) ));
                };
            }
        }
        if (menuDay.after(nowDay)) {
            // User can vote for menus of future days
        }
    }

    @Override
    public Long unVote(LunchMenu menu, User user) {
        LOGGER.debug("UnVote by user ={} for menu = {} ", user, menu );
//        if ( !menu.getState().equals(StateOfMenu.PUBLISHED) ) {
//            throw new ExceptionCanVoteOnlyPublishedMenu(String.format("Can not vote on LunchMenu (%s) " +
//                    "because is not PUBLISHED but %s.", menu.getIdLunchMenu() , menu.getState() ) );
//        }
        Vote voteOld = votesRepository.findOneByUserAndDay(user, menu.getTheDay());
        checkVotingRulesAtThisMoment(menu.getTheDay(), menu.getTheRestaurant(), voteOld);

        if ( voteOld == null ) {
            throw new ExceptionVoteNotFound(String.format("No vote in menu (%s) found for unVoting by current user.", menu.getIdLunchMenu() ));
        } else {
            voteOld.unLink();
            votesRepository.delete(voteOld );
        }
        return menu.getCountOfVotes();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ExceptionTooLateForVoting extends RuntimeException {
        public ExceptionTooLateForVoting(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ExceptionVoteNotFound extends RuntimeException {
        public ExceptionVoteNotFound(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ExceptionCanVoteOnlyPublishedMenu extends RuntimeException {
        public ExceptionCanVoteOnlyPublishedMenu (String message) {
            super(message);
        }
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
