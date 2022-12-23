# Microservices

In our implementation of Delft Blue, we opted to have three microservices:
- user-microservice
- cluster-microservice
- request-microservice

***

The `authentication-microservice`, also referred to as `user-microservice`, is responsible for registering new users and authenticating current ones. After successful authentication, this microservice will provide a JWT token which can be used to bypass the security on the `cluster-microservice` and `request-microservice`. This token contains the *NetID* of the user that authenticated, as well as the *role* of the user, which can be: 
- *user* - For regular users who want to use the cluster or contribute nodes.
- *faculty* - For accounts who can see more details on how the faculty's resources are distributed, as well as approve or reject job requests in the request microservice
- *sysadmin* - For accounts who can see all information existing in the system, have full access to nodes, and see the entire schedule. These users also have some other special priviliges, like deleting nodes
with immediate effect, over scheduling their removal
- *system*  - For interaction between microservices without direct user involvement; an example would be asynchronous posting of notifications from the cluster to user

This microservice also handles notifications. It can receive updates from all other microservices about a user's requests and job's statuses. The user can retrieve their own notifications, delete them or see a specific timeframe of notifications when authenticated.

User-microservice is accessible from localhost port 8081.
***
The `cluster-microservice` is responsible for keeping track of the entire cluster, including the nodes and the complete schedule of jobs. In our implementation, we opted for the cluster to have automated scheduling of incoming jobs forwarded to it
by the request microservice. The cluster also provides access to information about assigned, reserved, and available resources per day and faculty. These data are locked behind several layers of authorization:
what information a user can access depends on their role. It further
contains an asynchronous notification manager, which posts all job-related notifications to the user/auth service. If the system had a frontend, cluster would directly notify it of developments as well.


Cluster-microservice is accessible from localhost port 8082.
***
The `request-microservice` is responsible for handling requests of users who want to use CPU/GPU or memory from the Delft Blue supercomputer to complete some job. 
Requests are sent here and remain until an account with faculty privileges approves or rejects them. This changes after 6 PM, when requests no longer require manual 
approval. 

Request-microservice is accessible from localhost port 8083.

# Documents Relevant for Grading

These files are relevant for the grading of the project:
- [Assignment 1a](documents/Group-DB-11b-Assignment-1-Part-1.pdf): This is the PDF containing assignment 1 part 1, the UML diagram and architecture breakdown.
- [Assignment 1b](documents/Group-DB-11b-Assignment-1-Part-2.pdf): The file containing the second part of assignment 1; the diagrams of the design patterns we implemented in the code,
elaboration on why we chose them and how we implemented them in the code.
- [Functional Testing](documents/Group-DB-11b-Functional-Testing.pdf): The document detailing our end-to-end tests, the user journeys we identified and 
followed when testing.
- [Boundary Testing](documents/Group-DB-11b-Boundary-Testing.pdf): The document detailing our boundary tests, focusing on the ways in which we tried to break the system,
where it did break and where it held, as well as the improvements we implemented as a result of our findings.

# Running the microservices

- from the terminal: `./gradlew microservice-name:bootRun` for the three main microservices (authentication-microservice, cluster-microservice, request-microservice). The order should not matter.
- from an IDE: the recommended option. After the project is built using Gradle, the Application class in each of the three microservices
should become runnable. Run these in any order by clicking the green arrow.

# Using the project

Here are the most important endpoints for using the project. Starting with the `user-microservice` for logging in:

"http://localhost:8081/register". Here a user can send a netId with a password to register a new user account.

"http://localhost:8081/authenticate". Here a user can send their netId with their password, en will receive back their own JWT-Token. This token is needed to make requests to other endpoints which can verify your identity.

"http://localhost:8081/applyFaculty". Here an endpoint where a netId and a faculty can be sent as Strings, and that user will be registered to the corresponding faculty.

Now we move on to the `request-microservice`:

"http://localhost:8083/job/sendRequest". Here an authenticated user can send request information, on how much CPU/GPU or memory they want from the cluster.

"http://localhost:8083/job/publishRequest". Here an authenticated user can send the ID's of the requests they want to publish. These request are checked if the user is allowed to make the request if they are registered to the corresponding faculty, and if that faculty has enough resources left over.
The request is either sent to the cluster microservice for scheduling and further processing, or is cancelled, in which case a notification will be sent to the user microservice.

Now we move on to the `cluster-microservice`:

"http://localhost:8082/nodes/add". Here a user can submit their own node to the network so those resources can be used by others. The nodes can always be deleted if the user does not want to contribute anymore.

"http://localhost:8082/schedule". Here a sysadmin-role user can see the entire schedule of the cluster.

"http://localhost:8082/request". Called by the request service when forwarding accepted requests.

"http://localhost:8082/resources/available". Acquiring the available resources per faculty per day from the cluster.
