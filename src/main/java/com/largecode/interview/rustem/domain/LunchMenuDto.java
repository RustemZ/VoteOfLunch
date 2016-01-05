package com.largecode.interview.rustem.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * Data Transfer object for LunchMenu
 * Created by r.zhunusov on 25.12.2015.
 */
public class LunchMenuDto {
    @ApiModelProperty(value = "Id of Lunch Menu from DB." )
    private Long idLunchMenu = DomainUtils.NO_ID;

    @NotNull
    @ApiModelProperty(value = "Link to Restaurant.", required = true)
    Long theRestaurantId;

  //  Long theDayTime = DomainUtils.NO_ID;

    @ApiModelProperty(value = "Set of Dishes on menu.")
    Set<DishDto> theDishesDto;

    @ApiModelProperty(value = "Current status of life cycle of lunch menu.")
    StateOfMenu state= StateOfMenu.CREATED;

//    @JsonIgnore
    @NotNull
    @ApiModelProperty(value = "Day of relevancy of menu. Must send time code of this Date.", required = true,  dataType = "java.lang.Long")
    Date theDay;

    @JsonIgnore
    Restaurant  validatedTheRestaurant;


    public Long getIdLunchMenu() {
        return idLunchMenu;
    }

    public void setIdLunchMenu(Long idLunchMenu) {
        this.idLunchMenu = idLunchMenu;
    }

    public Long getTheRestaurantId() {
        return theRestaurantId;
    }

    public void setTheRestaurantId(Long theRestaurantId) {
        this.theRestaurantId = theRestaurantId;
    }


    public Set<DishDto> getTheDishesDto() {
        return theDishesDto;
    }

    public void setTheDishesDto(Set<DishDto> theDishesDto) {
        this.theDishesDto = theDishesDto;
    }

    public StateOfMenu getState() {
        return state;
    }

    public void setState(StateOfMenu state) {
        this.state = state;
    }

    public Restaurant getValidatedTheRestaurant() {
        return validatedTheRestaurant;
    }

    public void setValidatedTheRestaurant(Restaurant validatedTheRestaurant) {
        this.validatedTheRestaurant = validatedTheRestaurant;
    }

    @Override
    public String toString() {
        return "LunchMenuDto{" +
                "idLunchMenu=" + idLunchMenu +
                ", theRestaurantId=" + theRestaurantId +
                ", theDayTime=" + theDay +
                ", state=" + state +
                '}';
    }

    public Date getTheDay() {
        return theDay;
    }

    public void setTheDay(Date theDay) {
        this.theDay = theDay;
    }

    public boolean hasLogicKey(){
        return (getTheDay() !=null ) && (getValidatedTheRestaurant() != null);
    }

    public String logicalKeyDescription(){
        if (hasLogicKey()) {
            return String.format("[ theDay : '%s' , theRestaurantId : '%s' ]", getTheDay(), theRestaurantId );
        } else {
            return "No logical key from DB.";
        }
    }

}
