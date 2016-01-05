# VoteOfLunch
1)  Design and implement a JSON API using Hibernate/Spring/SpringMVC without frontend.

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

2) Additional Requirements and Conceptual model of the system :
	Main condition of original task was  'don't ask more questions about task'. 
	So I tried my best to formulate reasonable requirements for such voting system for lunch menu.

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

	Let me explain my position.  On the one hand I wanted to show you what such good programmer I am.
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
	You can see REST API documentation inside 'generated-docs'
	To start application run inside 'VoteOfLunch '  command  : mvn spring-boot:run 
	To test application run inside ' VoteOfLunch '  command  : mvn test
 
	