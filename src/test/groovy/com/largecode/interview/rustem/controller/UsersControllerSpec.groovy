package com.largecode.interview.rustem.controller

import com.largecode.interview.rustem.Application
import com.largecode.interview.rustem.domain.Role
import groovyx.net.http.Method
import org.apache.http.entity.ContentType
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.*
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise


import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import groovyx.net.http.RESTClient
import static TestSpecificationUtils.*

/**
 * Created by r.zhunusov on 22.12.2015.
 */
@Stepwise
class UsersControllerSpec extends Specification{

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
//    static final Map USER_2 = [ idUser:3, password:"123456", passwordRepeated: "123456", email : "user2@gmail.com" , role: Role.REGULAR]

    static String URL_VOTE_OF_LUNCH = "http://localhost:8080/";
    @Shared
    def userService = new RESTClient( URL_VOTE_OF_LUNCH , ContentType.APPLICATION_JSON)

    void "get all initial Users by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def response =  userService.get (   path:"users" )
            Map pageOfUsers =  response.data
            List allUserEmails =  pageOfUsers.content.collect {it.email}.sort()
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            allUserEmails == [ADMIN, USER_1, USER_2 ].collect {it.email}.sort()  ;
    }

    void "try to get all initial Users by regular User1!"() {
        when:
            userService.auth.basic  USER_1.email, USER_1.password
            def responseOfGet = null
            userService.request( Method.GET) { request ->
                uri.path = '/users'
                response.failure = { resp ->
                    responseOfGet= resp;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.FORBIDDEN
    }

    void "try to get all initial Users with a bad credentials!"() {
        when:
            userService.auth.basic USER_1.email, "bad"
            def responseOfGet = null
            userService.request( Method.GET) { request ->
                uri.path = '/users'
                response.failure = { resp ->
                    responseOfGet= resp;
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
    }


    void "get USER_1 by ADMIN!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String url = "users/${USER_1.idUser}";
            def response =  userService.get ( path:url )
            def userFromService =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            userFromService.email == USER_1.email
            userFromService.idUser == USER_1.idUser
            userFromService.role == "REGULAR"
    }

    void "get ADMIN by ADMIN!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String url = "users/${ADMIN.idUser}";
            def response =  userService.get ( path:url )
            def userFromService =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            userFromService.email == ADMIN.email
            userFromService.idUser == ADMIN.idUser
            userFromService.role == "ADMIN"
    }

    void "try to get USER_2 by USER_1 !"() {
        when:
            userService.auth.basic  USER_1.email, USER_1.password
            String url = "users/${ADMIN.idUser}";
            def responseOfGet = null
            Map responseData = null;
            userService.request( Method.GET) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfGet= resp;
                    responseData= data
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.FORBIDDEN
            responseData.exception.contains("AccessDeniedException")
            responseData.message == "Access is denied"
    }

    void "try to get unreal user by ADMIN!"() {
        when:
        userService.auth.basic  ADMIN.email, ADMIN.password
            String url = "users/10000";
            def responseOfGet = null
            Map responseData = null;
            userService.request( Method.GET) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfGet= resp;
                    responseData= data
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionUserNotFound")
            responseData.message == "User=10000 not found"
    }


    void "try to get ADMIN by without credentials !"() {
        when:
            userService.auth.basic  "", ""
            String url = "users/${ADMIN.idUser}";
            def responseOfGet = null
            Map responseData = null;
            userService.request( Method.GET) { request ->
                uri.path = url;
                response.failure = { resp , data ->
                    responseOfGet= resp;
                    responseData= data
                }
            }
        then:
            responseOfGet != null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
            responseData.message == "Bad credentials"
    }

    void "create a first new User by ADMIN and check authorization!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            String firstUserEmail= "r1@yandex.ru"
            String firstUserPassword= "NEW_PASSWORD"

            def userDto = [email : firstUserEmail, password : firstUserPassword,  passwordRepeated: firstUserPassword, role : "REGULAR"]
            def response =  userService.post ( path:"users" ,
                    body: userDto )
            def userFromService =  response.data
            userService.auth.basic firstUserEmail, firstUserPassword
            String url = "users/${userFromService.idUser}";
            def responseGetByNewUser =  userService.get ( path:url )
            println responseGetByNewUser

        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            userFromService.email == firstUserEmail
            userFromService.idUser > 0
            userFromService.role == "REGULAR"
            HttpStatus.valueOf(responseGetByNewUser.status) == HttpStatus.OK
    }

    void "create a second new User by ADMIN and check authorization!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            String firstUserEmail= "r_second1@gmail.com"
            String firstUserPassword= "NEW_PASSWORD"

            def userDto = [email : firstUserEmail, password : firstUserPassword,  passwordRepeated: firstUserPassword, role : "ADMIN"]
            def response =  userService.post ( path:"users" ,
                    body: userDto )
            def userFromService =  response.data
            userService.auth.basic firstUserEmail, firstUserPassword
            String url = "users/${USER_2.idUser}";
            def responseGetByNewUser =  userService.get ( path:url )
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            userFromService.email == firstUserEmail
            userFromService.idUser > 0
            userFromService.role == "ADMIN"
            HttpStatus.valueOf(responseGetByNewUser.status) == HttpStatus.OK
    }

    void "create a third new User by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [email :"r_third@yandex.ru", password : "123456",  passwordRepeated: "123456"]
            def response =  userService.post ( path:"users" ,
                    body: userDto )
            def userFromService =  response.data
        then:
            HttpStatus.valueOf(response.status) == HttpStatus.OK
            userFromService.email == "r_third@yandex.ru"
            userFromService.idUser > 0
            userFromService.role == "REGULAR"
    }


    void "try to create new User with existed email by ADMIN!"() {
        when:
        userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [email :"r1@yandex.ru", password : "123456",  passwordRepeated: "123456", role : "REGULAR"]
            def responseOfPost = null;
            def responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users'
                body = userDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }

        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionOtherUserWithSameEmail");
            responseData.message.contains("r1@yandex.ru");
    }

    void "try to create new User with bad role name by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [email :"r_10@yandex.ru", password : "123456",  passwordRepeated: "123456", role : "REGULAR_BAD"]
            def responseOfPost = null;
            def responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users'
                body = userDto
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
            responseData.message.contains("REGULAR_BAD");
    }

    void "try to create new User with idUser by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [idUser:10, email :"r_12@yandex.ru", password : "123456",  passwordRepeated: "123456"]
            def responseOfPost = null;
            def responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users'
                body = userDto
                response.failure = { resp , data ->
                    responseOfPost= resp;
                    responseData = data

                }
            }
        then:
            responseOfPost != null
            responseData != null
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionIdUserMustBeEmpty");
            responseData.message == "IdUser(10) must be equal to 0 when creating new User."
    }


    void "try to create new User with bad password confirmation by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [email :"r_1@yandex.ru", password : "123456",  passwordRepeated: "654321", role : "REGULAR"]
            def responseOfPost = null;
            Map responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users'
                body = userDto
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
            responseData.errors.size()==1;
            responseData.errors[0].defaultMessage == "Passwords do not match.";

    }

    void "try to create new User with small length of password by ADMIN!"() {
        when:
            userService.auth.basic  ADMIN.email, ADMIN.password
            def userDto = [email :"r_1@yandex.ru", password : "1",  passwordRepeated: "1", role : "REGULAR"]
            def responseOfPost = null;
            Map responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users'
                body = userDto
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
            responseData.errors.size()==1;
            responseData.errors[0].defaultMessage == "Length of password must be between 6 and 40 chars.";

    }

    void "try to create new User  by regular user!"() {
        when:
            userService.auth.basic  USER_1.email, USER_1.password
            def userDto = [email :"r_2@yandex.ru", password : "123456",  passwordRepeated: "123456", role : "REGULAR"]
            def responseOfPost = null
            Map responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users';
                body = userDto;
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

    void "try to create new User without credentials!"() {
        when:
            userService.auth.basic ( "", "" )
            def userDto = [email :"r_3@yandex.ru", password : "123456",  passwordRepeated: "123456", role : "REGULAR"]
            def responseOfPost = null
            Map responseData = null;
            userService.request( Method.POST) { request ->
                uri.path = '/users';
                body = userDto;
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

    void "update USER_1 with new role and no password by ADMIN and check authorization!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String changedEmail = "new_mail@gmail.com";
            String urlToUser1 = "users/${USER_1.idUser}";
            String urlToUser2 = "users/${USER_2.idUser}";
            def userDto = [idUser: USER_1.idUser , email : changedEmail, password : "" ,  passwordRepeated: "", role : Role.ADMIN]
            def responsePut1 =  userService.put ( path:urlToUser1 ,  body: userDto ) // update with new values

            userService.auth.basic changedEmail, USER_1.password // check authorization with new email and old password
            def responseGet1 =  userService.get ( path:urlToUser1 )
            def userFromService1 =  responseGet1.data
            def responseGet2 =  userService.get ( path: "users/${USER_2.idUser}" )
            def userFromService2 =  responseGet2.data

            def responsePut2 =  userService.put ( path: urlToUser1 , body: USER_1 ) // update to original values
            userService.auth.basic USER_1.email, USER_1.password // check authorization with old email and old password
            def responseOfGet3 = null
            userService.request( Method.GET) { request ->
                uri.path = urlToUser2;  // USER_1 with regular role can not more get information about USER_2
                response.failure = { resp , data->
                    responseOfGet3= resp;
                }
            }

        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            userFromService1.email == changedEmail
            userFromService1.idUser == USER_1.idUser
            userFromService1.role == Role.ADMIN.toString()
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.OK
            userFromService2.email == USER_2.email
            userFromService2.idUser == USER_2.idUser
            userFromService2.role == USER_2.role.toString()
            HttpStatus.valueOf(responsePut2.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet2.status) == HttpStatus.OK
            responseOfGet3 !=  null
            HttpStatus.valueOf(responseOfGet3.status) == HttpStatus.FORBIDDEN
    }

    void "update USER_1 with new password and no role by ADMIN and check authorization!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String changedPassword= "new_password_2"
            String url = "users/${USER_1.idUser}";
            def userDto = [idUser: USER_1.idUser , email : USER_1.email , password : changedPassword,  passwordRepeated: changedPassword]
            def responsePut =  userService.put ( path:url ,  body: userDto ) // update with new values

            userService.auth.basic USER_1.email, changedPassword // check authorization with new password
            def responseGet =  userService.get ( path:url )
            def userFromService =  responseGet.data

            userService.auth.basic ADMIN.email, ADMIN.password
            def responsePut2 =  userService.put ( path:url , body: USER_1 ) // update to original values
        then:
            HttpStatus.valueOf(responsePut.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet.status) == HttpStatus.OK
            userFromService.email == USER_1.email
            userFromService.idUser == USER_1.idUser
            userFromService.role == Role.REGULAR.toString() // default role is REGULAR
            HttpStatus.valueOf(responsePut2.status) == HttpStatus.NO_CONTENT
    }

    void "update USER_1 by USER_1. Regular user can change only email and password but NOT a role!"() {
        when:
            userService.auth.basic USER_1.email, USER_1.password
            String changedEmail = "new_mail_3@gmail.com";
            String changedPassword= "new_password_3"
            String url = "users/${USER_1.idUser}";
            def userDto = [idUser: USER_1.idUser , email : changedEmail , password : changedPassword,
                           passwordRepeated: changedPassword, role : Role.ADMIN]
            def responsePut =  userService.put ( path:url ,  body: userDto ) // update with new values

            userService.auth.basic changedEmail, changedPassword // check authorization with new credentials
            def responseGet =  userService.get ( path:url )
            def userFromService =  responseGet.data

            def responsePut2 =  userService.put ( path:url , body: USER_1 ) // update to original values
        then:
            HttpStatus.valueOf(responsePut.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet.status) == HttpStatus.OK
            userFromService.email == changedEmail
            userFromService.idUser == USER_1.idUser
            userFromService.role == Role.REGULAR.toString() // Regular User can not change his role.
            HttpStatus.valueOf(responsePut2.status) == HttpStatus.NO_CONTENT
    }

    void "update USER_1 by USER_1. User can be updated with his already existed email but not with email of other user!"() {
        when:
            userService.auth.basic USER_1.email, USER_1.password
            String url = "users/${USER_1.idUser}";
            def userDto = [idUser: USER_1.idUser , email : USER_1.email , password : "",
                           passwordRepeated: "" ]
            def responsePut1 =  userService.put ( path:url ,  body: userDto )
            def responseGet1 =  userService.get ( path:url )
            def userFromService1 =  responseGet1.data
            //
            def responseOfPut = null;
            def responseData = null;
            def userDto2 = [idUser: USER_1.idUser , email : USER_2.email , password : "",
                       passwordRepeated: "" ]
            userService.request( Method.PUT) { request ->
                uri.path = url
                body = userDto2
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            HttpStatus.valueOf(responsePut1.status) == HttpStatus.NO_CONTENT
            HttpStatus.valueOf(responseGet1.status) == HttpStatus.OK
            userFromService1.email == USER_1.email
            userFromService1.idUser == USER_1.idUser
            userFromService1.role == Role.REGULAR.toString()
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionOtherUserWithSameEmail");
            responseData.message.contains(USER_2.email);
    }


    void "try to update USER_1 with existed email by ADMIN!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String url = "users/${USER_1.idUser}";
            def userDto = [idUser: USER_1.idUser , email : USER_2.email, password : "123456",  passwordRepeated: "123456", role : "ADMIN"]
            def responseOfPut = null;
            def responseData = null;
            userService.request( Method.PUT) { request ->
                uri.path = url
                body = userDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionOtherUserWithSameEmail");
            responseData.message.contains(USER_2.email);
    }

    void "try to update unreal user by ADMIN!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String fakeEmail = "fake_box@gmail.com";
            String fakePassword= "fake_password"
            String url = "users/15000";
            def userDto = [idUser: 15000 , email : fakeEmail, password : fakePassword,  passwordRepeated: fakePassword, role : "ADMIN"]
            def responseOfPut = null;
            def responseData = null;
            userService.request( Method.PUT) { request ->
                uri.path = url
                body = userDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionUserNotFound")
            responseData.message == "User=15000 not found for updating."

    }

    void "try to update USER_1 by ADMIN with different ID between URL and JSON!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String url =   "users/${USER_1.idUser}";
            def userDto = [idUser: USER_2.idUser , email : USER_1.email, password : "123456",  passwordRepeated: "123456", role : "ADMIN"]
            def responseOfPut = null;
            def responseData = null;
            userService.request( Method.PUT) { request ->
                uri.path = url
                body = userDto
                response.failure = { resp , data ->
                    responseOfPut= resp;
                    responseData = data
                }
            }
            println responseData
        then:
            responseOfPut != null
            responseData != null
            HttpStatus.valueOf(responseOfPut.status) == HttpStatus.BAD_REQUEST
            responseData.exception.contains("ExceptionDifferentIdBetweenUrlAndBody");
            responseData.message == "'idUser' in URL ($USER_1.idUser) must be as same as in request body ($USER_2.idUser)"
    }
    //"exception" -> "com.largecode.interview.rustem.controller.UsersController$ExceptionDifferentIdBetweenUrlAndBody"

    void "try to update USER_2 by USER_1!"() {
        when:
            userService.auth.basic USER_1.email, USER_1.password
            String fakeEmail = "fake_box@gmail.com";
            String fakePassword= "fake_password"
            String url = "users/${USER_2.idUser}";
            def userDto = [idUser: USER_2.idUser , email : fakeEmail , password : fakePassword,
                           passwordRepeated: fakePassword, role : Role.ADMIN]
            def responseOfPut = null;
            def responseData = null;
            userService.request( Method.PUT) { request ->
                uri.path = url
                body = userDto
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


    void "delete user by ADMIN!"() {
        when:
            userService.auth.basic ADMIN.email, ADMIN.password
            String fakeEmail = "fake_box@gmail.com";
            String fakePassword= "fake_password"
            def userDto = [email : fakeEmail, password : fakePassword,  passwordRepeated: fakePassword]
            def responseOfPost =  userService.post ( path:"users" ,
                    body: userDto )
            def userNewId =  responseOfPost.data.idUser

            String urlToUser2 = "users/${userNewId}";
            def responseDelete =  userService.delete ( path:urlToUser2 )
            def responseOfGet = null
            def responseData = null
            userService.request( Method.GET) { request ->
                uri.path = urlToUser2;
                response.failure = { resp , data->
                    responseOfGet= resp;
                    responseData= data
                }
            }

        then:
            HttpStatus.valueOf(responseOfPost.status) == HttpStatus.OK
            HttpStatus.valueOf(responseDelete.status) == HttpStatus.NO_CONTENT
            responseOfGet !=  null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.NOT_FOUND
            responseData.message == "User=${userNewId} not found".toString()

    }

    void "try to delete USER_2 by USER_1 !"() {
        when:
            userService.auth.basic USER_1.email, USER_1.password
            String urlToUser2 = "users/${USER_2.idUser}";
            def responseOfDelete = null
            def responseData = null
            userService.request( Method.DELETE) { request ->
                uri.path = urlToUser2;
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
            userService.auth.basic ADMIN.email, ADMIN.password
            String urlToUser2 = "users/20000";
            def responseOfDelete = null
            def responseData = null
            userService.request( Method.DELETE) { request ->
                uri.path = urlToUser2;
                response.failure = { resp , data->
                    responseOfDelete= resp;
                    responseData= data
                }
            }
        then:
            responseOfDelete !=  null
            HttpStatus.valueOf(responseOfDelete.status) == HttpStatus.NOT_FOUND
            responseData.exception.contains("ExceptionUserNotFound")
            responseData.message == "User=20000 not found for deleting."
    }

    void "delete USER_2 by USER_2 !"() {
        when:
            userService.auth.basic USER_2.email, USER_2.password
            String urlToUser2 = "users/${USER_2.idUser}";
            def responseDelete =  userService.delete ( path:urlToUser2 )

            def responseOfGet = null
            def responseData = null
            userService.request( Method.GET) { request ->
                uri.path = urlToUser2;
                response.failure = { resp , data->
                    responseOfGet= resp;
                    responseData= data
                }
            }

        then:
            HttpStatus.valueOf(responseDelete.status) == HttpStatus.NO_CONTENT
            responseOfGet !=  null
            responseData !=  null
            HttpStatus.valueOf(responseOfGet.status) == HttpStatus.UNAUTHORIZED
            responseData.message == "Bad credentials"
    }


}
