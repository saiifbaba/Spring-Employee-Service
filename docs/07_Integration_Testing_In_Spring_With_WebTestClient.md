# Integration Testing in Spring Boot with WebTestClient

---

# Table of Contents

1. Introduction to Integration Testing
2. Why Integration Testing is Important
3. Unit Testing vs Integration Testing
4. Spring Boot Integration Testing Architecture
5. What is WebTestClient?
6. Why Use WebTestClient?
7. WebTestClient vs MockMvc
8. Setting Up WebTestClient
9. Spring Test Annotations for Integration Testing
10. Basic WebTestClient Operations
11. WebTestClient Testing Methods
12. Testing GET APIs
13. Testing POST APIs
14. Testing PUT APIs
15. Testing DELETE APIs
16. Testing Request Headers
17. Testing Query Parameters
18. Testing Path Variables
19. Testing JSON Responses
20. Testing Collections
21. Testing Error Responses
22. Testing Authentication APIs
23. Integration Testing with TestContainers
24. Best Practices
25. Common Mistakes
26. Quick Revision Sheet
27. Interview Questions and Answers

---

# 1. Introduction to Integration Testing

Integration Testing verifies that multiple application components work correctly together.

Instead of testing:

```text
Only Service
```

We test:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

as a complete flow.

---

# Real Example

Suppose we have:

```text
POST /users
```

Request Flow:

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
      ↓
Response
```

Integration testing verifies the entire flow.

---

# 2. Why Integration Testing is Important

Unit Tests verify:

```java
UserService
```

Integration Tests verify:

```text
UserController
UserService
UserRepository
Database
```

working together.

---

# Problems Unit Tests Cannot Detect

---

## Wrong Spring Configuration

Example:

```java
@Autowired
private UserService service;
```

Bean creation may fail.

Unit Tests won't catch this.

Integration Tests will.

---

## Incorrect Database Mappings

```java
@OneToMany
@ManyToOne
```

mapping issues are detected.

---

## Broken REST APIs

Example:

```text
POST /users
```

returns:

```text
404
500
415
```

Integration Tests detect these issues.

---

## Serialization Problems

Example:

```java
LocalDateTime
UUID
Enum
```

JSON conversion may fail.

Integration Tests verify actual JSON responses.

---

# Benefits of Integration Testing

* Validates complete request flow
* Detects configuration issues
* Verifies database interactions
* Verifies REST APIs
* Builds confidence before deployment

---

# 3. Unit Testing vs Integration Testing

| Feature         | Unit Test    | Integration Test    |
| --------------- | ------------ | ------------------- |
| Scope           | Single Class | Multiple Components |
| Database        | No           | Yes                 |
| Spring Context  | No           | Yes                 |
| Speed           | Fast         | Slower              |
| Mocking         | Yes          | Rare                |
| Real HTTP Calls | No           | Yes                 |

---

# Example

Unit Test:

```java
@Mock
UserRepository repository;
```

Integration Test:

```java
@SpringBootTest
```

Loads actual Spring application.

---

# 4. Spring Boot Integration Testing Architecture

Typical flow:

```text
WebTestClient
        ↓
Controller
        ↓
Service
        ↓
Repository
        ↓
Database
```

Everything is real.

No mocks involved.

---

# 5. What is WebTestClient?

WebTestClient is Spring's HTTP client specifically designed for testing web applications.

It allows testing:

* REST APIs
* Controllers
* Request validation
* JSON responses

without manually opening Postman.

---

# Think of WebTestClient as

```text
Postman
      +
JUnit
      +
Assertions
```

inside your automated tests.

---

# Example

```java
webTestClient
    .get()
    .uri("/users")
    .exchange()
    .expectStatus()
    .isOk();
```

---

# 6. Why Use WebTestClient?

Advantages:

---

## Real HTTP Requests

Simulates actual client requests.

---

## Fluent API

Readable syntax.

```java
.expectStatus()
.isOk()
```

---

## JSON Validation

Can validate response body directly.

---

## Works with Reactive and Non-Reactive Applications

Supports:

```text
Spring MVC
Spring WebFlux
```

---

# 7. WebTestClient vs MockMvc

| Feature          | WebTestClient | MockMvc |
| ---------------- | ------------- | ------- |
| Spring MVC       | Yes           | Yes     |
| WebFlux          | Yes           | No      |
| Reactive Support | Yes           | No      |
| Fluent API       | Excellent     | Good    |
| Future Preferred | Yes           | Older   |

---

# Interview Tip

Modern Spring projects increasingly prefer:

```text
WebTestClient
```

especially with Spring Boot 3+.

---

# 8. Setting Up WebTestClient

Dependency already exists:

```xml
spring-boot-starter-test
```

---

# Test Class

```java
@SpringBootTest(
        webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UserControllerIT {

}
```

---

# Inject WebTestClient

```java
@Autowired
private WebTestClient webTestClient;
```

---

# Full Setup

```java
@SpringBootTest(
        webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UserControllerIT {

    @Autowired
    private WebTestClient webTestClient;
}
```

---

# Why RANDOM_PORT?

Spring starts an embedded server:

```text
8081
65432
54321
```

randomly.

Avoids port conflicts.

---

# 9. Important Spring Test Annotations

---

## @SpringBootTest

Loads entire Spring context.

```java
@SpringBootTest
```

---

## @AutoConfigureWebTestClient

Creates WebTestClient bean.

```java
@AutoConfigureWebTestClient
```

---

## @Testcontainers

Used with TestContainers.

```java
@Testcontainers
```

---

## @Transactional

Rollback changes after tests.

```java
@Transactional
```

---

# 10. Basic WebTestClient Operations

WebTestClient supports:

```text
GET
POST
PUT
DELETE
PATCH
```

---

# Structure

```java
webTestClient
        .method()
        .uri(...)
        .exchange()
        .expectStatus()
        .expectBody();
```

---

# 11. WebTestClient Testing Methods

---

## GET

```java
webTestClient.get()
```

---

## POST

```java
webTestClient.post()
```

---

## PUT

```java
webTestClient.put()
```

---

## DELETE

```java
webTestClient.delete()
```

---

## PATCH

```java
webTestClient.patch()
```

---

# Exchange

Actually sends request.

```java
.exchange()
```

Without this:

```text
Request never executes
```

---

# 12. Testing GET APIs

Controller:

```java
@GetMapping("/users/{id}")
public User getUser(Long id)
```

---

# Test

```java
webTestClient
        .get()
        .uri("/users/1")
        .exchange()
        .expectStatus()
        .isOk();
```

---

# Flow

```text
GET Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Response
```

---

# 13. Testing POST APIs

Controller:

```java
@PostMapping("/users")
public User createUser(...)
```

---

# Test

```java
webTestClient
        .post()
        .uri("/users")
        .bodyValue(userRequest)
        .exchange()
        .expectStatus()
        .isCreated();
```

---

# bodyValue()

Sends JSON body.

```java
.bodyValue(userRequest)
```

Equivalent to:

```json
{
  "name":"John"
}
```

---

# 14. Testing PUT APIs

```java
webTestClient
        .put()
        .uri("/users/1")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isOk();
```

---

# 15. Testing DELETE APIs

```java
webTestClient
        .delete()
        .uri("/users/1")
        .exchange()
        .expectStatus()
        .isNoContent();
```

---

# 16. Testing Request Headers

```java
webTestClient
        .get()
        .uri("/users")
        .header(
            "Authorization",
            "Bearer token"
        )
        .exchange();
```

Useful for:

* JWT
* OAuth2
* API Keys

---

# 17. Testing Query Parameters

```java
webTestClient
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/users")
                .queryParam("name","John")
                .build()
        )
        .exchange();
```

Request:

```text
/users?name=John
```

---

# 18. Testing Path Variables

```java
webTestClient
        .get()
        .uri("/users/{id}",1)
        .exchange();
```

Request:

```text
/users/1
```

---

# 19. Testing JSON Responses

Controller Response:

```json
{
  "id":1,
  "name":"John"
}
```

---

# Verify Response

```java
webTestClient
        .get()
        .uri("/users/1")
        .exchange()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("John");
```

---

# Verify Multiple Fields

```java
.expectBody()
.jsonPath("$.id").isEqualTo(1)
.jsonPath("$.name").isEqualTo("John");
```

---

# 20. Testing Collections

Response:

```json
[
  {
    "id":1
  },
  {
    "id":2
  }
]
```

---

# Verify Size

```java
.expectBodyList(User.class)
.hasSize(2);
```

---

# Verify Elements

```java
.expectBodyList(User.class)
.contains(user1,user2);
```

---

# 21. Testing Error Responses

Example:

```text
GET /users/999
```

User not found.

---

# Test

```java
webTestClient
        .get()
        .uri("/users/999")
        .exchange()
        .expectStatus()
        .isNotFound();
```

---

# Verify Error Message

```java
.expectBody()
.jsonPath("$.message")
.isEqualTo("User Not Found");
```

---

# 22. Testing Authentication APIs

JWT Login Example

```java
webTestClient
        .post()
        .uri("/auth/login")
        .bodyValue(loginRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.accessToken")
        .exists();
```

---

# Verify Protected Endpoint

```java
webTestClient
        .get()
        .uri("/users")
        .header(
            "Authorization",
            "Bearer " + token
        )
        .exchange()
        .expectStatus()
        .isOk();
```

---

# 23. Integration Testing with TestContainers

Best practice for enterprise applications.

---

# Flow

```text
Start PostgreSQL Container
           ↓
Start Spring Boot
           ↓
Run API Tests
           ↓
Destroy Container
```

---

# Benefits

* Real database
* Production-like testing
* Consistent environment

---

# Example

```java
@Testcontainers
@SpringBootTest
class UserIT {

}
```

---

# 24. Best Practices

---

## Test Complete User Flows

Example:

```text
Create User
Get User
Update User
Delete User
```

---

## Use RANDOM_PORT

```java
webEnvironment = RANDOM_PORT
```

---

## Use TestContainers

Prefer:

```text
PostgreSQL Container
```

over H2 for real projects.

---

## Verify Response Body

Don't only verify status codes.

Verify:

```json
{
  "id":1,
  "name":"John"
}
```

---

# 25. Common Mistakes

---

## Using Integration Tests for Everything

Integration Tests are slower.

Business logic should be covered by Unit Tests.

---

## Testing Only Status Codes

Bad:

```java
.expectStatus().isOk()
```

Good:

```java
.expectStatus().isOk()
.expectBody()
```

---

## Ignoring Error Scenarios

Always test:

```text
404
400
401
403
500
```

---

## Using Production Database

Never run tests against production systems.

---

# 26. Quick Revision Sheet

```text
@SpringBootTest
----------------
Loads Full Context

@AutoConfigureWebTestClient
---------------------------
Creates WebTestClient

WebTestClient
-------------
GET
POST
PUT
DELETE

.exchange()
-----------
Send Request

.expectStatus()
---------------
Verify Status

.expectBody()
-------------
Verify Response

.jsonPath()
-----------
Verify JSON

RANDOM_PORT
-----------
Avoid Port Conflicts
```

---

# 27. Interview Questions and Answers

---

## Q1. What is Integration Testing?

### Answer

Integration Testing verifies that multiple application components work together correctly.

Example:

```text
Controller
Service
Repository
Database
```

---

## Q2. Why is Integration Testing important?

### Answer

It detects:

* Configuration issues
* Database problems
* Serialization issues
* API failures

that Unit Tests cannot detect.

---

## Q3. What is WebTestClient?

### Answer

WebTestClient is Spring's HTTP testing client used to test REST APIs in integration tests.

---

## Q4. What is the advantage of WebTestClient?

### Answer

* Fluent API
* JSON validation
* Reactive support
* Real HTTP request simulation

---

## Q5. Difference between WebTestClient and MockMvc?

### Answer

WebTestClient supports both Spring MVC and WebFlux and is the preferred modern solution.

---

## Q6. What does .exchange() do?

### Answer

It sends the HTTP request and receives the response.

Without it, the request is not executed.

---

## Q7. What annotation loads the entire Spring Context?

### Answer

```java
@SpringBootTest
```

---

## Q8. Why use RANDOM_PORT?

### Answer

Avoids port conflicts during test execution.

---

## Q9. How do you verify JSON responses?

### Answer

Using:

```java
.expectBody()
.jsonPath("$.name")
.isEqualTo("John");
```

---

## Q10. How do you test a POST endpoint?

### Answer

```java
webTestClient
        .post()
        .uri("/users")
        .bodyValue(request)
        .exchange();
```

---

## Q11. How do you test authentication APIs?

### Answer

Send Authorization header and verify secured endpoint behavior.

---

## Q12. How do you test error responses?

### Answer

Verify:

```java
.expectStatus()
.isNotFound();
```

and validate error body.

---

## Q13. Can WebTestClient test Spring MVC applications?

### Answer

Yes.

It works with both:

```text
Spring MVC
Spring WebFlux
```

---

## Q14. Why use TestContainers with Integration Testing?

### Answer

To test against a real database such as PostgreSQL or MySQL.

---

## Q15. What is the recommended testing strategy?

### Answer

```text
70% Unit Tests
20% Integration Tests
10% End-to-End Tests
```

Use Unit Tests for business logic and Integration Tests for validating complete application flows.
