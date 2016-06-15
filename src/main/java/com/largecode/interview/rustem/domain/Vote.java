/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.domain;


import javax.persistence.*;

/**
 *
 *  Domain object and DTO is the same in this simple project
 * @author Rustem.Zhunusov_at_gmail.com
 */
@Entity
@Table(name = "vote",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"the_user", "the_menu"})})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vote", nullable = false, updatable = false)
    private Long idVote;

    @JoinColumn( name = "the_menu")
    @ManyToOne(fetch = FetchType.EAGER)
    LunchMenu theMenu;
    
    @JoinColumn( name = "the_user")
    @ManyToOne(fetch = FetchType.EAGER)
    User theUser;

    public Vote(){}
    public Vote(LunchMenu menu, User user) {
        theMenu= menu;
        theUser= user;
        menu.getTheVotes().add( this );
        user.getTheVotes().add( this );
    }

    public Long getIdVote() {
        return idVote;
    }

    public void setIdVote(Long idVote) {
        this.idVote = idVote;
    }

    public LunchMenu getTheMenu() {
        return theMenu;
    }

    public void setTheMenu(LunchMenu theMenu) {
        this.theMenu = theMenu;
    }

    public User getTheUser() {
        return theUser;
    }

    public void setTheUser(User theUser) {
        this.theUser = theUser;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "idVote=" + idVote +
                ", theMenu=" + theMenu +
                ", theUser=" + theUser +
                '}';
    }

    public boolean isSame(LunchMenu menu, User user) {
        if ((menu==null) || (user==null)) {
            return false;
        }
        return  (getTheMenu().equals(menu) && getTheUser().equals(user));
        //return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        if (!theMenu.equals(vote.theMenu)) return false;
        if (!theUser.equals(vote.theUser)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = theMenu.hashCode();
        result = 31 * result + theUser.hashCode();
        return result;
    }

    public void unLink() {
        getTheMenu().getTheVotes().remove(this);
        getTheUser().getTheVotes().remove(this);

    }
}
