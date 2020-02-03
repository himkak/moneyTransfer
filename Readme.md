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
     Restlet : Lightweight.  Supports major web standards. Many extensions also available to integrate like jackson, . Suitable for both server and client applications.
     RestEay  
     Jersey v2: A lightweight framework.  It is an implementation of the jax-rs standard, 
        and many other frameworks can natively understand the jax-rs annotations. Open source. 
     
     Apache CXF  
     Dropwizard : A heavy framework. Lot of plumbing reqd even to build tiny service. Uses Jetty server
     javalin :   
     vertx : Uses Jetty server
     spark  : 
            Uses Jetty server

    Comparison on points :
        server being used
        leight weight
        logging
        documentation
        monitoring
        in build features

    References:
        https://cdelmas.github.io/2015/11/01/A-comparison-of-Microservices-Frameworks.html
    
    

2. Logging
3. Testing:
    Unit Testing : JUNIT
    Tools : 
        JMeter
        SoapUI

4. DB :
    Requirement is to have in-memory DB

   Relational: Provides ACID behavior. Vertically Scalable. More complex as have to maintain relationship. 
    H2 is the in-memory DB 
   NoSql : Horizontally scalable. Less complex. Schemaless.
    Redis (embedded) can be used as in memory

5. ORM Framework : Hibernate

As the structure of the data is relational and we are looking for ACID behavior, so preferring 
    relational H2 in-memory DB.
   

5.
   

## Use case

## How to Run

## APIs : swagger doc also present
1. send money 
   Http method : PUT
   url : accounts/transfer/
   request body: fromAccount, toAccount, amount
   response : requestId
   
2. create account
   Http method : POST
   url: accounts
   body: {userName, userId}
   response : json : {accountNumber}, http status : 201

## DB Structure
1. Account table : accountNumber (pk), totalAmt, earmarkedAmt, userId, state (active/closed/blocked)
2. User table : userId, userName
