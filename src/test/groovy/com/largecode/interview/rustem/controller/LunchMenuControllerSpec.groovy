package com.largecode.interview.rustem.controller

import com.largecode.interview.rustem.Application
import com.largecode.interview.rustem.domain.Role
import com.largecode.interview.rustem.domain.StateOfMenu
import com.largecode.interview.rustem.service.VoteService
import com.largecode.interview.rustem.service.VoteServiceImpl
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.http.entity.ContentType
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpStatus
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import static TestSpecificationUtils.*

/**
 * Created by r.zhunusov on 26.12.2015.
 */
@Stepwise
class LunchMenuControllerSpec extends Specification{

    @Shared
    @AutoCleanup
    ConfigurableApplicationContext context

    void setupSpec() {
        Future future = Executors
                .newSingleThreadExecutor().submit(
                new Callable() {
                    @Override
                    public ConfigurableApplicationContext call() throws Exception {
                        return (ConfigurableApplicationContext) SpringApplication
                                .run(Application.class)
                    }
                })
        context = future.get(90, TimeUnit.SECONDS)
    }


    @Shared
    def lunchMenuRest = new RESTClient( URL_VOTE_OF_LUNCH , ContentType.APPLICATION_JSON)

    void "try to get all initial LunchMenus with a bad credentials!"() {
        when:
            lunchMenuRest.auth.basic USER_1.email, "bad_password"
            def responseOfGet = null
            Map responseData = null;
            lunchMenuRest.request( Method.GET) { request ->
                uri.path = '/menus'
                response.failure = { resp, data ->
                    responseOfGet= resp;
                    responseData= data;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
    }

    //[lunchMenuId : it.theRestaurantId, theDay: it.theDay.time ]
    void "get all initial LunchMenus by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            def response =  lunchMenuRest.get ( path:"menus" )
            Map pageOfLunchMenus =  response.data
            List allLunchMenusIds =  pageOfLunchMenus.content.collect{ it.idLunchMenu }.sort()
            //println pageOfLunchMenus
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            allLunchMenusIds == LUNCH_MENU_ALL.collect { it.idLunchMenu }.sort() ;
    }

    void "get all initial only PUBLISHED LunchMenus by USER_1!"() {
        when:
            lunchMenuRest.auth.basic  USER_1.email, USER_1.password
            def response =  lunchMenuRest.get ( path:"menus" )
            Map pageOfLunchMenus =  response.data
            List allLunchMenusIds =  pageOfLunchMenus.content.collect{ it.idLunchMenu }.sort()
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            allLunchMenusIds == LUNCH_MENU_ONLY_PUBLISHED.collect { it.idLunchMenu }.sort() ;
    }

    void "get LUNCH_MENU_D26_1 by ADMIN!"() {
        when:
            def dishesIds = LUNCH_MENU_D26_1.theDishes.collect{ return it.idDish;}.sort();
            def dishesNames = LUNCH_MENU_D26_1.theDishes.collect{ return it.name;}.sort();
            def dishesPrices = LUNCH_MENU_D26_1.theDishes.collect{ return it.priceStr;}.sort();
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String url = "menus/${LUNCH_MENU_D26_1.idLunchMenu}";
            def response =  lunchMenuRest.get ( path:url )
            def lunchMenuFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            lunchMenuFromRest.idLunchMenu == LUNCH_MENU_D26_1.idLunchMenu
            lunchMenuFromRest.theRestaurant.idRestaurant == LUNCH_MENU_D26_1.theRestaurantId
            lunchMenuFromRest.theDay == LUNCH_MENU_D26_1.theDay
            lunchMenuFromRest.state == LUNCH_MENU_D26_1.state.toString()
            lunchMenuFromRest.countOfVotes == LUNCH_MENU_D26_1.countOfVotes
            lunchMenuFromRest.theDishes.size() == LUNCH_MENU_D26_1.theDishes.size()
            lunchMenuFromRest.theDishes.collect{it.idDish}.sort() == dishesIds;
            lunchMenuFromRest.theDishes.collect{it.name}.sort() == dishesNames;
            lunchMenuFromRest.theDishes.collect{it.priceStr}.sort() == dishesPrices;
    }

    void "get LUNCH_MENU_D26_2 by USER_1!"() {
        when:
            def dishesIds = LUNCH_MENU_D26_2.theDishes.collect{ return it.idDish;}.sort();
            def dishesNames = LUNCH_MENU_D26_2.theDishes.collect{ return it.name;}.sort();
            def dishesPrices = LUNCH_MENU_D26_2.theDishes.collect{ return it.priceStr;}.sort();
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String url = "menus/${LUNCH_MENU_D26_2.idLunchMenu}";
            def response =  lunchMenuRest.get ( path:url )
            def lunchMenuFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            lunchMenuFromRest.idLunchMenu == LUNCH_MENU_D26_2.idLunchMenu
            lunchMenuFromRest.theRestaurant.idRestaurant == LUNCH_MENU_D26_2.theRestaurantId
            lunchMenuFromRest.theDay == LUNCH_MENU_D26_2.theDay
            lunchMenuFromRest.state == LUNCH_MENU_D26_2.state.toString()
            lunchMenuFromRest.countOfVotes == LUNCH_MENU_D26_2.countOfVotes
            lunchMenuFromRest.theDishes.size() == LUNCH_MENU_D26_2.theDishes.size()
            lunchMenuFromRest.theDishes.collect{it.idDish}.sort() == dishesIds;
            lunchMenuFromRest.theDishes.collect{it.name}.sort() == dishesNames;
            lunchMenuFromRest.theDishes.collect{it.priceStr}.sort() == dishesPrices;

    }

    void "try to get LUNCH_MENU_D26_3 without credentials !"() {
        when:
            lunchMenuRest.auth.basic "", ""
            def responseOfGet = null
            Map responseData = null;
            lunchMenuRest.request( Method.GET) { request ->
                uri.path = "/menus/${LUNCH_MENU_D26_3.idLunchMenu}"
                response.failure = { resp, data ->
                    responseOfGet= resp;
                    responseData= data;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
    }

    void "try to get unreal LunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url = "/menus/10000";
            def responseOfGet = null
            Map responseData = null;
            lunchMenuRest.request( Method.GET) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfGet= resp;
                    responseData= data
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionLunchMenuNotFound")
            responseData.message == "LunchMenu=10000 not found."
    }


    void "create first new LunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuNewDto = [ theRestaurantId: 1, theDay: convertToDate("2015-12-28").time ,
                                    theDishesDto: [ [ name:"dish #9" , priceStr : "99.99"],
                                                 [ name:"dish #10" , priceStr : "100.100"] ]  ]

            def response =  lunchMenuRest.post ( path:"menus" ,
                    body: lunchMenuNewDto )
            def lunchMenuFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            lunchMenuFromRest.idLunchMenu >0
            lunchMenuFromRest.theRestaurant.idRestaurant == lunchMenuNewDto.theRestaurantId
            lunchMenuFromRest.theDay == lunchMenuNewDto.theDay
            lunchMenuFromRest.state == StateOfMenu.CREATED.toString(); // default state is CREATED
            lunchMenuFromRest.countOfVotes == 0
            lunchMenuFromRest.theDishes.size() == lunchMenuNewDto.theDishesDto.size()
            lunchMenuFromRest.theDishes.collect{it.name}.sort() == lunchMenuNewDto.theDishesDto.collect{it.name}.sort()
            lunchMenuFromRest.theDishes.collect{it.priceStr}.sort() == lunchMenuNewDto.theDishesDto.collect{it.priceStr}.sort()
    }


    void "try to create new LunchMenu by regular USER_1!"() {
        when:
            lunchMenuRest.auth.basic  USER_1.email, USER_1.password
            Map lunchMenuNewDto = [ theRestaurantId: 1, theDay: convertToDate("2016-01-28").time , state : StateOfMenu.CREATED,
                                theDishesDto: [ [ name:"dish #11" , priceStr : "110.110"],
                                                [ name:"dish #12" , priceStr : "120.120"] ]  ]
            def responseOfPost = null
            Map responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus';
                body = lunchMenuNewDto;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data;
                }
            }
        then:
            responseOfPost != null
            responseData !=null;
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException")
            responseData.message == "Access is denied"
    }



    void "try to create new LunchMenu with existed logical key by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuNewDto = [ theRestaurantId: LUNCH_MENU_D26_1.theRestaurantId , theDay: LUNCH_MENU_D26_1.theDay ,
                                    theDishesDto: [ [ name:"dish #11" , priceStr : "110.110"],
                                                    [ name:"dish #12" , priceStr : "120.120"] ]  ]
            def responseOfPost = null;
            def responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus'
                body = lunchMenuNewDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==1
            responseData.errors[0].code == "other_lunch.logical_key.exists";
            responseData.errors[0].defaultMessage.contains("other LunchMenu ("+LUNCH_MENU_D26_1.theRestaurantId+") already exists");
    }


    void "try to create new LunchMenu with filled idLunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuNewDto = [ idLunchMenu: 10, theRestaurantId: 1, theDay: convertToDate("2016-02-28").time , state : StateOfMenu.CREATED,
                                theDishesDto: [ [ name:"dish #11" , priceStr : "110.110"],
                                                [ name:"dish #12" , priceStr : "120.120"] ]  ]
            def responseOfPost = null;
            def responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus'
                body = lunchMenuNewDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionCannotCreateLunchMenuWithId");
            responseData.message == "IdLunchMenu(10) must be equal to 0 when creating new LunchMenu."

    }


    void "try to create new LunchMenu by ADMIN and check validation !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuDto = [ : ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus'
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
            def fieldsWithWrongValidation =  ["theDay", "theRestaurantId" ]
    //        def fieldsWithWrongValidation =  ["theDay", "theRestaurantId", "name", "priceStr"]
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==2
            responseData.errors.collect{it.field}.sort() == fieldsWithWrongValidation.sort();

    }

    void "try to create new LunchMenu by ADMIN with bad dishes !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuDto = [ theRestaurantId: 1, theDay: convertToDate("2016-02-28").time , theDishesDto: [ [ ] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus'
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("HttpMessageNotReadableException");
            responseData.message.contains("DishDto");

    }


    void "try to create new LunchMenu by ADMIN with bad price of dish !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuDto = [ theRestaurantId: 1, theDay: convertToDate("2016-02-28").time ,
                                 theDishesDto:  [[ name:"dish #13" , priceStr : "110,110"],
                                                 [ name:"dish #14" , priceStr : "110d110"] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus'
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==2
            responseData.errors.collect{it.code}.unique() == ['the_dishes_dto.price.not_big_decimal'];
            responseData.errors.collect{it.defaultMessage}.any { it.contains ("110,110") }
            responseData.errors.collect{it.defaultMessage}.any { it.contains ("110d110") }
    }


    void "try to create new LunchMenu without credentials!"() {
        when:
            lunchMenuRest.auth.basic ( "", "" )
            Map lunchMenuNewDto = [  theRestaurantId: 1, theDay: convertToDate("2016-02-28").time  ]
            def responseOfPost = null
            Map responseData = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = '/menus';
                body = lunchMenuNewDto;
                response.failure = { resp, data ->
                    responseOfPost= resp;
                    responseData= data
                }
            }
        then:
            responseOfPost != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.UNAUTHORIZED
            responseData.message == "Bad credentials"
    }

    void "create second new LunchMenu with PUBLISHED state by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            Map lunchMenuNewDto = [ theRestaurantId: 2, theDay: convertToDate("2015-12-28").time , state: StateOfMenu.PUBLISHED,
                                    theDishesDto: [ [ name:"dish #15" , priceStr : "150.150"],
                                                    [ name:"dish #16" , priceStr : "160.160"] ]  ]
            def response =  lunchMenuRest.post ( path:"menus" ,
                    body: lunchMenuNewDto )
            def lunchMenuFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            lunchMenuFromRest.idLunchMenu >0
            lunchMenuFromRest.theRestaurant.idRestaurant == lunchMenuNewDto.theRestaurantId
            lunchMenuFromRest.theDay == lunchMenuNewDto.theDay
            lunchMenuFromRest.state == lunchMenuNewDto.state.toString()
            lunchMenuFromRest.countOfVotes == 0
            lunchMenuFromRest.theDishes.size() == lunchMenuNewDto.theDishesDto.size()
            lunchMenuFromRest.theDishes.collect{it.name}.sort() == lunchMenuNewDto.theDishesDto.collect{it.name}.sort()
            lunchMenuFromRest.theDishes.collect{it.priceStr}.sort() == lunchMenuNewDto.theDishesDto.collect{it.priceStr}.sort()
    }


    void "update of LUNCH_MENU_D26_3 by ADMIN  !"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String urlToLunchMenu1 = "/menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                                 theDay: convertToDate("2016-02-01").time,
                                 theDishesDto: [ [ idDish:7 , name:"dish #7 new" , priceStr : "77.01"],
                                                 [ idDish:8 , name:"dish #8 new" , priceStr : "88.01"] ] ]
            def dishesIds = lunchMenuDto.theDishesDto.collect{ return it.idDish;}.sort();
            def dishesNames = lunchMenuDto.theDishesDto.collect{ return it.name;}.sort();
            def dishesPrices = lunchMenuDto.theDishesDto.collect{ return it.priceStr;}.sort();

            def responsePut1 =  lunchMenuRest.put ( path:urlToLunchMenu1 ,  body: lunchMenuDto ) // update with new values
            def responseGet1 =  lunchMenuRest.get ( path:urlToLunchMenu1 )
            def lunchMenuFromRest1 =  responseGet1.data

        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            lunchMenuFromRest1.idLunchMenu == lunchMenuDto.idLunchMenu
            lunchMenuFromRest1.theRestaurant.idRestaurant == lunchMenuDto.theRestaurantId
            lunchMenuFromRest1.theDay == lunchMenuDto.theDay
            lunchMenuFromRest1.state == LUNCH_MENU_D26_3.state.toString()
            lunchMenuFromRest1.countOfVotes == LUNCH_MENU_D26_3.countOfVotes
            lunchMenuFromRest1.theDishes.size() == lunchMenuDto.theDishesDto.size()
            lunchMenuFromRest1.theDishes.collect{it.idDish}.sort() == dishesIds;
            lunchMenuFromRest1.theDishes.collect{it.name}.sort() == dishesNames;
            lunchMenuFromRest1.theDishes.collect{it.priceStr}.sort() == dishesPrices;
    }

    void "update of LUNCH_MENU_D26_3 by ADMIN with addition and deletion of dishes !"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String urlToLunchMenu1 = "/menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                                 theDay: convertToDate("2016-02-01").time,
                                 theDishesDto: [ [  name:"dish #7 new 2" , priceStr : "77.02"],
                                                 [ idDish:8 , name:"dish #8 new" , priceStr : "88.02"] ] ]
            def dishesNames = lunchMenuDto.theDishesDto.collect{ return it.name;}.sort();
            def dishesPrices = lunchMenuDto.theDishesDto.collect{ return it.priceStr;}.sort();

            def responsePut1 =  lunchMenuRest.put ( path:urlToLunchMenu1 ,  body: lunchMenuDto ) // update with new values
            def responseGet1 =  lunchMenuRest.get ( path:urlToLunchMenu1 )
            def lunchMenuFromRest1 =  responseGet1.data

        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            lunchMenuFromRest1.idLunchMenu == lunchMenuDto.idLunchMenu
            lunchMenuFromRest1.theRestaurant.idRestaurant == lunchMenuDto.theRestaurantId
            lunchMenuFromRest1.theDay == lunchMenuDto.theDay
            lunchMenuFromRest1.state == LUNCH_MENU_D26_3.state.toString()
            lunchMenuFromRest1.countOfVotes == LUNCH_MENU_D26_3.countOfVotes
            lunchMenuFromRest1.theDishes.size() == lunchMenuDto.theDishesDto.size()
            lunchMenuFromRest1.theDishes.collect{it.name}.sort() == dishesNames;
            lunchMenuFromRest1.theDishes.collect{it.priceStr}.sort() == dishesPrices;
    }


    void "try to update unreal LunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String url = "menus/15000";
            def lunchMenuDto = [ idLunchMenu: 15000,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                             theDay: convertToDate("2016-02-02").time,
                             theDishesDto: [ [ name:"dish #7 new 3" , priceStr : "77.04"],
                                             [ name:"dish #8 new 3" , priceStr : "88.04"] ] ]
            def responseOfPut = null;
            def responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionLunchMenuNotFound")
            responseData.message == "LunchMenu=15000 not found for updating."

    }

    void "try to update LUNCH_MENU_D26_3 by ADMIN with different ID between URL and JSON!"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String url = "menus/${16000}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu ,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                             theDay: convertToDate("2016-02-03").time,
                             theDishesDto: [ [  name:"dish #7 new 3" , priceStr : "77.04"],
                                             [  name:"dish #8 new 3" , priceStr : "88.04"] ] ]
            def responseOfPut = null;
            def responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionDifferentIdBetweenUrlAndBody");
            responseData.message == "'idLunchMenu' in URL (16000) must be as same as in request body ($LUNCH_MENU_D26_3.idLunchMenu)."
    }

    void "try to update LUNCH_MENU_D26_3 by USER_1!"() {
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String url = "menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu ,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                                 theDay: convertToDate("2016-02-04").time,
                                 theDishesDto: [ [  name:"dish #7 new 5" , priceStr : "77.05"],
                                                 [  name:"dish #8 new 5" , priceStr : "88.05"] ] ]
            def responseOfPut = null;
            def responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException");
            responseData.message == "Access is denied"
    }


    void "try to update LUNCH_MENU_D26_3 with logical key of LUNCH_MENU_D26_1 !"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String url = "menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu ,  theRestaurantId: LUNCH_MENU_D26_1.theRestaurantId,
                                 theDay: LUNCH_MENU_D26_1.theDay,
                                 theDishesDto: [ [  name:"dish #7 new 6" , priceStr : "77.06"],
                                                 [  name:"dish #8 new 6" , priceStr : "88.06"] ] ]

            def responseOfPut = null;
            def responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==1
            responseData.errors[0].code == 'other_lunch.logical_key.exists';
            responseData.errors[0].defaultMessage.contains("Can not create or update LunchMenu (4) because other LunchMenu (1)")
    }

    void "try to update RESTAURANT_2 by ADMIN and check fields validation !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url = "menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            def lunchMenuDto = [ : ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
            def fieldsWithWrongValidation =  ["theRestaurantId", "theDay" ]
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==2
            responseData.errors.collect{it.field}.sort() == fieldsWithWrongValidation.sort();

    }

    void "try to update LUNCH_MENU_D26_3 by ADMIN with bad dishes !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url = "menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            Map lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu ,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                                 theDay: LUNCH_MENU_D26_3.theDay, theDishesDto: [ [ ] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("HttpMessageNotReadableException");
            responseData.message.contains("DishDto");

    }


    void "try to update LUNCH_MENU_D26_3 by ADMIN with bad price of dish !"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url = "menus/${LUNCH_MENU_D26_3.idLunchMenu}";
            Map lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_3.idLunchMenu ,  theRestaurantId: LUNCH_MENU_D26_3.theRestaurantId,
                                 theDay: LUNCH_MENU_D26_3.theDay,
                                 theDishesDto:  [[ name:"dish #888" , priceStr : "888,8"],
                                                 [ name:"dish #999" , priceStr : "999d9"] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==2
            responseData.errors.collect{it.code}.unique() == ['the_dishes_dto.price.not_big_decimal'];
            responseData.errors.collect{it.defaultMessage}.any { it.contains ("888,8") }
            responseData.errors.collect{it.defaultMessage}.any { it.contains ("999d9") }
    }

    void "try update already PUBLISHED LUNCH_MENU_D26_1 by ADMIN  !"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String url = "/menus/${LUNCH_MENU_D26_1.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D26_1.idLunchMenu,  theRestaurantId: LUNCH_MENU_D26_1.theRestaurantId,
                                 theDay: convertToDate("2016-02-01").time,
                                 theDishesDto: [ [ idDish:1 , name:"dish #1 new" , priceStr : "11.01"],
                                                 [ idDish:2 , name:"dish #2 new" , priceStr : "22.01"] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }

        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionCanUpdateOnlyCreatedLunchMenu");
            responseData.message == "Business rule violation: Can not update LunchMenu(1) because its state isn't StateOfMenu.CREATED but PUBLISHED."
    }

    void "cancel and try update LUNCH_MENU_D27_1 by ADMIN  !"() {
        when:
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String urlCancelMenu = "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/cancel";
            def responsePut1 =  lunchMenuRest.put ( path: urlCancelMenu ) // change state of LUNCH_MENU_D27_1 from PUBLISHED to CANCELED

            String url = "/menus/${LUNCH_MENU_D27_1.idLunchMenu}";
            def lunchMenuDto = [ idLunchMenu: LUNCH_MENU_D27_1.idLunchMenu,  theRestaurantId: LUNCH_MENU_D27_1.theRestaurantId,
                                 theDay: convertToDate("2016-02-07").time,
                                 theDishesDto: [ [  name:"dish #111 new" , priceStr : "11.01"],
                                                 [  name:"dish #222 new" , priceStr : "22.01"] ] ]
            def responseOfPost = null;
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url
                body = lunchMenuDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }

            String urlPublishMenu = "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/publish";
            def responsePut2 =  lunchMenuRest.put ( path: urlPublishMenu ) // republish LUNCH_MENU_D27_1 again
            def responseGet2 =  lunchMenuRest.get ( path: url )
            def lunchMenuFromRest2 =  responseGet2.data

        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT

            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionCanUpdateOnlyCreatedLunchMenu");
            responseData.message == "Business rule violation: Can not update LunchMenu(3) because its state isn't StateOfMenu.CREATED but CANCELED."

            HttpStatus.valueOf(responsePut2.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.OK
            lunchMenuFromRest2.idLunchMenu == LUNCH_MENU_D27_1.idLunchMenu
            lunchMenuFromRest2.state == StateOfMenu.PUBLISHED.toString()

    }


    void "cancel LUNCH_MENU_D27_1 and republish it  by ADMIN  !"() {
        when:
            String urlGetMenu =  "/menus/${LUNCH_MENU_D27_1.idLunchMenu}";
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            def responseGet1 = lunchMenuRest.get ( path:urlGetMenu )
            def lunchMenuFromRest1 =  responseGet1.data

            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String urlCancelMenu = "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/cancel";
            def responsePut1 =  lunchMenuRest.put ( path: urlCancelMenu ) // change state of LUNCH_MENU_D27_1 from PUBLISHED to CANCELED
            def responseGet2 =  lunchMenuRest.get ( path: urlGetMenu )
            def lunchMenuFromRest2 =  responseGet2.data

            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            // try get LUNCH_MENU_D27_1 by USER_1
            def responseOfGet3 = null
            Map responseData3 = null;
            lunchMenuRest.request( Method.GET) { request ->
                uri.path = urlGetMenu;
                response.failure = { resp , data ->
                    responseOfGet3= resp;
                    responseData3= data
                }
            }

            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            String urlPublishMenu = "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/publish";
            def responsePut2 =  lunchMenuRest.put ( path: urlPublishMenu ) // republish LUNCH_MENU_D27_1 again
            def responseGet4 =  lunchMenuRest.get ( path: urlGetMenu )
            def lunchMenuFromRest4 =  responseGet4.data

        then:
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            lunchMenuFromRest1.idLunchMenu == LUNCH_MENU_D27_1.idLunchMenu

            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.OK
            lunchMenuFromRest2.idLunchMenu == LUNCH_MENU_D27_1.idLunchMenu
            lunchMenuFromRest2.state == StateOfMenu.CANCELED.toString()

            responseOfGet3 != null
            HttpStatus.valueOf(responseOfGet3.status) == HttpStatus.NOT_FOUND
            responseData3.exception.contains("ExceptionLunchMenuNotFound")
            responseData3.message == "LunchMenu=${LUNCH_MENU_D27_1.idLunchMenu} not found."

            HttpStatus.valueOf(responsePut2.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet4.status) == HttpStatus.OK
            lunchMenuFromRest4.idLunchMenu == LUNCH_MENU_D27_1.idLunchMenu
            lunchMenuFromRest4.state == StateOfMenu.PUBLISHED.toString()
    }

    void "try to publish unreal LunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url =  "/menus/30000/publish";
            def responseOfPut = null
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData= data
                }
            }
        then:
            responseOfPut != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionLunchMenuNotFound")
            responseData.message == "LunchMenu=30000 not found for publishing."
    }

    void "try to cancel unreal LunchMenu by ADMIN!"() {
        when:
            lunchMenuRest.auth.basic  ADMIN.email, ADMIN.password
            String url =  "/menus/35000/cancel";
            def responseOfPut = null
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData= data
                }
            }
        then:
            responseOfPut != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionLunchMenuNotFound")
            responseData.message == "LunchMenu=35000 not found for canceling."
    }

    void "try to publish LUNCH_MENU_D26_3 by USER_1!"() {
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String url =  "/menus/${LUNCH_MENU_D26_3.idLunchMenu}/publish";
            def responseOfPut = null
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData= data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException");
            responseData.message == "Access is denied"
    }

    void "try to cancel LUNCH_MENU_D26_1 by USER_1!"() {
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String url =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/cancel";
            def responseOfPut = null
            Map responseData = null;
            lunchMenuRest.request( Method.PUT) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData= data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException");
            responseData.message == "Access is denied"
    }


     void "twice vote for LUNCH_MENU_D26_1 and after that one for LUNCH_MENU_D26_2 by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant   instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password

            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";

            def response1 =  lunchMenuRest.post ( path: urlVote1  )
            def response2 =  lunchMenuRest.post ( path: urlVote1  )
            String urlVote2 =  "/menus/${LUNCH_MENU_D26_2.idLunchMenu}/vote";
            def response3 =  lunchMenuRest.post ( path: urlVote2  )
            String urlGet1 = "menus/${LUNCH_MENU_D26_1.idLunchMenu}";
            def responseGet1 =  lunchMenuRest.get ( path: urlGet1 )
            def lunchMenuFromRest1 =  responseGet1.data
            String urlGet2 = "menus/${LUNCH_MENU_D26_2.idLunchMenu}";
            def responseGet2 =  lunchMenuRest.get ( path: urlGet2 )
            def lunchMenuFromRest2 =  responseGet2.data
            String urlUnVote =  "/menus/${LUNCH_MENU_D26_2.idLunchMenu}/unvote";
            def response4 =  lunchMenuRest.post ( path: urlUnVote  )
        then:
            4*clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(response1.status) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf(response2.status) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf(response3.status) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            lunchMenuFromRest1.countOfVotes == 0
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.OK
            lunchMenuFromRest2.countOfVotes == 1
            HttpStatus.valueOf(response4.status) == HttpStatus.OK
            response4.responseData==0
        cleanup:
            voteService.setClock( Clock.systemUTC() );
    }


    void "try twice vote for LUNCH_MENU_D26_1 after beginning of lunch by USER_1 and unVote!"() {
        setup:
                Clock clock = Mock(Clock);
                VoteService voteService = context.getBean(VoteServiceImpl.class);
                voteService.setClock( clock );
                LocalDateTime date15_12_26_11_00 = LocalDateTime.parse("2015-12-26T11:00:00", DateTimeFormatter.ISO_DATE_TIME);
                Instant instant15_12_26_11_00 = date15_12_26_11_00.atZone(ZoneId.systemDefault()).toInstant();
                LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
                Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
                lunchMenuRest.auth.basic USER_1.email, USER_1.password
                String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";

                def response1 =  lunchMenuRest.post ( path: urlVote1  )
                def responseOfPost = null
                Map responseError1 = null;
                lunchMenuRest.request( Method.POST) { request ->
                    uri.path = urlVote1;
                    response.failure = { resp , data ->
                        responseOfPost= resp;
                        responseError1= data
                    }
                }
                String urlUnVote =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/unvote";
                def responseUnVote =  lunchMenuRest.post ( path: urlUnVote  )

        then:
            2*clock.instant() >> { return instant15_12_26_11_00; }
            1*clock.instant() >> { return instant15_12_26_10_59; } // reverse time back
            HttpStatus.valueOf(response1.status) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseError1.message.contains("Can not vote because You already vote for LunchMenu (1)");
            HttpStatus.valueOf(responseUnVote.status) == HttpStatus.OK
            responseUnVote.responseData==0

        cleanup:
            voteService.setClock( Clock.systemUTC() );

    }

    void "try vote for LUNCH_MENU_D26_1 on next day by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_27_10_59 = LocalDateTime.parse("2015-12-27T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_27_10_59 = date15_12_27_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";

            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }

        then:
            clock.instant() >> { return instant15_12_27_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseError1.message == "Can not vote because Day of Voting (2015-12-26) is gone. Now is (2015-12-27).";

        cleanup:
            voteService.setClock( Clock.systemUTC() );

    }

    void "try vote for LUNCH_MENU_D26_1 after end of lunch time by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_15_59 = LocalDateTime.parse("2015-12-26T15:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_15_59 = date15_12_26_15_59.atZone(ZoneId.systemDefault()).toInstant();
            LocalDateTime date15_12_26_16_00 = LocalDateTime.parse("2015-12-26T16:00:00", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_16_00 = date15_12_26_16_00.atZone(ZoneId.systemDefault()).toInstant();
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";
            def response1 =  lunchMenuRest.post ( path: urlVote1  )

            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }
            String urlUnVote =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/unvote";
            def responseUnVote =  lunchMenuRest.post ( path: urlUnVote  )

        then:
            1*clock.instant() >> { return instant15_12_26_15_59; }
            1*clock.instant() >> { return instant15_12_26_16_00; }
            1*clock.instant() >> { return instant15_12_26_10_59; } // reverse time back

            HttpStatus.valueOf( response1.status ) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf( responseOfPost.status ) == HttpStatus.BAD_REQUEST
            responseError1.message == "Can not vote because Lunch time is gone at (2015-12-26 16:00) in (Dublis Restoranas). Now is (2015-12-26 16:00).";
            HttpStatus.valueOf(responseUnVote.status) == HttpStatus.OK
            responseUnVote.responseData==0

        cleanup:
            voteService.setClock( Clock.systemUTC() );

    }

    void "try vote for unpublished LUNCH_MENU_D26_3 by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_3.idLunchMenu}/vote";

            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }

        then:
            clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.NOT_FOUND
            responseError1.message == "LunchMenu=4 not found.";

        cleanup:
            voteService.setClock( Clock.systemUTC() );

    }

    void "try vote for unreal LunchMenu by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String urlVote1 =  "/menus/7777/vote";
            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }
        then:
            clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.NOT_FOUND
            responseError1.message == "LunchMenu=7777 not found.";

        cleanup:
            voteService.setClock( Clock.systemUTC() );
    }


    void "try vote for LUNCH_MENU_D26_1 without credentials!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic  USER_1.email, "bad"
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";
            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }

        then:
            clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.UNAUTHORIZED
            responseError1.message == "Bad credentials";
        cleanup:
            voteService.setClock( Clock.systemUTC() );

    }

    void "try vote for LUNCH_MENU_D26_1 with bad password!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic "", ""
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/vote";
            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }

        then:
            clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.UNAUTHORIZED
            responseError1.message == "Bad credentials";
            cleanup:
            voteService.setClock( Clock.systemUTC() );

    }


    void "vote for LUNCH_MENU_D26_2, LUNCH_MENU_D27_1 by USER_1, USER_2, ADMIN and unVote!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant   instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_2.idLunchMenu}/vote";
            String urlVote2 =  "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/vote";
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            def response1 =  lunchMenuRest.post ( path: urlVote1  )
            def response2 =  lunchMenuRest.post ( path: urlVote2  )
            lunchMenuRest.auth.basic USER_2.email, USER_2.password
            def response3 =  lunchMenuRest.post ( path: urlVote1  )
            def response4 =  lunchMenuRest.post ( path: urlVote2  )
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            def response5 =  lunchMenuRest.post ( path: urlVote1  )
            def response6 =  lunchMenuRest.post ( path: urlVote2  )

            String urlUnVote1 =  "/menus/${LUNCH_MENU_D26_2.idLunchMenu}/unvote";
            String urlUnVote2 =  "/menus/${LUNCH_MENU_D27_1.idLunchMenu}/unvote";
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            def response7 =  lunchMenuRest.post ( path: urlUnVote1  )
            def response8 =  lunchMenuRest.post ( path: urlUnVote2 )
            lunchMenuRest.auth.basic USER_2.email, USER_2.password
            def response9 =  lunchMenuRest.post ( path: urlUnVote1  )
            def response10 =  lunchMenuRest.post ( path: urlUnVote2 )
            lunchMenuRest.auth.basic ADMIN.email, ADMIN.password
            def response11 =  lunchMenuRest.post ( path: urlUnVote1  )
            def response12 =  lunchMenuRest.post ( path: urlUnVote2 )
        then:
            12*clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(response1.status) == HttpStatus.OK
            response1.responseData==1
            HttpStatus.valueOf(response2.status) == HttpStatus.OK
            response2.responseData==1
            HttpStatus.valueOf(response3.status) == HttpStatus.OK
            response3.responseData==2
            HttpStatus.valueOf(response4.status) == HttpStatus.OK
            response4.responseData==2
            HttpStatus.valueOf(response5.status) == HttpStatus.OK
            response5.responseData==3
            HttpStatus.valueOf(response6.status) == HttpStatus.OK
            response6.responseData==3

            HttpStatus.valueOf(response7.status) == HttpStatus.OK
            response7.responseData==2
            HttpStatus.valueOf(response8.status) == HttpStatus.OK
            response8.responseData==2
            HttpStatus.valueOf(response9.status) == HttpStatus.OK
            response9.responseData==1
            HttpStatus.valueOf(response10.status) == HttpStatus.OK
            response10.responseData==1
            HttpStatus.valueOf(response11.status) == HttpStatus.OK
            response11.responseData==0
            HttpStatus.valueOf(response12.status) == HttpStatus.OK
            response12.responseData==0
        cleanup:
            voteService.setClock( Clock.systemUTC() );
    }

    void "try unVote without voting on menu LUNCH_MENU_D26_1 by USER_1!"() {
        setup:
            Clock clock = Mock(Clock);
            VoteService voteService = context.getBean(VoteServiceImpl.class);
            voteService.setClock( clock );
            LocalDateTime date15_12_26_10_59 = LocalDateTime.parse("2015-12-26T10:59:59", DateTimeFormatter.ISO_DATE_TIME);
            Instant instant15_12_26_10_59 = date15_12_26_10_59.atZone(ZoneId.systemDefault()).toInstant();
        when:
            lunchMenuRest.auth.basic USER_1.email, USER_1.password
            String urlVote1 =  "/menus/${LUNCH_MENU_D26_1.idLunchMenu}/unvote";
            def responseOfPost = null
            Map responseError1 = null;
            lunchMenuRest.request( Method.POST) { request ->
                uri.path = urlVote1;
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseError1= data
                }
            }

        then:
            clock.instant() >> { return instant15_12_26_10_59; }
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseError1.message == "No vote in menu (1) found for unVoting by current user.";

        cleanup:
            voteService.setClock( Clock.systemUTC() );
    }

}
