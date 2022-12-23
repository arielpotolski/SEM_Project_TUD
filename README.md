# Lab Template

This project contains 3 microservices
- user-microservice
- cluster-microservice
- request-microservice

The `user-microservice` is responsible for registering new users and authenticating current ones. After successful authentication, this microservice will provide a JWT token which can be used to bypass the security on the `cluster-microservice` and `request-microservice`. This token contains the *NetID* of the user that authenticated, as well as the role of the user, which can be: 
- *user* - For regular users who want to use the cluster or contribute nodes.
- *faculty* - For accounts who can see more details on how the faculty's resources are distributed.
- *sysadmin* - For accounts who can see all faculty details, have full access to nodes and see the entire schedule
- *system*  - TO BE IMPLEMENTED



## Running the microservices

You can run the two microservices individually by starting the Spring applications, which are the *application.java* files. Then, you can use *Postman* to perform the different requests:

Register:

[//]: # (![image]&#40;instructions/register.png&#41;)

Authenticate:

Hello:
