package com.largecode.interview.rustem.controller

import com.largecode.interview.rustem.domain.Role
import com.largecode.interview.rustem.domain.StateOfMenu

import java.text.SimpleDateFormat

/**
 * Created by r.zhunusov on 26.12.2015.
 */
class TestSpecificationUtils {

    static final String URL_VOTE_OF_LUNCH = "http://localhost:8080/";
    static final Map ADMIN = [ idUser:1, password:"123456", passwordRepeated: "123456", email : "admin@gmail.com" , role: Role.ADMIN ]
    static final Map USER_1 = [ idUser:2, password:"123456", passwordRepeated: "123456", email : "user1@gmail.com" , role: Role.REGULAR]
    static final Map USER_2 = [ idUser:3, password:"123456", passwordRepeated: "123456", email : "user2@gmail.com" , role: Role.REGULAR]
    static final Map RESTAURANT_1 = [idRestaurant:1, address:"Via Sopeno Gatve, 10, Vilnius 03211", idByAuthorities:"00000001", lunchEndHour:16, phone:"+370 674 41922", title:"Dublis Restoranas"]
    static final Map RESTAURANT_2 = [idRestaurant:2, address:"Traku G. 4, Vilnius 01132", idByAuthorities:"00000002", lunchEndHour:15, phone:"+370 5 212 6874", title:"Alaus Biblioteka"]
    static final Map RESTAURANT_3 = [idRestaurant:3, address:"Verkiu G. 29 | Ogmios Miestas, Seimos Aikste 6, Vilnius 09108", idByAuthorities:"00000003", lunchEndHour:17, phone:"+370 645 52250", title:"Jurgis ir Drakonas"]
    static final Map LUNCH_MENU_D26_1 = [idLunchMenu:1, theRestaurantId: 1, theDay: convertToDate("2015-12-26").time , state : StateOfMenu.PUBLISHED, countOfVotes : 0,
           theDishes: [ [idDish:1 ,  name: "dish #1" , priceStr : "11.11"],
                        [idDish:2 ,  name: "dish #2" , priceStr : "22.22"] ]  ]
    static final Map LUNCH_MENU_D26_2 = [idLunchMenu:2, theRestaurantId: 2, theDay: convertToDate("2015-12-26").time , state : StateOfMenu.PUBLISHED, countOfVotes : 0,
           theDishes: [ [idDish:3 ,  name: "dish #3" , priceStr : "33.33"],
                        [idDish:4 ,  name: "dish #4" , priceStr : "44.44"] ] ]
    static final Map LUNCH_MENU_D27_1 = [idLunchMenu:3, theRestaurantId: 1, theDay: convertToDate("2015-12-27").time , state : StateOfMenu.PUBLISHED, countOfVotes : 0,
           theDishes: [ [idDish:5 ,  name: "dish #5" , priceStr : "55.55"],
                        [idDish:6 ,  name: "dish #6" , priceStr : "66.66"] ] ]
    static final Map LUNCH_MENU_D26_3 = [idLunchMenu:4, theRestaurantId: 3, theDay: convertToDate("2015-12-26").time , state : StateOfMenu.CREATED, countOfVotes : 0,
           theDishes: [ [idDish:7 ,  name: "dish #7" , priceStr : "77.77"],
                        [idDish:8 ,  name: "dish #8" , priceStr : "88.88"] ] ]
    static final List LUNCH_MENU_ALL = [LUNCH_MENU_D26_1, LUNCH_MENU_D26_2, LUNCH_MENU_D26_3, LUNCH_MENU_D27_1].sort {it.idLunchMenu}
    static final List LUNCH_MENU_ONLY_PUBLISHED = [LUNCH_MENU_D26_1, LUNCH_MENU_D26_2, LUNCH_MENU_D27_1].sort {it.idLunchMenu}

    static private Date convertToDate(String day){
        return new SimpleDateFormat("yyyy-MM-dd").parse(day);
    }

}
