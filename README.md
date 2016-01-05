# VoteOfLunch
1)  Original Task:
	Design and implement a JSON API using Hibernate/Spring/SpringMVC without frontend.

	The task is:
	Build a voting system for deciding where to have lunch.

	2 types of users: admin and regular users
	Admin can input a restaurant and it's lunch menu of the day (2-5 items usually, just a dish name and price)
	Menu changes each day (admins do the updates)
	Users can vote on which restaurant they want to have lunch at
	Only one vote counted per user
	If user votes again the same day:
	If it is before 11:00 we asume that he changed his mind.
	If it is after 11:00 then it is too late, vote can't be changed
	Each restaurant provides new menu each day.
	As a result, provide a link to github repository. It should contain the code, README.md 
	with API documentation and couple curl commands to test it.

	P.S.: you can use a project seed you find where all technologies are already preconfigured.
	P.P.S.: Make sure everything works with latest version that is on github :)
	P.P.P.S.: Asume that your API will used by a frontend developer to build frontend on top of that.

2)  Additional Requirements and Conceptual model of the system :
	Main condition of original task was 'don't ask more questions about task'. 
	So I tried my best to formulate reasonable requirements for such voting system of lunch menu.

	Here they are my guesses.
	- Each restaurant has hour when a lunch time has ended  and the lunch menu is no longer valid for that day .
	- Users of the system can not vote for the lunch menu of the restaurant where lunch has been ended that day.
	- Users can vote for the lunch menu which created for  future days.
	- One restaurant can publish only one menu per day.
	- The administrator can change any of dishes and their prices in the lunch menu till a time 
		when the menu would be published on the site.
	- The regular user may  vote only for published menu.
	- It's prohibited to change any of dishes and their prices in the lunch menu after the menu was published on the site. 
	  It would be unfair to user who vote on them.
	- But If the menu is incorrect and the administrator would know about it after it's publication. 
	  He can cancel the incorrect menu after that create and publish new one.
	- There are a little probability that administrator can cancel the menu by mistake. 
	  So system should give him the opportunity to republish the menu again.
	- If user make a vote on a lunch menu then the system must remove his previous vote on other menu on this day. 
	- User can cancel his vote on the menu without voting on another menu.
	- I assume that each restaurant has its own unique logical ID which is issued by the state authorities of his country.
	  At least in my county we have such situation.
	  
	I suppose that all this Requirements and business rules allow you to get general understanding 
	 of my point of view on the system, my conceptual model  which I wanted to implement.

3)  Architecture. 

	Let me explain my position. On the one hand I wanted to show you 'what are such a good programmer I am'.
	But on other the hand I tried to save your and my time to accomplish task quickly as possible 
	because it is no more than a task for test.
	So this is two main factors which had big influence on my choice of architecture 
	and technologies for my implementation of Your task.
	I used Spring Boot on maven as a foundation platform for the project.
	Because it was the fastest way to collect 'Hibernate / Spring / Spring MVC' together without too much hustle. 
	The project also used other Spring technologies such as Spring Data, Spring Security and etc.

	I used Spock as testing framework. 'With Spock, application testing is only logical'. 
	I wrote only integration tests because I think they are more important than unit tests. 
	So I choose integration tests over unit tests and I spent my time on them. 
	You should know than most of my time was not spent on creating the system but on testing a behavior of the system.

	And I generated documentation using SpringFox SwaggerUI.
	I used basic authentication as its most simple. But of course I know that It's not very safe.

4) Implementation.
	Please, see my code and other 95 integration test in my project 'vote-lunch' .
	Also I must say that I don't implement all things that I wanted to do. 
	For example It was a custom sorting, a localization, more advanced life cycle of lunch menu and etc. 
	Just didn't have enough time on it. 
	Let me say that I can resolve all this issues on next iteration. Of course if it would be .

5) Test it.
	You need JDK8 and Maven3.
	All test users ('admin@gmail.com', 'user1@gmail.com', 'user2@gmail.com') have same password : '123456'.
	You can see REST API documentation inside 'generated-docs'
	To start application run inside 'VoteOfLunch' command : 'mvn spring-boot:run' 
	To test application run inside ' VoteOfLunch' command : 'mvn test'

6) Some Curls for test:  
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: a54cf013-87a3-1c16-3ac5-2660cd6b9088" 'http://localhost:8080/users/' 
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic dXNlcjFAZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 95e0aa19-15f9-b5ca-5ed7-bc65ab7d118a" 'http://localhost:8080/users/2'
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 398bc092-ba90-beda-d0ff-bc133ba90c5c" -d '{ "email":"r4@ya.ru", "password":"123456@_", "passwordRepeated":"123456@_"}' 'http://localhost:8080/users/'
	curl -X PUT -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: b69b113b-bb97-e99f-0fed-e61c8c694a6c" -d '{ "email":"r4@ya.ru", "password":"123456@_", "passwordRepeated":"123456@_", "idUser":4}' 'http://localhost:8080/users/4'
	curl -X DELETE -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 420ce499-21e9-8702-9830-a9c4b4169428" -d '' 'http://localhost:8080/users/4'
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: b6c9b8cc-61f1-2e65-a858-72939b7a49f1" 'http://localhost:8080/restaurants/'
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic dXNlcjFAZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 78a8d567-fd73-941f-650c-dbe8097c37c8" 'http://localhost:8080/restaurants/2'
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 4ea792b3-108d-d2f2-d489-02e65d4d25e2" -d '{ "idByAuthorities":"00000004", "address": "Naugarduko str. 36, Vilnius", "lunchEndHour":16, "phone": "+370 609 99002", "title":"Senoji Trobele"}' 'http://localhost:8080/restaurants/'
	curl -X PUT -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: bbc60e5f-5a7b-86ce-5968-ffc623aca246" -d '{ "idByAuthorities":"00000004", "address": "Naugarduko str. 99, Vilnius", "lunchEndHour":16, "phone": "+370 609 99002", "title":"Senoji Trobele", "idRestaurant": 4}' 'http://localhost:8080/restaurants/4/'
	curl -X DELETE -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: baaf45f6-8710-7c1f-f47a-8a9173902dff" -d '' 'http://localhost:8080/restaurants/4/'
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 6de07b8c-7c62-6019-42f6-242471e451e9" 'http://localhost:8080/menus/'
	curl -X GET -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 641a2e63-1c14-955b-16d3-a9c1bd35f979" 'http://localhost:8080/menus/2/'
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 0bfea2e4-5944-2008-0bff-ea628568032b" -d '{"theRestaurantId": 2, "theDay": 1451152800000 ,                                    "theDishesDto": [ { "name":"dish #9" , "priceStr":"99.99"}, { "name":"dish #10" , "priceStr" : "100.100"} ]  }' 'http://localhost:8080/menus/'
	curl -X PUT -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 225924b6-93a7-25db-4eb9-73e85358ffb3" -d '{ "idLunchMenu": 5, "theRestaurantId": 2, "theDay": 1451152800000 ,"theDishesDto": [ { "name":"dish #9" , "priceStr":"99.99"}, { "name":"dish #10" , "priceStr" : "100.100"} ]  }' 'http://localhost:8080/menus/5/'
	curl -X PUT -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 665ed980-9129-d174-dfc7-cb218bbe3786" -d '' 'http://localhost:8080/menus/5/publish'
	curl -X PUT -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 2527ac63-2ec2-52c9-953b-5820c6ced8c2" -d '' 'http://localhost:8080/menus/5/cancel'
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: f8e1bb25-cc5e-b7e0-7a79-e8948949335a" -d '' 'http://localhost:8080/menus/5/vote'
	curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW5AZ21haWwuY29tOjEyMzQ1Ng==" -H "Cache-Control: no-cache" -H "Postman-Token: 2ce367d2-1495-b296-c61d-3f97d363f808" -d '' 'http://localhost:8080/menus/5/unvote'
	
	