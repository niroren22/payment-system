# Payment-System

This payment system is a distributed payment service application. 
The system provides a platform for customers to pay other customers for various services.

## Components
- **Payment Service** - web service that exposes REST API for sending payments and retrieving user information.
- **Risk Engine** - service that computes risk assessment for transactions and determines whether to approve or reject the transaction.

### Third parties and technology stack
The payment service uses ***Spring Boot*** with embedded Tomcat server, and ***Jersey (JAX_RS)*** for REST endpoints.
The risk engine service uses ***MySQL*** database to save transactions, and ***Liquibase*** for schema management and migration.

### Design and Architecture
The payment workflow in the system is built in an event driven paradigm.
In this approach buisness events are propogated through the system and triggers different actions in different services.
I am using ***Kafka Message Broker*** to pass payment requests between the web server and the risk engine, and specifically ***Kafka Streams*** to implement the event-driven approach.

The payment service exposes a REST interface to POST and GET payment. 
A posted payment request creates an event in Kafka that is recorded in the ***payments*** topic.
The event is then picked up by the Risk Engine service for validation. The payment request is then validated and persisted in the database with a ACCEPTED or REJECTED status.
The payment validation result is then pushed back to Kafka through a separate topic - ***validated-payments***.

To allow users to GET the validated payments result, the Payment Service create a queriable materialized view of the ***validated-payments*** topic, using a state store in the Payment Service so payments can be requested historically.

To relieve the pressure on REST I/O threads, the POST and GET payment APIs are asynchroneus - the POST returns when the payment message was committed to the queue (or timeout), and the GET returns when the validated payment was found in the store (or timeout).

### Code Generation
I used Apache Avro to generate DTO objects to send through Kafka queue and also for responses to the user. 
Kafka is sending these object as a byte stream in an efficient way, also the generation template is easy and straightforward.

For JOOQ code generation I used a temporary in memory database during the build process - HSQL.
This allowed me not to be dependent on a running MySQL server in order to build the project.

## Getting Started
I provided 2 methods to run the payment system application:
- **Production** - using Docker.
- **Development** - run locally all 5 services separatly (Zookeeper, Kafka, MySQL, payment-service, risk-engine).

### Deployment - Using Docker
Make sure that you have docker and docker-compose installed and using a linux deamon.
```
# cd to the project base directory.

# Build docker images
$ docker-compose build

# Start services in adocker container
$ docker-compose up
```

### Development - running locally
Make sure you have kafka and MySQL database server installed before you start.
```
# Build the project:
# cd to the project base direcory
$ mvn clean install -DskipTests

# Run zookeeper
# cd to kafka base directory
$ .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# Run Kafka-Broker
# In he same directory (kafka base directory)
$ .\bin\windows\kafka-server-start.bat .\config\server.properties

# Create topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic payments
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic validated-payments

# Start Risk-Engine
$ cd <project-base-dir>/risk-engine
$ mvnw spring-boot:run

# Start Payment-Service
$ cd <project-base-dir>/payment-service
$ mvnw spring-boot:run
```

## REST API
### Send Payment
- `POST /payments`
- Body:
```
{
    "amount" : 19.90, // Amount to pay
    "currency" : "USD", // Payment currency
    "userId" : "3a381912-ab81-4474-b1cc-8bd7f1b60822", // Paying user unique identifier
    "payeeId" : "7451c4b2-7737-4178-a4f6-7a42cd1c0bc9", // Payee user unique identifier
    "paymentMethodId" : "47560c84-3703-4efd-b02f-76567584d2a7" // Payment Method unique identifier
}
```
- Responses:
  - 202 - Accepted. Includes the payment URL to retrieve its status:
    - `http://localhost:8080/payments/97051c8a-2e19-4f51-b6fb-ab6c2d7491d4`
  - 404 - Payer/Payee/Payment method not found.
  - 406 - Currency code not recognized / Payed amount must be positive.
  - 503 - Payment request timed out.
  
### Retrieve Payment
- `GET /payments/{paymentId}`
- Responses:
  - 200 - OK
```
{
    "payment": {
        "paymentId": "97051c8a-2e19-4f51-b6fb-ab6c2d7491d4",
        "userId": "3a381912-ab81-4474-b1cc-8bd7f1b60822",
        "payeeId": "7451c4b2-7737-4178-a4f6-7a42cd1c0bc9",
        "amount": 19.90,
        "currency": "USD",
        "paymentMethodId": "47560c84-3703-4efd-b02f-76567584d2a7"
    },
    "validation": "AUTHORIZED"
}
```
  - 503 - Payment not found within timeout.
  
### Get User by id
  - `GET /users/{id}`
  - Responses:
    - 200 - OK
```
{
    "userId": "3a381912-ab81-4474-b1cc-8bd7f1b60822",
    "firstName": "Barney",
    "lastName": "Leverington",
    "email": "bleverington5@spotify.com",
    "gender": "Male"
}
```
  - 404 - User not found.
  
### Get Users
  - `GET /users` - Retrieves all users.
  - `GET /users?email={userEmail}` - get user by email.
  
### Get Payment Methods
  - `GET /users/{id}/payment-methods`
  - Responses:
    - 200 - OK
```
[
  {
    "paymentMethodId": "47560c84-3703-4efd-b02f-76567584d2a7",
    "type": "BANK_ACCOUNT",
    "name": "Columbia Banking System",
    "last4": 9728,
    "userId": "3a381912-ab81-4474-b1cc-8bd7f1b60822"
  }, ...
]

