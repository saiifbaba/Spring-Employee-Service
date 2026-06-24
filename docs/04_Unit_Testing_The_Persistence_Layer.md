# Unit Testing the Persistence Layer in Spring Boot

---

# Table of Contents

1. Introduction
2. What is the Persistence Layer?
3. Why Test the Persistence Layer?
4. Challenges of Repository Testing
5. Configuring Test Databases
6. Understanding @AutoConfigureTestDatabase
7. Using Embedded Databases (H2)
8. Using @DataJpaTest Slice
9. What Gets Loaded with @DataJpaTest?
10. Repository Testing Examples
11. Testing Custom Queries
12. Testing Relationships
13. Transaction Rollback Behavior
14. TestContainers Overview
15. Why Use TestContainers?
16. TestContainers with PostgreSQL
17. TestContainers Lifecycle
18. TestContainers vs H2
19. Best Practices
20. Common Mistakes
21. Quick Revision Sheet
22. Interview Questions and Answers

---

# 1. Introduction

In Spring Boot applications, the Persistence Layer is responsible for storing and retrieving data from the database.

Typical architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Repository testing verifies that:

* Data is saved correctly
* Queries return expected results
* Relationships work properly
* Database mappings are correct

---

# 2. What is the Persistence Layer?

Persistence Layer consists of:

```java
@Entity
@Repository
JpaRepository
EntityManager
Database
```

Example:

```java
@Entity
public class User {

    @Id
    private Long id;

    private String name;
}
```

Repository:

```java
@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

}
```

---

# 3. Why Test the Persistence Layer?

Even if business logic is correct, problems may exist in:

* Entity mappings
* JPQL queries
* Native SQL queries
* Relationships
* Constraints

Examples:

```java
@OneToMany
@ManyToOne
@JoinColumn
```

A repository test ensures database interactions work correctly.

---

# Real Production Example

Code compiles successfully.

Application starts successfully.

But:

```sql
SELECT * FROM users WHERE email = ?
```

returns wrong results due to incorrect query.

Repository tests catch these issues before deployment.

---

# 4. Challenges of Repository Testing

Repositories depend on:

```text
Database
Schema
Tables
JPA
Hibernate
Spring Context
```

Because of this, repository tests are not pure Unit Tests.

They are generally:

```text
Repository Slice Tests
or
Lightweight Integration Tests
```

---

# 5. Configuring Test Databases

Never run tests against production databases.

Testing should use:

```text
H2 Database
PostgreSQL TestContainer
MySQL TestContainer
```

Benefits:

* Isolated
* Safe
* Fast
* Repeatable

---

# Typical Test Database Flow

```text
Start Test
      ↓
Create Temporary Database
      ↓
Run Tests
      ↓
Delete Database
```

---

# 6. Understanding @AutoConfigureTestDatabase

Spring Boot automatically replaces your production database with an embedded database during tests.

Annotation:

```java
@AutoConfigureTestDatabase
```

---

# Default Behavior

Suppose application uses:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/app
```

During testing:

```text
MySQL
  ↓
Automatically Replaced By
  ↓
H2
```

---

# Example

```java
@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {

}
```

---

# Replace Modes

---

## Replace.ANY

```java
@AutoConfigureTestDatabase(
        replace = Replace.ANY
)
```

Behavior:

```text
Any Database
       ↓
Replace with Embedded Database
```

Most common option.

---

## Replace.NONE

```java
@AutoConfigureTestDatabase(
        replace = Replace.NONE
)
```

Behavior:

```text
Use Real Database
```

Commonly used with:

```text
TestContainers
PostgreSQL
MySQL
```

---

# Example

```java
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
```

Spring will not replace your datasource.

---

# 7. Using Embedded Database (H2)

Dependency:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

# Why H2?

Advantages:

* In-memory
* Fast
* Lightweight
* No installation required

---

# Test Flow

```text
Test Starts
      ↓
H2 Database Created
      ↓
Tables Generated
      ↓
Tests Run
      ↓
Database Destroyed
```

---

# 8. Using @DataJpaTest Slice

One of the most important Spring Boot testing annotations.

---

## Purpose

Loads only:

```text
Entities
Repositories
JPA Components
Hibernate
```

Does NOT load:

```text
Controllers
Services
Security
Web Layer
```

---

# Example

```java
@DataJpaTest
class UserRepositoryTest {

}
```

---

# What Spring Loads

```text
JpaRepository
EntityManager
Hibernate
DataSource
```

---

# What Spring Does NOT Load

```text
@RestController
@Service
@Configuration
SecurityConfig
```

This makes tests faster.

---

# 9. What Gets Loaded with @DataJpaTest?

```text
@DataJpaTest

     ↓

DataSource
Hibernate
JPA
Repositories
Entities
Transaction Manager
```

---

# Auto Rollback

Each test runs inside a transaction.

After test execution:

```text
Rollback
```

Database returns to clean state.

---

# Example

```java
@DataJpaTest
class UserRepositoryTest {

}
```

Spring automatically rolls back changes.

---

# 10. Repository Testing Example

---

## Entity

```java
@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String email;
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

## Test

```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void shouldSaveUser() {

        User user = new User();
        user.setEmail("john@gmail.com");

        User saved = repository.save(user);

        assertThat(saved.getId())
                .isNotNull();
    }
}
```

---

# Test Flow

```text
Create User
     ↓
Save User
     ↓
Insert into DB
     ↓
Verify ID Generated
```

---

# 11. Testing Custom Queries

Repository:

```java
@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User>
    findByEmail(String email);
}
```

---

# Test

```java
@Test
void shouldFindUserByEmail() {

    User user = new User();
    user.setEmail("john@gmail.com");

    repository.save(user);

    Optional<User> result =
            repository.findByEmail(
                    "john@gmail.com"
            );

    assertThat(result)
            .isPresent();
}
```

---

# Why Test Queries?

Common failures:

```text
Wrong field name
Wrong JPQL
Wrong SQL
Wrong Join
```

---

# 12. Testing Relationships

---

## One To Many Example

```java
@OneToMany
private List<Order> orders;
```

---

## Many To One Example

```java
@ManyToOne
private User user;
```

---

# Verify Relationship

```java
assertThat(
        savedUser.getOrders()
)
.hasSize(2);
```

Repository tests are the best place to validate entity mappings.

---

# 13. Transaction Rollback Behavior

Every @DataJpaTest is transactional.

Example:

```java
@DataJpaTest
class UserRepositoryTest {

}
```

Flow:

```text
Start Transaction
      ↓
Insert Records
      ↓
Run Assertions
      ↓
Rollback Transaction
```

Database remains clean.

---

# 14. TestContainers Overview

TestContainers is a Java library that runs real databases inside Docker containers.

Instead of H2:

```text
Real PostgreSQL
Real MySQL
Real MongoDB
```

inside Docker.

---

# Why Was TestContainers Created?

Problem:

H2 behaves differently from PostgreSQL.

Example:

```sql
JSONB
ARRAY
Postgres Functions
```

may work in PostgreSQL but fail in H2.

---

# Solution

Run actual database.

```text
Docker Container
      ↓
PostgreSQL
      ↓
Run Tests
```

---

# 15. Why Use TestContainers?

Advantages:

* Real database
* Production-like environment
* Consistent results
* Better confidence

---

# Example

Instead of:

```text
H2
```

Use:

```text
PostgreSQL Container
```

during tests.

---

# 16. TestContainers with PostgreSQL

Dependency:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

---

# Container Configuration

```java
@Testcontainers
@DataJpaTest
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:16"
            );
}
```

---

# Dynamic Property Source

```java
@DynamicPropertySource
static void configure(
        DynamicPropertyRegistry registry
){

    registry.add(
            "spring.datasource.url",
            postgres::getJdbcUrl
    );

    registry.add(
            "spring.datasource.username",
            postgres::getUsername
    );

    registry.add(
            "spring.datasource.password",
            postgres::getPassword
    );
}
```

Spring automatically connects to container database.

---

# Test Flow

```text
Start Docker
      ↓
Start PostgreSQL Container
      ↓
Connect Spring Boot
      ↓
Run Tests
      ↓
Destroy Container
```

---

# 17. TestContainers Lifecycle

```text
Test Starts
      ↓
Container Starts
      ↓
Database Ready
      ↓
Run Tests
      ↓
Container Stops
```

Everything is temporary.

---

# 18. TestContainers vs H2

| Feature               | H2     | TestContainers |
| --------------------- | ------ | -------------- |
| Speed                 | Faster | Slower         |
| Real DB               | No     | Yes            |
| Docker Required       | No     | Yes            |
| Production Similarity | Low    | High           |
| Confidence Level      | Medium | Very High      |
| Setup Complexity      | Easy   | Medium         |

---

# When To Use H2?

Good for:

* Learning
* Basic repository tests
* Small projects

---

# When To Use TestContainers?

Good for:

* Production applications
* PostgreSQL specific features
* Complex queries
* Enterprise systems

---

# 19. Best Practices

---

## Prefer @DataJpaTest for Repository Testing

Avoid:

```java
@SpringBootTest
```

when testing repositories only.

---

## Test Custom Queries

Always test:

```java
findByEmail()
findByUsername()
findByStatus()
```

---

## Verify Relationships

Test:

```java
@OneToMany
@ManyToOne
@ManyToMany
```

---

## Use TestContainers in Real Projects

Provides highest confidence.

---

## Keep Repository Tests Focused

Test:

* Persistence
* Queries
* Relationships

Not business logic.

---

# 20. Common Mistakes

---

## Using Full SpringBootTest

Bad:

```java
@SpringBootTest
class UserRepositoryTest
```

Slow startup.

---

## Testing Against Production Database

Never do this.

---

## Ignoring Query Tests

Custom queries can break easily.

---

## Assuming H2 Equals PostgreSQL

This assumption causes production bugs.

---

## Not Testing Entity Relationships

Mappings frequently fail in real applications.

---

# 21. Quick Revision Sheet

```text
@DataJpaTest
-------------
Loads:
✓ Repository
✓ Entity
✓ Hibernate
✓ DataSource

Does NOT Load:
✗ Controller
✗ Service
✗ Security

@AutoConfigureTestDatabase
--------------------------
Replace.ANY
→ Replace datasource

Replace.NONE
→ Use actual datasource

H2
---
Fast
In-memory

TestContainers
--------------
Real Database
Docker Required
Production-like Testing
```

---

# 22. Interview Questions and Answers

---

## Q1. What is @DataJpaTest?

### Answer

A Spring Boot test slice annotation that loads only JPA-related components such as repositories, entities, Hibernate, and datasource.

---

## Q2. What components are loaded by @DataJpaTest?

### Answer

* Entities
* Repositories
* Hibernate
* JPA
* DataSource
* Transaction Manager

---

## Q3. Does @DataJpaTest load Controllers and Services?

### Answer

No.

It only loads persistence-related components.

---

## Q4. Why is @DataJpaTest faster than @SpringBootTest?

### Answer

Because it loads only a small portion of the Spring Context instead of the entire application.

---

## Q5. What is @AutoConfigureTestDatabase?

### Answer

An annotation that controls whether Spring Boot replaces the application's datasource during tests.

---

## Q6. Difference between Replace.ANY and Replace.NONE?

### Answer

Replace.ANY:

Uses embedded test database.

Replace.NONE:

Uses actual configured datasource.

---

## Q7. Why do we use H2 in tests?

### Answer

Because it is lightweight, fast, in-memory, and requires no installation.

---

## Q8. What problem does TestContainers solve?

### Answer

It allows testing against real databases such as PostgreSQL or MySQL instead of simulated databases like H2.

---

## Q9. Why is TestContainers preferred in production projects?

### Answer

Because it provides a production-like environment and catches database-specific issues early.

---

## Q10. Does @DataJpaTest rollback changes automatically?

### Answer

Yes.

Each test runs inside a transaction and rolls back after execution.

---

## Q11. What is @Testcontainers?

### Answer

An annotation that enables TestContainers support in JUnit tests.

---

## Q12. What is @Container?

### Answer

Marks a Docker container managed by TestContainers.

---

## Q13. What is DynamicPropertySource?

### Answer

A mechanism for dynamically providing datasource properties from a running container to Spring Boot.

---

## Q14. Should business logic be tested in repository tests?

### Answer

No.

Repository tests should focus on persistence, mappings, and queries.

---

## Q15. In a real Spring Boot project, which approach is preferred?

### Answer

For enterprise applications:

```text
@DataJpaTest
+
TestContainers
+
PostgreSQL
```

This combination provides fast, reliable, and production-like repository testing.
