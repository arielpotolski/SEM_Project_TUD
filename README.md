# Microservices

This project contains 3 microservices
- user-microservice
- cluster-microservice
- request-microservice

The `authentication-microservice`, or also called `user-microservice` in our project, which can be used interchangeable, is responsible for registering new users and authenticating current ones. After successful authentication, this microservice will provide a JWT token which can be used to bypass the security on the `cluster-microservice` and `request-microservice`. This token contains the *NetID* of the user that authenticated, as well as the *role* of the user, which can be: 
- *user* - For regular users who want to use the cluster or contribute nodes.
- *faculty* - For accounts who can see more details on how the faculty's resources are distributed.
- *sysadmin* - For accounts who can see all faculty details, have full access to nodes and see the entire schedule
- *system*  - TO BE IMPLEMENTED

This microservice also handles notifications. It can receive updates from all other microservices about a user's requests and job's statuses. The user can retrieve their own notifications, delete them or see a specific timeframe of notifications, when authenticated.
User-microservice is accessible from localhost port 8081.
<br/><br/>

The `cluster-microservice` is responsible for keeping track of the entire cluster, and can handle nodes being added and removed from it. It is also responsible for dividing the resources based on the incoming requests it receives.
Cluster-microservice is accessible from localhost port 8082.

<br/><br/>
The `request-microservice` is responsible for handling cluster requests of users, who want to use CPU/GPU or memory from the Delft Blue supercomputer.
Request-microservice is accessible from localhost port 8083.

# Important Things for Grading

These files should be looked at:
- [Assignment 1](documents/Group-DB-11b-Assignment-1-Part-1.pdf): This is the PDF containing assignment 1.
-  INSERT TESTING HERE

# Implementation Choices

TBD

# Running the microservices

You can run the two microservices individually by starting the Spring applications, which are the *application.java* files in each microservice module's **"src/main/java"** folder. Then, you can use *Postman* to perform the different requests to use the system. All available endpoints are mentioned in  TBDDDD


# Using the project

Here are the most important endpoints for using the project. Starting with the `user-microservice` for logging in:

"http://localhost:8081/register". Here a user can send a netId with a password to register a new user account.

"http://localhost:8081/authenticate". Here a user can send their netId with their password, en will receive back their own JWT-Token. This token is needed to make requests to other endpoints which can verify your identity.

"http://localhost:8081/applyFaculty". Here an endpoint where a netId and a faculty can be sent as Strings, and that user will be registered to the corresponding faculty.

Now we move on to the `request-microservice`:

"http://localhost:8083/sendRequest". Here an authenticated user can send request information, on how much CPU/GPU or memory they want from the cluster.

"http://localhost:8083/publishRequest". Here an authenticated user can send the ID's of the requests they want to publish. These request are checked if the user is allowed to make the request if they are registered to the corresponding faculty, and if that faculty has enough resources left over.
The request is either sent to the cluster microservice for scheduling and further processing, or is cancelled, in which case a notification will be sent to the user microservice.

Now we move on to the `cluster-microservice`:

"http://localhost:8082/nodes/add". Here a user can submit their own node to the network so those resources can be used by others. The nodes can always be deleted if the user does not want to contribute anymore.

"http://localhost:8082/schedule". Here a sysadmin-role user can see the entire schedule of the cluster. 





"http://localhost:8083/sendRequest". Here an authenticated user

[//]: # (![image]&#40;instructions/register.png&#41;)

Authenticate:

Hello:
