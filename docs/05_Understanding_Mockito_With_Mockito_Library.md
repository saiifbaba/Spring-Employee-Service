# Understanding Mockito with Mockito Library

---

# Table of Contents

1. Introduction to Mockito
2. Why Mockito is Needed
3. What is a Mock?
4. Mockito Architecture
5. Creating Mocks
6. Mockito Annotations
7. Stubbing Mocks
8. Verifying Mocks
9. Argument Matchers
10. ArgumentCaptor
11. Common Mockito Methods
12. Mock vs Stub vs Spy
13. Mockito Testing Flow
14. Best Practices
15. Common Mistakes
16. Quick Revision Sheet
17. Interview Questions and Answers

---

# 1. Introduction to Mockito

Mockito is the most popular Java mocking framework.

It is used to:

* Create fake objects
* Simulate dependency behavior
* Verify interactions
* Isolate business logic

Mockito is heavily used in:

```text
Spring Boot
JUnit 5
Unit Testing
```

---

# Why Mockito?

Suppose we have:

```text
UserService
    ↓
UserRepository
    ↓
Database
```

While testing UserService, we do not want:

* Database access
* Network calls
* External dependencies

We only want to test:

```java
UserService
```

This is where Mockito helps.

---

# 2. Why Mockito is Needed

Without Mockito:

```text
UserService
     ↓
Repository
     ↓
Database
```

Test becomes:

* Slow
* Complex
* Integration Test

---

With Mockito:

```text
UserService
      ↓
Mock Repository
```

Result:

* Fast
* Isolated
* Reliable Unit Test

---

# Real World Example

Production Code:

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User getUser(Long id) {
        return repository.findById(id)
                .orElseThrow();
    }
}
```

Testing this service should not require:

```text
Database
Spring Context
Hibernate
```

Mockito replaces the repository.

---

# 3. What is a Mock?

A Mock is a fake implementation of a dependency.

Example:

Real Repository:

```java
UserRepository repository;
```

Mock Repository:

```java
UserRepository repository = mock(UserRepository.class);
```

Mockito creates a fake object at runtime.

---

# Real vs Mock

## Real Object

```java
repository.findById(1L);
```

Actually queries database.

---

## Mock Object

```java
repository.findById(1L);
```

Returns predefined value.

No database involved.

---

# 4. Mockito Architecture

Typical Flow:

```text
Create Mock
      ↓
Stub Behavior
      ↓
Call Method
      ↓
Verify Result
      ↓
Verify Interaction
```

---

# Example Flow

```text
Mock Repository
       ↓
Return Fake User
       ↓
Call Service
       ↓
Verify Output
```

---

# 5. Creating Mocks

Mockito provides multiple ways.

---

## Method 1: mock()

```java
UserRepository repository =
        mock(UserRepository.class);
```

Creates a mock manually.

---

# Example

```java
@Test
void createMock() {

    UserRepository repository =
            mock(UserRepository.class);

    assertNotNull(repository);
}
```

---

## Method 2: @Mock Annotation

Most common approach.

```java
@Mock
private UserRepository repository;
```

Mockito creates the object automatically.

---

# Enable Mockito

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

}
```

Required for annotation-based mocks.

---

# Example

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
}
```

---

# 6. Mockito Annotations

---

## @Mock

Creates fake dependency.

```java
@Mock
private UserRepository repository;
```

---

## @InjectMocks

Injects mocks into class under test.

```java
@InjectMocks
private UserService service;
```

---

# Example

```java
@Mock
private UserRepository repository;

@InjectMocks
private UserService service;
```

Mockito internally does:

```java
service =
    new UserService(repository);
```

---

## @Captor

Creates ArgumentCaptor automatically.

```java
@Captor
ArgumentCaptor<User> captor;
```

Used for capturing method arguments.

---

## @Spy

Creates partial mock.

```java
@Spy
private ArrayList<String> list;
```

Real methods execute unless stubbed.

---

# 7. Stubbing Mocks

Stubbing means:

```text
Define behavior of mock object
```

---

# Basic Syntax

```java
when(mock.method())
        .thenReturn(value);
```

---

# Example

```java
when(repository.findById(1L))
        .thenReturn(Optional.of(user));
```

Meaning:

```text
If findById(1L) called
Return user
```

---

# Complete Example

```java
@Test
void shouldReturnUser() {

    User user = new User(1L,"John");

    when(repository.findById(1L))
            .thenReturn(Optional.of(user));

    User result = service.getUser(1L);

    assertThat(result.getName())
            .isEqualTo("John");
}
```

---

# Returning Different Values

```java
when(repository.count())
        .thenReturn(1L)
        .thenReturn(2L)
        .thenReturn(3L);
```

Calls return:

```text
1
2
3
```

---

# Throwing Exceptions

```java
when(repository.findById(1L))
        .thenThrow(
            new RuntimeException()
        );
```

---

# Testing Exceptions

```java
assertThrows(
    RuntimeException.class,
    () -> service.getUser(1L)
);
```

---

# doReturn()

Useful for spies.

```java
doReturn(user)
        .when(repository)
        .findById(1L);
```

---

# doThrow()

```java
doThrow(RuntimeException.class)
        .when(repository)
        .deleteById(1L);
```

---

# 8. Verifying Mocks

Mockito can verify interactions.

Question:

```text
Was method called?
How many times?
With which arguments?
```

---

# Basic Verification

```java
verify(repository)
        .findById(1L);
```

Checks invocation.

---

# Example

```java
service.getUser(1L);

verify(repository)
        .findById(1L);
```

Passes if method was called.

---

# Verify Number of Calls

---

## Times

```java
verify(repository, times(2))
        .findById(1L);
```

Must be called twice.

---

## Never

```java
verify(repository, never())
        .save(any());
```

Must never be called.

---

## At Least Once

```java
verify(repository, atLeastOnce())
        .save(any());
```

---

## At Least

```java
verify(repository, atLeast(2))
        .save(any());
```

---

## At Most

```java
verify(repository, atMost(3))
        .save(any());
```

---

# Verify No Interactions

```java
verifyNoInteractions(repository);
```

No method should be called.

---

# Verify No More Interactions

```java
verifyNoMoreInteractions(repository);
```

Useful for strict verification.

---

# 9. Argument Matchers

Used when exact values are unknown.

---

## any()

```java
when(repository.save(any()))
        .thenReturn(user);
```

Matches any object.

---

## anyString()

```java
anyString()
```

---

## anyLong()

```java
anyLong()
```

---

## eq()

```java
verify(repository)
        .findById(eq(1L));
```

Exact match.

---

# Common Matchers

```java
any()
anyString()
anyInt()
anyLong()
eq()
isNull()
isNotNull()
```

---

# Example

```java
verify(repository)
        .save(any(User.class));
```

---

# 10. ArgumentCaptor

One of the most important Mockito features.

Used to capture actual argument values passed to a method.

---

# Problem

Suppose:

```java
service.createUser("John");
```

Internally:

```java
repository.save(user);
```

How do we verify:

```text
user.name == John
```

Answer:

```java
ArgumentCaptor
```

---

# Example

```java
ArgumentCaptor<User> captor =
        ArgumentCaptor.forClass(
                User.class
        );
```

---

# Capture Argument

```java
verify(repository)
        .save(captor.capture());
```

---

# Get Captured Value

```java
User capturedUser =
        captor.getValue();
```

---

# Complete Example

```java
@Test
void shouldCaptureUser() {

    service.createUser("John");

    verify(repository)
            .save(captor.capture());

    User user = captor.getValue();

    assertThat(user.getName())
            .isEqualTo("John");
}
```

---

# Flow

```text
Call Service
      ↓
Service Creates User
      ↓
Repository.save(user)
      ↓
Captor Captures User
      ↓
Assertions
```

---

# Multiple Captured Values

```java
captor.getAllValues();
```

Useful when method called multiple times.

---

# 11. Common Mockito Methods

| Method         | Purpose            |
| -------------- | ------------------ |
| mock()         | Create mock        |
| when()         | Stub behavior      |
| thenReturn()   | Return value       |
| thenThrow()    | Throw exception    |
| verify()       | Verify interaction |
| times()        | Verify count       |
| never()        | Verify no calls    |
| any()          | Match any argument |
| eq()           | Exact argument     |
| ArgumentCaptor | Capture argument   |

---

# 12. Mock vs Stub vs Spy

| Feature          | Mock | Stub       | Spy     |
| ---------------- | ---- | ---------- | ------- |
| Fake Object      | Yes  | Yes        | Partial |
| Real Methods Run | No   | No         | Yes     |
| Verification     | Yes  | Usually No | Yes     |
| Most Common      | Yes  | Yes        | Rare    |

---

# Example Spy

```java
List<String> list =
        spy(new ArrayList<>());
```

Real methods work.

```java
list.add("A");
```

Actually adds value.

---

# 13. Mockito Testing Flow

Standard Mockito workflow:

```text
Arrange
      ↓
Create Mock
      ↓
Stub Behavior
      ↓
Act
      ↓
Call Method
      ↓
Assert
      ↓
Verify Interaction
```

---

# Complete Example

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserService service;

    @Test
    void shouldReturnUser() {

        User user =
                new User(1L,"John");

        when(repository.findById(1L))
                .thenReturn(Optional.of(user));

        User result =
                service.getUser(1L);

        assertThat(result.getName())
                .isEqualTo("John");

        verify(repository)
                .findById(1L);
    }
}
```

---

# 14. Best Practices

---

## Mock Only Dependencies

Good:

```java
@Mock
UserRepository repository;
```

Bad:

```java
@Mock
UserService service;
```

Class under test should not be mocked.

---

## Verify Important Interactions

Verify:

```java
save()
delete()
sendEmail()
publishEvent()
```

---

## Use ArgumentCaptor Carefully

Use only when argument inspection is needed.

---

## Keep Tests Readable

Follow:

```text
Arrange
Act
Assert
```

---

# 15. Common Mistakes

---

## Forgetting MockitoExtension

```java
@ExtendWith(MockitoExtension.class)
```

Without it:

```java
@Mock
```

won't initialize.

---

## Overusing verify()

Verify meaningful interactions only.

---

## Mocking Everything

Do not mock:

```java
String
List
DTO
```

Only dependencies.

---

## Mixing Unit and Integration Testing

Mockito tests should not connect to:

```text
Database
Network
File System
```

---

# 16. Quick Revision Sheet

```text
@Mock
--------
Creates Fake Dependency

@InjectMocks
------------
Injects Mocks

when()
-------
Stub Behavior

thenReturn()
------------
Return Value

thenThrow()
-----------
Throw Exception

verify()
--------
Verify Interaction

ArgumentCaptor
--------------
Capture Arguments

any()
-----
Any Value

eq()
----
Exact Value
```

---

# 17. Interview Questions and Answers

---

## Q1. What is Mockito?

### Answer

Mockito is a Java mocking framework used to create mock objects for unit testing.

---

## Q2. Why do we use Mockito?

### Answer

To isolate the class under test by replacing external dependencies with fake objects.

---

## Q3. What is a Mock?

### Answer

A mock is a fake implementation of a dependency created by Mockito.

---

## Q4. Difference between @Mock and @InjectMocks?

### Answer

@Mock:

Creates fake dependency.

@InjectMocks:

Injects mocks into the class being tested.

---

## Q5. What is stubbing?

### Answer

Defining behavior of a mock object.

Example:

```java
when(repository.findById(1L))
        .thenReturn(user);
```

---

## Q6. What is verification?

### Answer

Checking whether a method was called and how many times it was called.

---

## Q7. How do you verify method invocation?

### Answer

```java
verify(repository)
        .save(user);
```

---

## Q8. What is ArgumentCaptor?

### Answer

A Mockito utility used to capture method arguments passed to a mock.

---

## Q9. What is the purpose of any() matcher?

### Answer

Matches any argument of a compatible type.

---

## Q10. Difference between any() and eq()?

### Answer

any():

Matches any value.

eq():

Matches exact value.

---

## Q11. What is @Spy?

### Answer

Creates a partial mock where real methods execute unless stubbed.

---

## Q12. What happens if you forget MockitoExtension?

### Answer

Mocks are not initialized and tests fail with NullPointerException.

---

## Q13. What is verifyNoInteractions()?

### Answer

Verifies that no methods were invoked on a mock.

---

## Q14. What is the Mockito testing workflow?

### Answer

```text
Create Mock
      ↓
Stub Behavior
      ↓
Call Method
      ↓
Assert Result
      ↓
Verify Interaction
```

---

## Q15. In Spring Boot service-layer testing, which Mockito annotations are used most?

### Answer

Most commonly:

```java
@ExtendWith(MockitoExtension.class)
@Mock
@InjectMocks
ArgumentCaptor
```

These form the foundation of almost every service-layer unit test in Spring Boot.
