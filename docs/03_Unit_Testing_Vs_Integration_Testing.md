# Unit Testing vs Integration Testing in Spring Boot

---

# Table of Contents

1. Introduction
2. What is Unit Testing?
3. Characteristics of Unit Testing
4. Unit Testing Example
5. What is Integration Testing?
6. Characteristics of Integration Testing
7. Integration Testing Example
8. Unit Testing vs Integration Testing
9. Test Pyramid
10. Key Test Annotations in Spring Boot
11. When to Use Which Test?
12. Best Practices
13. Common Mistakes
14. Interview Questions and Answers

---

# 1. Introduction

Testing ensures that our application behaves correctly.

In Spring Boot applications, testing is generally divided into:

```text
Unit Testing
        +
Integration Testing
```

These are the most commonly used testing approaches in real-world projects.

A Spring Boot application usually contains:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Unit Tests verify each layer individually.

Integration Tests verify that multiple layers work together correctly.

---

# 2. What is Unit Testing?

Unit Testing means testing a single unit of code in isolation.

A "Unit" can be:

* Method
* Function
* Class

The goal is to verify business logic without involving external dependencies.

---

## Example

Testing:

```java
public int add(int a, int b){
    return a + b;
}
```

Only this method is tested.

No database.

No API calls.

No Spring Context.

---

# Characteristics of Unit Testing

## Fast

Usually executes within milliseconds.

---

## Isolated

No interaction with:

* Database
* Network
* File System
* External APIs

---

## Uses Mocks

Dependencies are replaced by mocks.

---

## Easy to Debug

Failures point directly to a specific method.

---

## Runs Frequently

Executed during:

* Development
* Git commits
* CI/CD pipelines

---

# 3. Unit Testing Example

---

## Production Code

### User Repository

```java
public interface UserRepository {

    Optional<User> findById(Long id);
}
```

---

### User Service

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User getUser(Long id) {

        return repository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException("User Not Found")
                );
    }
}
```

---

# Unit Test

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void shouldReturnUser() {

        User user = new User(1L, "John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = service.getUser(1L);

        assertThat(result.getName())
                .isEqualTo("John");
    }
}
```

---

# Unit Test Flow

```text
Test
 ↓
Mock Repository
 ↓
Call Service Method
 ↓
Verify Result
```

Database is never touched.

Everything is simulated using Mockito.

---

# Benefits of Unit Testing

* Extremely fast
* Cheap to execute
* Detects bugs early
* Easy maintenance
* High developer confidence

---

# 4. What is Integration Testing?

Integration Testing verifies that multiple components work together.

Instead of testing a single class, it tests interactions between components.

---

## Example

```text
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

The entire flow is tested.

---

# Characteristics of Integration Testing

---

## Loads Spring Context

Spring creates actual beans.

---

## Uses Real Dependencies

Can use:

* Real database
* H2 database
* Actual repositories

---

## Slower Than Unit Tests

Because Spring Context starts.

---

## Verifies Component Communication

Checks:

* Controller → Service
* Service → Repository
* Repository → Database

---

# Integration Testing Example

---

## User Entity

```java
@Entity
public class User {

    @Id
    private Long id;

    private String name;
}
```

---

## Repository

```java
@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

}
```

---

## Service

```java
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User save(User user){
        return repository.save(user);
    }
}
```

---

# Integration Test

```java
@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository repository;

    @Test
    void shouldSaveUser() {

        User user = new User();
        user.setName("John");

        User saved = service.save(user);

        assertThat(saved.getId())
                .isNotNull();

        assertThat(
                repository.findById(saved.getId())
        ).isPresent();
    }
}
```

---

# Integration Test Flow

```text
Test
 ↓
Spring Context Starts
 ↓
UserService Bean Created
 ↓
Repository Bean Created
 ↓
Database Connection Created
 ↓
Save User
 ↓
Verify Database Record
```

Everything is real.

No mocks.

---

# 5. Unit Testing vs Integration Testing

| Feature          | Unit Test    | Integration Test    |
| ---------------- | ------------ | ------------------- |
| Scope            | Single Class | Multiple Components |
| Speed            | Very Fast    | Slower              |
| Database         | No           | Yes                 |
| Spring Context   | No           | Yes                 |
| Uses Mocking     | Yes          | Usually No          |
| Complexity       | Low          | Medium              |
| Reliability      | High         | High                |
| Maintenance Cost | Low          | Higher              |

---

# Example Comparison

---

## Unit Test

```java
@Mock
private UserRepository repository;
```

Repository is fake.

---

## Integration Test

```java
@Autowired
private UserRepository repository;
```

Repository is real.

---

# Real Project Example

Suppose Login API:

```text
POST /login
```

---

## Unit Test

Test only:

```java
AuthService.login()
```

Mock:

* UserRepository
* PasswordEncoder
* JWTService

---

## Integration Test

Test entire flow:

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
```

Verify actual response.

---

# 6. Test Pyramid

```text
             UI Tests
                ▲
                │
       Integration Tests
                ▲
                │
           Unit Tests
```

---

## Recommended Distribution

```text
Unit Tests       70%
Integration      20%
UI Tests         10%
```

Reason:

Unit tests are:

* Fast
* Cheap
* Easy

Integration tests are:

* Valuable
* Slower

---

# 7. Key Test Annotations in Spring Boot

These annotations are extremely important for interviews.

---

# @Test

Marks a test method.

```java
@Test
void shouldCreateUser() {

}
```

---

# @SpringBootTest

Loads entire Spring Application Context.

```java
@SpringBootTest
class UserTest {

}
```

Use for:

* Integration Testing
* End-to-End Component Testing

---

# @ExtendWith(MockitoExtension.class)

Enables Mockito support.

```java
@ExtendWith(MockitoExtension.class)
```

Used in Unit Tests.

---

# @Mock

Creates a mock object.

```java
@Mock
private UserRepository repository;
```

Fake dependency.

---

# @InjectMocks

Injects mocks into class under test.

```java
@InjectMocks
private UserService service;
```

Mockito automatically injects mocks.

---

# @Autowired

Injects real Spring bean.

```java
@Autowired
private UserService service;
```

Mostly used in Integration Tests.

---

# @DataJpaTest

Loads only JPA-related components.

```java
@DataJpaTest
class RepositoryTest {

}
```

Used for repository testing.

Starts:

* EntityManager
* Repositories
* H2 Database

Does NOT load:

* Controllers
* Services

---

# @WebMvcTest

Loads only MVC components.

```java
@WebMvcTest(UserController.class)
```

Used for controller testing.

Loads:

* Controller
* MockMvc

Does NOT load:

* Services
* Database

---

# @AutoConfigureMockMvc

Configures MockMvc.

```java
@SpringBootTest
@AutoConfigureMockMvc
```

Allows API testing without starting server.

---

# @Transactional

Rolls back database changes after test.

```java
@Transactional
```

Useful in Integration Tests.

---

# 8. When Should You Use Unit Testing?

Use Unit Testing when:

* Testing business logic
* Testing utility methods
* Testing service layer
* Verifying edge cases

Example:

```java
calculateDiscount()
generateToken()
validateEmail()
```

---

# 9. When Should You Use Integration Testing?

Use Integration Testing when:

* Testing database operations
* Testing REST APIs
* Testing repositories
* Testing Spring configuration

Example:

```java
User Registration Flow
Login Flow
Order Placement Flow
Payment Flow
```

---

# 10. Best Practices

---

## Write More Unit Tests

Follow:

```text
70% Unit
20% Integration
10% UI
```

---

## Keep Unit Tests Isolated

Never connect to database.

Use mocks.

---

## Test Real Scenarios in Integration Tests

Verify:

* Database persistence
* API responses
* Bean configuration

---

## Use H2 Database

Common choice for Integration Tests.

```xml
<dependency>
    <groupId>com.h2database</groupId>
</dependency>
```

---

## Follow AAA Pattern

```text
Arrange
Act
Assert
```

Example:

```java
// Arrange

// Act

// Assert
```

---

# 11. Common Mistakes

---

## Using SpringBootTest for Everything

Bad:

```java
@SpringBootTest
class CalculatorTest
```

Loads entire Spring Context unnecessarily.

Use simple Unit Test instead.

---

## Database Access in Unit Tests

Bad practice.

Unit Tests should not use:

```text
Database
Network
File System
```

---

## Too Many Integration Tests

They become:

* Slow
* Expensive
* Hard to maintain

---

## Not Testing Edge Cases

Always test:

* Null values
* Empty inputs
* Invalid data
* Exceptions

---

# Quick Revision Sheet

```text
Unit Test
---------
Fast
Mock Dependencies
No Spring Context
No Database

Integration Test
----------------
Real Components
Spring Context
Database Access
Slower

@SpringBootTest
→ Full Context

@DataJpaTest
→ Repository Testing

@WebMvcTest
→ Controller Testing

@Mock
→ Fake Object

@InjectMocks
→ Inject Mocks

@Autowired
→ Real Bean
```

---

# Interview Questions and Answers

---

## Q1. What is Unit Testing?

### Answer

Unit Testing verifies a single class, method, or component in isolation without external dependencies.

---

## Q2. What is Integration Testing?

### Answer

Integration Testing verifies interactions between multiple components such as Controller, Service, Repository, and Database.

---

## Q3. What is the main difference between Unit Testing and Integration Testing?

### Answer

Unit Testing:

* Tests a single component
* Uses mocks
* Fast

Integration Testing:

* Tests multiple components together
* Uses real dependencies
* Slower

---

## Q4. Why are Unit Tests faster?

### Answer

Because they do not start Spring Context or connect to external systems like databases.

---

## Q5. What annotation is commonly used for Integration Testing?

### Answer

```java
@SpringBootTest
```

It loads the entire Spring Application Context.

---

## Q6. What is @Mock?

### Answer

Creates a fake object that simulates dependency behavior.

Example:

```java
@Mock
private UserRepository repository;
```

---

## Q7. What is @InjectMocks?

### Answer

Injects mock dependencies into the class being tested.

---

## Q8. What is @DataJpaTest?

### Answer

A slice test that loads only JPA-related components for repository testing.

---

## Q9. What is @WebMvcTest?

### Answer

A slice test that loads only Spring MVC components for controller testing.

---

## Q10. What is MockMvc?

### Answer

A testing tool used to test REST APIs without starting a real web server.

---

## Q11. Should Unit Tests access a database?

### Answer

No.

Unit Tests should remain isolated and use mocks.

---

## Q12. Why use H2 Database in Integration Tests?

### Answer

Because it is lightweight, in-memory, fast, and easy to configure.

---

## Q13. Which tests should be greater in number?

### Answer

Unit Tests.

A common recommendation is:

```text
70% Unit Tests
20% Integration Tests
10% UI Tests
```

---

## Q14. Can Mockito be used in Integration Tests?

### Answer

Generally no.

Integration Tests usually use real dependencies.

Mockito is primarily used in Unit Testing.

---

## Q15. What is the biggest mistake beginners make?

### Answer

Using:

```java
@SpringBootTest
```

for every test, even when a simple Unit Test would be sufficient. This makes the test suite unnecessarily slow.
