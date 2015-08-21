# Exchange Money API


A RESTful API that allows the exchange of amounts between accounts of any currency.

Databases start empty on application launch. All data needs to be created through REST. (See examples)

The API was developed using Java 8.
 
## Features

* Account Creation (any currency)
* 2 Step Transaction creation/execution to avoid duplication due to network glitches
* Transactions can be created in any currency
* Meaningful/RESTful return status codes
* A dummy exchange rate service (all currencies equal in current implementation)
* Concurrent updates handling the race conditions between resources
* In memory manual store of accounts / transactions
* SpringBoot framework (DI and REST API)
* Clean separation of model and business logic from the frameworks used
* Tests (TDD followed throughout)

*For the API HATEOAS was not used since nowadays the client tools supporting hateoas are very limited. But soon it
could be an option.

## Requires
* Java 8
* Maven


## How to start

Once the application is fetched from git it can be built with maven

    mvn clean install

This will fetch dependencies and run all tests

To run the app execute:

    mvn spring-boot:run

The application will start and will be listening to the port 8080

## Examples

### Accounts

#### Create an account

The following creates an account and returns the created resource and its location via the appropriate header

    POST /accounts
    {
        "balance": {
            value: 20,
            currency: "EUR"
        }
    }

Example response:

    HTTP 201 Created
    {
        "id": 1
        "balance": {
            value: 20,
            currency: "EUR"
        }
    }    

#### List all accounts

The following gets all the accounts that exist in the DB

    GET /accounts

Example response:


    HTTP 200 OK
    [{
        "id": 1,
        "balance": {
            value: 20,
            currency: "EUR"
        }
    }]

#### Get account details

The following gets all the accounts that exist in the DB

    GET /accounts/1


Example response:

    HTTP 200 OK
    {
        "id": 1,
        "balance": {
            value: 20,
            currency: "EUR"
        }
    }

### Transactions

#### Create a transaction

The following creates a new transaction if possible (valid accounts and parameters)

    POST /transactions
    {
        "sourceId": 1,
        "destinationId": 2,
        "amount": {
            "value": "5",
            "currency": "EUR"
        }
    }
    
This will return the created resource and its location. It automatically generates an auto
incremented ID which needs to be used in order for this transaction to be executed.

Example response:

    HTTP 201 Created
    {
        "transaction": {
            "id": 1,
            "sourceId": 1,
            "destinationId": 2,
            "amount": {
                "value": "5",
                "currency": "EUR"
            },
            "status": "PENDING"
        }
        "status": "SUCCESS",
        "detailMessage": ""
    }

#### Execute a transaction

Given that a transaction is created, it can be executed as follows:

    PUT /transactions/1/executed

Example response:

    HTTP 200 OK
    {
        "transaction": {
            "id": 1,
            "sourceId": 1,
            "destinationId": 2,
            "amount": {
                "value": "5",
                "currency": "EUR"
            },
            "status": "EXECUTED"
        }
        "status": "SUCCESS",
        "detailMessage": ""
    }

#### Get all transactions

    GET /transactions

Example response:

    HTTP 200 OK    
    [{
        "id": 1,
        "sourceId": 1,
        "destinationId": 2,
        "amount": {
            "value": "5",
            "currency": "EUR"
        },
        "status": "EXECUTED"
    }]
    
#### Get a specific transaction by its ID

    GET /transactions/1

Example response:

    HTTP 200 OK    
    {
        "id": 1,
        "sourceId": 1,
        "destinationId": 2,
        "amount": {
            "value": "5",
            "currency": "EUR"
        },
        "status": "EXECUTED"
    }
