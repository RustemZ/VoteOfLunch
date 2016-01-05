package com.largecode.interview.rustem.controller

import com.largecode.interview.rustem.Application
import com.largecode.interview.rustem.domain.Role
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

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import static TestSpecificationUtils.*


/**
 * Created by r.zhunusov on 22.12.2015.
 */
@Stepwise
class RestaurantControllerSpec extends Specification{

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
        context = future.get(60, TimeUnit.SECONDS)
    }

//    static final Map ADMIN = [ idUser:1, password:"123456", passwordRepeated: "123456", email : "admin@gmail.com" , role: Role.ADMIN ]
//    static final Map USER_1 = [ idUser:2, password:"123456", passwordRepeated: "123456", email : "user1@gmail.com" , role: Role.REGULAR]
//    static final Map RESTAURANT_1 = [idRestaurant:1, address:"Via Sopeno Gatve, 10, Vilnius 03211", idByAuthorities:"00000001", lunchEndHour:16, phone:"+370 674 41922", title:"Dublis Restoranas"]
//    static final Map RESTAURANT_2 = [idRestaurant:2, address:"Traku G. 4, Vilnius 01132", idByAuthorities:"00000002", lunchEndHour:15, phone:"+370 5 212 6874", title:"Alaus Biblioteka"]
//    static final Map RESTAURANT_3 = [idRestaurant:3, address:"Verkiu G. 29 | Ogmios Miestas, Seimos Aikste 6, Vilnius 09108", idByAuthorities:"00000003", lunchEndHour:17, phone:"+370 645 52250", title:"Jurgis ir Drakonas"]

    static String URL_VOTE_OF_LUNCH = "http://localhost:8080/";
    @Shared
    def restaurantService = new RESTClient( URL_VOTE_OF_LUNCH , ContentType.APPLICATION_JSON)

    void "get all initial Restaurants by ADMIN!"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            def response =  restaurantService.get ( path:"restaurants" )
            Map pageOfRestaurants =  response.data
            List allRestaurantsLogicalIds =  pageOfRestaurants.content.collect {it.idByAuthorities}.sort()
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            allRestaurantsLogicalIds == [RESTAURANT_1, RESTAURANT_2, RESTAURANT_3 ].collect {it.idByAuthorities}.sort()  ;
    }

    void "get all initial Restaurants by regular USER_1!"() {
        when:
            restaurantService.auth.basic  USER_1.email, USER_1.password
            def response =  restaurantService.get ( path:"restaurants" )
            Map pageOfRestaurants =  response.data
            List allRestaurantsLogicalIds =  pageOfRestaurants.content.collect {it.idByAuthorities}.sort()
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            allRestaurantsLogicalIds == [RESTAURANT_1, RESTAURANT_2, RESTAURANT_3 ].collect {it.idByAuthorities}.sort()  ;
    }

    void "try to get all initial Restaurants with a bad credentials!"() {
        when:
            restaurantService.auth.basic USER_1.email, "bad_password"
            def responseOfGet = null
            Map responseData = null;
            restaurantService.request( Method.GET) { request ->
                uri.path = '/restaurants'
                response.failure = { resp, data ->
                    responseOfGet= resp;
                    responseData= data;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
    }

  //  static final Map RESTAURANT_3 = [idRestaurant:3, idByAuthorities:"00000003", lunchEndHour:17, phone:"+370 645 52250", title:"Jurgis ir Drakonas"]

    void "get RESTAURANT_1 by ADMIN!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String url = "restaurants/${RESTAURANT_1.idRestaurant}";
            def response =  restaurantService.get ( path:url )
            def restaurantFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            restaurantFromRest.idRestaurant == RESTAURANT_1.idRestaurant
            restaurantFromRest.idByAuthorities == RESTAURANT_1.idByAuthorities
            restaurantFromRest.address == RESTAURANT_1.address
            restaurantFromRest.lunchEndHour == RESTAURANT_1.lunchEndHour
            restaurantFromRest.phone == RESTAURANT_1.phone
            restaurantFromRest.title == RESTAURANT_1.title
    }

    void "get RESTAURANT_2 by USER_1!"() {
        when:
            restaurantService.auth.basic USER_1.email, USER_1.password
            String url = "restaurants/${RESTAURANT_2.idRestaurant}";
            def response =  restaurantService.get ( path:url )
            def restaurantFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            restaurantFromRest.idRestaurant == RESTAURANT_2.idRestaurant
            restaurantFromRest.idByAuthorities == RESTAURANT_2.idByAuthorities
            restaurantFromRest.address == RESTAURANT_2.address
            restaurantFromRest.lunchEndHour == RESTAURANT_2.lunchEndHour
            restaurantFromRest.phone == RESTAURANT_2.phone
            restaurantFromRest.title == RESTAURANT_2.title
    }

    void "try to get RESTAURANT_2 without credentials !"() {
        when:
            restaurantService.auth.basic "", ""
            def responseOfGet = null
            Map responseData = null;
            restaurantService.request( Method.GET) { request ->
                uri.path = "/restaurants/${RESTAURANT_2.idRestaurant}"
                response.failure = { resp, data ->
                    responseOfGet= resp;
                    responseData= data;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
    }

    void "try to get unreal Restaurant by ADMIN!"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            String url = "restaurants/10000";
            def responseOfGet = null
            Map responseData = null;
            restaurantService.request( Method.GET) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfGet= resp;
                    responseData= data
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionRestaurantNotFound")
            responseData.message == "Restaurant=10000 not found."
    }



    void "create new Restaurant by ADMIN!"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            Map restaurantNewDto = [idByAuthorities:"00000004", address: "Naugarduko str. 36, Vilnius",
                                    lunchEndHour:16, phone: "+370 609 99002", title:"Senoji Trobele"]
            def response =  restaurantService.post ( path:"restaurants" ,
                    body: restaurantNewDto )
            def restaurantFromRest =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            restaurantFromRest.idRestaurant >0
            restaurantFromRest.idByAuthorities == restaurantNewDto.idByAuthorities
            restaurantFromRest.address == restaurantNewDto.address
            restaurantFromRest.lunchEndHour == restaurantNewDto.lunchEndHour
            restaurantFromRest.phone == restaurantNewDto.phone
            restaurantFromRest.title == restaurantNewDto.title
    }

    void "try to create new Restaurant by regular USER_1!"() {
        when:
            restaurantService.auth.basic  USER_1.email, USER_1.password
            Map restaurantDto = [idByAuthorities:"00000005", address: "Stikliu g. 18 | Stikliu g. 18, Vilnius 01131",
                                lunchEndHour:16, phone: "67772091", title:"Bistro 18"]
            def responseOfPost = null
            Map responseData = null;
            restaurantService.request( Method.POST) { request ->
                uri.path = '/restaurants';
                body = restaurantDto;
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


    void "try to create new Restaurant with existed idByAuthorities by ADMIN!"() {
        when:
        restaurantService.auth.basic  ADMIN.email, ADMIN.password
            Map restaurantDto = [idByAuthorities: RESTAURANT_1.idByAuthorities, address: "Stikliu g. 18 | Stikliu g. 18, Vilnius 01131",
                             lunchEndHour:16, phone: "67772091", title:"Bistro 18"]
            def responseOfPost = null;
            def responseData = null;
            restaurantService.request( Method.POST) { request ->
                uri.path = '/restaurants'
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionOtherRestaurantWithSameIdByAuthorities");
            responseData.message.contains(RESTAURANT_1.idByAuthorities);
    }

    void "try to create new Restaurant with filled idRestaurant by ADMIN!"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            Map restaurantDto = [idRestaurant:77,  idByAuthorities: "00000005", address: "Stikliu g. 18 | Stikliu g. 18, Vilnius 01131",
                             lunchEndHour:16, phone: "67772091", title:"Bistro 18"]
            def responseOfPost = null;
            def responseData = null;
            restaurantService.request( Method.POST) { request ->
                uri.path = '/restaurants'
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionCannotCreateRestaurantWithId");
            responseData.message == "IdRestaurant(77) must be equal to 0 when creating new Restaurant."

    }

    void "try to create new Restaurant by ADMIN and check validation !"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            Map restaurantDto = [ lunchEndHour:18 ]
            def responseOfPost = null;
            Map responseData = null;
            restaurantService.request( Method.POST) { request ->
                uri.path = '/restaurants'
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
            def fieldsWithWrongValidation =  ["address", "lunchEndHour", "idByAuthorities", "title"]
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==4
            responseData.errors.collect{it.field}.sort() == fieldsWithWrongValidation.sort();

    }


    void "try to create new Restaurant without credentials!"() {
        when:
            restaurantService.auth.basic ( "", "" )
            Map restaurantDto = [ idByAuthorities: "00000006", address: "Stikliu g. 18 | Stikliu g. 18, Vilnius 01131",
                             lunchEndHour:16, phone: "67772091", title:"Bistro 18"]
            def responseOfPost = null
            Map responseData = null;
            restaurantService.request( Method.POST) { request ->
                uri.path = '/restaurants';
                body = restaurantDto;
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

    void "update RESTAURANT_1 by ADMIN !"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String changedAddress = "Via Sopeno Gatve, 12";
            int changedLunchEndTime =15;
            String changedIdByAuthorities="99999001"
            String changedPhone= "+370 674 99999"
            String changedTitle= "Dublis Restoranas New"
            String urlToRestaurant1 = "restaurants/${RESTAURANT_1.idRestaurant}";
            def restaurantDto = [idRestaurant:1, address: changedAddress,
                  idByAuthorities: changedIdByAuthorities, lunchEndHour:changedLunchEndTime ,
                  phone:changedPhone, title: changedTitle]

            def responsePut1 =  restaurantService.put ( path:urlToRestaurant1 ,  body: restaurantDto ) // update with new values
            def responseGet1 =  restaurantService.get ( path:urlToRestaurant1 )
            def restaurantFromRest1 =  responseGet1.data

        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            restaurantFromRest1.idRestaurant == RESTAURANT_1.idRestaurant
            restaurantFromRest1.address ==  changedAddress
            restaurantFromRest1.idByAuthorities == changedIdByAuthorities
            restaurantFromRest1.lunchEndHour ==changedLunchEndTime
            restaurantFromRest1.phone == changedPhone
            restaurantFromRest1.title == changedTitle
    }

     void "try to update unreal Restaurant by ADMIN!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String url = "restaurants/15000";
            Map restaurantDto = [ idRestaurant: 15000,  idByAuthorities: "00000007", address: "Stikliu g. 18 | Stikliu g. 18, Vilnius 01131",
                              lunchEndHour:16, phone: "67772091", title:"Bistro 18"]
            def responseOfPut = null;
            def responseData = null;
            restaurantService.request( Method.PUT) { request ->
                uri.path = url
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionRestaurantNotFound")
            responseData.message == "Restaurant=15000 not found for updating."

    }

    void "try to update RESTAURANT_1 by ADMIN with different ID between URL and JSON!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String url = "restaurants/${RESTAURANT_1.idRestaurant}";
            Map restaurantDto = [ idRestaurant: RESTAURANT_2.idRestaurant ,  idByAuthorities: RESTAURANT_1.idByAuthorities,
                                  address: RESTAURANT_1.address, lunchEndHour: RESTAURANT_1.lunchEndHour,
                                  phone: RESTAURANT_1.phone, title: RESTAURANT_1.title]
            def responseOfPut = null;
            def responseData = null;
            restaurantService.request( Method.PUT) { request ->
                uri.path = url
                body = restaurantDto
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
            responseData.message == "'idRestaurant' in URL ($RESTAURANT_1.idRestaurant) must be as same as in request body ($RESTAURANT_2.idRestaurant)."
    }

    void "try to update RESTAURANT_1 by USER_1!"() {
        when:
            restaurantService.auth.basic USER_1.email, USER_1.password
            String url = "restaurants/${RESTAURANT_1.idRestaurant}";
            def responseOfPut = null;
            def responseData = null;
            restaurantService.request( Method.PUT) { request ->
                uri.path = url
                body = RESTAURANT_1
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

    void "try to update RESTAURANT_1 with idByAuthorities of RESTAURANT_2 !"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String url = "restaurants/${RESTAURANT_1.idRestaurant}";
            Map restaurantDto = [ idRestaurant: RESTAURANT_1.idRestaurant ,  idByAuthorities: RESTAURANT_2.idByAuthorities,
                              address: RESTAURANT_1.address, lunchEndHour: RESTAURANT_1.lunchEndHour,
                              phone: RESTAURANT_1.phone, title: RESTAURANT_1.title]

            def responseOfPut = null;
            def responseData = null;
            restaurantService.request( Method.PUT) { request ->
                uri.path = url
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionOtherRestaurantWithSameIdByAuthorities");
            responseData.message.contains( RESTAURANT_2.idByAuthorities );
    }

    void "try to update RESTAURANT_2 by ADMIN and check fields validation !"() {
        when:
            restaurantService.auth.basic  ADMIN.email, ADMIN.password
            String url = "restaurants/${RESTAURANT_2.idRestaurant}";
            Map restaurantDto = [ idRestaurant: RESTAURANT_2.idRestaurant , lunchEndHour: 21 ]
            def responseOfPost = null;
            Map responseData = null;
            restaurantService.request( Method.PUT) { request ->
                uri.path = url
                body = restaurantDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data
                }
            }
            def fieldsWithWrongValidation =  ["address", "lunchEndHour", "idByAuthorities", "title"]
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("MethodArgumentNotValidException");
            responseData.containsKey( 'errors' )
            responseData.errors.size()==4
            responseData.errors.collect{it.field}.sort() == fieldsWithWrongValidation.sort();

    }

    void "create and delete Restaurant by ADMIN!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String titleOfNewRestaurant = "Restaurant Lokys"
            Map restaurantDto = [idByAuthorities:"00000007", address: "Stikliu str. 8/10, Vilnius LT-2001",
                                    lunchEndHour:15, phone: "+370 5 2629046", title: titleOfNewRestaurant]
            def responseOfPost =  restaurantService.post ( path:"restaurants" ,
                    body: restaurantDto )
            def restaurantNewId =  responseOfPost.data.idRestaurant

            String urlToRestaurant = "restaurants/${restaurantNewId}";
            def responseGet1 =  restaurantService.get ( path: urlToRestaurant )
            def restaurantFromGet1 =  responseGet1.data

            def responseDelete =  restaurantService.delete ( path:urlToRestaurant )

            def responseGet2 = null
            def responseData2 = null
            restaurantService.request( Method.GET) { request ->
                uri.path = urlToRestaurant;
                response.failure = { resp , data->
                    responseGet2 = resp;
                    responseData2 = data
                }
            }

        then:
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.OK
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            restaurantFromGet1.title == titleOfNewRestaurant

            HttpStatus.valueOf(responseDelete.status) == HttpStatus.NO_CONTENT

            responseGet2 !=  null
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.NOT_FOUND
            responseData2.message ==  "Restaurant=${restaurantNewId} not found.".toString()

    }

    void "try to delete RESTAURANT_2 by USER_1 !"() {
        when:
            restaurantService.auth.basic USER_1.email, USER_1.password
            String urlToRestaurant2 = "restaurants/${RESTAURANT_2.idRestaurant}";
            def responseOfDelete = null
            def responseData = null
            restaurantService.request( Method.DELETE) { request ->
                uri.path = urlToRestaurant2;
                response.failure = { resp , data->
                    responseOfDelete= resp;
                    responseData= data
                }
            }
        then:
            responseOfDelete !=  null
            HttpStatus.valueOf(responseOfDelete.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException")
            responseData.message == "Access is denied"

    }


    void "try to delete unreal user by ADMIN!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String urlToRestaurant2 = "restaurants/20000";
            def responseOfDelete = null
            def responseData = null
            restaurantService.request( Method.DELETE) { request ->
                uri.path = urlToRestaurant2;
                response.failure = { resp , data->
                    responseOfDelete= resp;
                    responseData= data
                }
            }
        then:
            responseOfDelete !=  null
            HttpStatus.valueOf(responseOfDelete.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionRestaurantNotFound")
            responseData.message == "Restaurant=20000 not found for deleting."
    }


    void "delete RESTAURANT_1 by ADMIN!"() {
        when:
            restaurantService.auth.basic ADMIN.email, ADMIN.password
            String urlToRestaurant = "restaurants/${RESTAURANT_1.idRestaurant}";

            def responseDelete =  restaurantService.delete ( path:urlToRestaurant )

            def responseGet2 = null
            def responseData2 = null
            restaurantService.request( Method.GET) { request ->
                uri.path = urlToRestaurant;
                response.failure = { resp , data->
                    responseGet2 = resp;
                    responseData2 = data
                }
            }

        then:

            HttpStatus.valueOf(responseDelete.status) == HttpStatus.NO_CONTENT

            responseGet2 !=  null
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.NOT_FOUND
            responseData2.message ==  "Restaurant=${RESTAURANT_1.idRestaurant} not found.".toString()

    }


}
