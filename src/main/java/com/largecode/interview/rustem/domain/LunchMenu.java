/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.largecode.interview.rustem.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;
import javax.persistence.*;

/**
 *
 *
 * @author Rustem.Zhunusov_at_gmail.com
 */
@Entity
@Table(name = "lunch_menu")
public class LunchMenu {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lunch_menu", nullable = false, updatable = false )
    @ApiModelProperty(value = "Id of Lunch Menu from DB.", required = true)
    private Long idLunchMenu;
    
    @JoinColumn( name = "the_restaurant", nullable = false )
    @ManyToOne(  fetch = FetchType.EAGER )
    @ApiModelProperty(value = "Link to Restaurant.", required = true)
    Restaurant theRestaurant;

    @Column(name = "the_day", nullable = false)
    @ApiModelProperty(value = "Day of relevancy of menu. Must send time code of this Date.", required = true,  dataType = "java.lang.Long")
    Date theDay;

    @OneToMany(mappedBy = "theMenu", orphanRemoval = true, cascade = CascadeType.ALL)
    @ApiModelProperty(value = "Set of Dishes on menu.", required = true)
    Set<Dish> theDishes = new HashSet<Dish>();
    
    @OneToMany(mappedBy = "theMenu", cascade = CascadeType.REMOVE)
    @JsonIgnore
    Set<Vote> theVotes = new HashSet<Vote>();

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Current status of life cycle of lunch menu.", required = true)
    StateOfMenu state= StateOfMenu.CREATED;

    public LunchMenu(Restaurant theRestaurant, Date theDay) {
        this.theRestaurant = theRestaurant;
        this.theDay = theDay;
    }

    public LunchMenu() {
    }

    public Long getIdLunchMenu() {
        return idLunchMenu;
    }

    public void setIdLunchMenu(Long idLunchMenu) {
        this.idLunchMenu = idLunchMenu;
    }

    public Restaurant getTheRestaurant() {
        return theRestaurant;
    }

    public void setTheRestaurant(Restaurant theRestaurant) {
        this.theRestaurant = theRestaurant;
    }

    public Date getTheDay() {
        return theDay;
    }

    public void setTheDay(Date theDay) {
        this.theDay = theDay;
    }

    public Set<Dish> getTheDishes() {
        return theDishes;
    }

    public void setTheDishes(Set<Dish> theDishes) {
        this.theDishes = theDishes;
    }

    public Set<Vote> getTheVotes() {
        return theVotes;
    }

    public void setTheVotes(Set<Vote> theVotes) {
        this.theVotes = theVotes;
    }

    public StateOfMenu getState() {
        return state;
    }

    public void setState(StateOfMenu state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "LunchMenu{" +
                "idLunchMenu=" + idLunchMenu +
                ", theDay=" + theDay +
                ", state=" + state +
                ", theRestaurant=" + theRestaurant +
                '}';
    }

    @ApiModelProperty(value = "Count of votes for this menu.")
    public Long getCountOfVotes(){
        return Long.valueOf( theVotes.size() );
    }

    public void addDish(Dish dish) {
        dish.setTheMenu( this);
        this.getTheDishes().add( dish );

    }

    public Dish findDishById(Long idDish ) {
        for (Dish dish : getTheDishes()) {
            if ( (dish.getIdDish()!=null) && (dish.getIdDish().equals(idDish)) ) {
                return dish;
            }
        }
        return null;

    }

    public void removeAllThatNotInList(List<Long> dishIdsFromClient) {
        Iterator<Dish> iterator = getTheDishes().iterator();
        while (iterator.hasNext() ) {
            Dish dish = iterator.next();
            if ( dish.getIdDish() == null) {continue;}
            if ( !dishIdsFromClient.contains( dish.getIdDish() ) ) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LunchMenu lunchMenu = (LunchMenu) o;

        if (!theDay.equals(lunchMenu.theDay)) return false;
        if (!theRestaurant.equals(lunchMenu.theRestaurant)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = theRestaurant.hashCode();
        result = 31 * result + theDay.hashCode();
        return result;
    }
}
