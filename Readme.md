# Implementation for Money transfer between Accounts

## Problem Statement

Design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.
### Explicit requirements:
1. You can use Java or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like ( except Spring ), but don't forget about requirement #2 and keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.

### Implicit requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.

Please put your work on github or bitbucket.

########################-------------------------------------------------------#########################

## Solution Introduction:
- Implementing a microservice architecture.
- Stateless
- Exception Handling
- Input Validation 

## Tech Stack used
Detail out the comparison
1. Http server Framework : Should be able to run without installing any server. LightWeight, 
   supports Rest, run as jar, high performance, concurrency
     
    - Restlet : Lightweight.  Supports major web standards. Many extensions also available to integrate like jackson . Suitable for both server and client applications. 
      Restlet has got bigger and more matured community around it


    - RestEay  : A bit heavy framework as its environment is JBOSS oriented.

    - Jersey v2: A lightweight framework.  It is an implementation of the jax-rs standard, 
        and many other frameworks can natively understand the jax-rs annotations. Open source. 

    - Apache CXF : A bit heavy framework as it supports different protocols, supports 
      soap also. 

    - Dropwizard : A heavy framework. Lot of plumbing reqd even to build tiny service. Uses Jetty server


    References:
        https://cdelmas.github.io/2015/11/01/A-comparison-of-Microservices-Frameworks.html
        https://javarevisited.blogspot.com/2017/02/difference-between-jax-rs-restlet-jersey-apache-cfx-RESTEasy.html
    
2. Http Client:
   Apache http client  

2. Logging:
   log4j has been used for logging.
   
3. Testing:
    Unit Testing : JUNIT
    

4. DB :
    Requirement is to have in-memory DB


    - Relational: Provides ACID behavior. Vertically Scalable. More complex as have to maintain relationship. As 
      this use case requires ACID behaviour so proceeding with relational.
      H2 is the in-memory DB 

    - NoSql : Horizontally scalable. Less complex. Schemaless.
         Redis (embedded) can be used as in memory. Not appropriate for this usecase.

5. ORM Framework : Hibernate  
As the structure of the data is relational and we are looking for ACID behavior, so preferring 
    relational H2 in-memory DB.
   


## How to Run

### Prerequisites
1)Maven should be installed
2)Java should be installed

### Steps to run the application
1) Execute command : `mvn clean install`
2) move to target folder
3) execute command `java -jar moneytransfer-0.0.1-SNAPSHOT-shaded.jar`

### Configuration
There are 3 configuration files:
1) application.properties : to configure the application related configuration
2) log4j.properties : TO configure the logging level
3) hibernate.cfg.xml : Database related configuration

### Try the APIs
1)Load the swagger file in swagger editor.
2)Execute the APIs from there
 - Create user A Account
 - Create user B Account
 - Get all users accounts, to verify the balances as 0
 - Topup money into A user's account
 - Get all users accounts to check the updated balance
 - Transfer money from user A's account to user B's account
 - Get all users accounts to check the updated balance
 - Get all the transaction history, to check the transaction done


## APIs : 
Swagger doc present, in the root of the project, named 'swagger.yaml'. Load it in swagger io editor.


## DB Structure
1. Account table : accountNumber (pk), balance, earmarkedAmt, userId (fk), state (active/closed/blocked), version
2. User table : userId (pk), userName
3. TransactionHistory : fromAccountId, toAccountId, amount, transactionId (pk)
4. TransactionState : id (pk), status (EARMARKED / TRANSFERRED / RECEIVER_EARMARKUPDATED / SUCCESS / ROLLEDBACK), transactionId (fk)


## Solution Explanation :
This is an end to end solution implementation with the below APIs
1) Create user Account
2) Topup money into an account
3) Transfer money from one account to another
4) Get all users accounts
5) Get all the transaction history

### Transfer Money Flow

![Transfer Money flow](https://github.com/himkak/moneyTransfer/blob/master/FlowDiagram.jpg)

## Tests
Integration tests have been written to test all the scenarios. In these tests the application 
is started, client sends requests and asserts the response. Even the concurrency scenario has 
been tested with that.

To test postman script is also present.