# Understanding JUnit and AssertJ

---

# Table of Contents

1. Introduction to Testing Frameworks
2. What is JUnit?
3. Why JUnit?
4. JUnit Architecture
5. Common JUnit Annotations
6. Detailed JUnit Annotations
7. Assertions in JUnit
8. What is AssertJ?
9. Why AssertJ?
10. JUnit vs AssertJ
11. Common AssertJ Methods
12. Advanced AssertJ Features
13. Best Practices
14. Common Mistakes
15. Interview Questions and Answers

---

# 1. Introduction to Testing Frameworks

Writing tests manually is difficult and repetitive.

Testing frameworks help developers:

* Write test cases
* Execute tests automatically
* Verify expected behavior
* Generate reports
* Integrate with CI/CD

In Spring Boot, the two most commonly used libraries are:

```text
JUnit 5
+
AssertJ
```

JUnit provides:

* Test execution
* Test lifecycle
* Test annotations

AssertJ provides:

* Fluent assertions
* Better readability
* Rich object validations

---

# 2. What is JUnit?

JUnit is the most popular Java testing framework.

It is used to:

* Create test cases
* Execute tests
* Verify expected results

JUnit is the foundation of Spring Boot testing.

---

## Example

Production Code

```java
public class Calculator {

    public int add(int a, int b){
        return a + b;
    }
}
```

JUnit Test

```java
@Test
void shouldAddNumbers() {

    Calculator calculator = new Calculator();

    int result = calculator.add(2,3);

    assertEquals(5,result);
}
```

---

# 3. Why JUnit?

Without JUnit:

```text
Run Application
Print Values
Manually Verify Output
```

With JUnit:

```text
Run Tests
Automatic Verification
Pass / Fail Result
```

Benefits:

* Automated testing
* Repeatable tests
* Faster development
* CI/CD support
* Easy bug detection

---

# 4. JUnit Architecture

JUnit 5 consists of three modules:

```text
JUnit Platform
        ↓
JUnit Jupiter
        ↓
JUnit Vintage
```

---

## JUnit Platform

Provides infrastructure for launching tests.

Used by:

* IntelliJ
* Maven
* Gradle

---

## JUnit Jupiter

Actual JUnit 5 programming model.

Contains:

* @Test
* @BeforeEach
* Assertions

Most of your work happens here.

---

## JUnit Vintage

Supports old JUnit 3 and JUnit 4 tests.

Used when migrating legacy projects.

---

# 5. Common JUnit Annotations

| Annotation         | Purpose                             |
| ------------------ | ----------------------------------- |
| @Test              | Marks a test method                 |
| @BeforeEach        | Runs before every test              |
| @AfterEach         | Runs after every test               |
| @BeforeAll         | Runs once before all tests          |
| @AfterAll          | Runs once after all tests           |
| @DisplayName       | Custom test name                    |
| @Disabled          | Skip test                           |
| @Nested            | Group tests                         |
| @RepeatedTest      | Repeat test multiple times          |
| @ParameterizedTest | Run same test with different inputs |
| @ValueSource       | Provide parameter values            |
| @CsvSource         | Provide CSV inputs                  |
| @Tag               | Categorize tests                    |

---

# 6. Detailed JUnit Annotations

---

## @Test

Marks a method as a test case.

```java
@Test
void shouldCreateUser() {

}
```

JUnit executes this method automatically.

---

## @BeforeEach

Runs before every test.

```java
@BeforeEach
void setup(){
    System.out.println("Setup");
}
```

Example Flow:

```text
setup()
test1()

setup()
test2()

setup()
test3()
```

Useful for:

* Object initialization
* Mock creation

---

## @AfterEach

Runs after every test.

```java
@AfterEach
void cleanup(){

}
```

Used for:

* Closing resources
* Cleanup operations

---

## @BeforeAll

Runs only once.

```java
@BeforeAll
static void init(){

}
```

Execution:

```text
BeforeAll

Test1
Test2
Test3

AfterAll
```

---

## @AfterAll

Runs once after all tests.

```java
@AfterAll
static void destroy(){

}
```

---

## @DisplayName

Provides readable test names.

```java
@Test
@DisplayName("User should be created successfully")
void createUser(){

}
```

Output:

```text
✓ User should be created successfully
```

---

## @Disabled

Skips a test.

```java
@Disabled
@Test
void oldFeatureTest(){

}
```

Useful for:

* Temporary disabling
* Incomplete functionality

---

## @RepeatedTest

Runs multiple times.

```java
@RepeatedTest(5)
void testFiveTimes(){

}
```

Runs:

```text
1
2
3
4
5
```

---

## @Nested

Groups related tests.

```java
@Nested
class LoginTests {

}
```

Improves organization.

---

## @ParameterizedTest

Runs same test with different inputs.

```java
@ParameterizedTest
@ValueSource(ints = {1,2,3,4})
void shouldBePositive(int number){

}
```

JUnit executes 4 tests.

---

## @CsvSource

Multiple inputs.

```java
@ParameterizedTest
@CsvSource({
    "2,3,5",
    "5,5,10"
})
void testAddition(int a,int b,int expected){

}
```

---

## @Tag

Categorizes tests.

```java
@Tag("unit")
@Test
void userTest(){

}
```

Examples:

```text
unit
integration
security
slow
```

---

# 7. Assertions in JUnit

Assertions verify expected outcomes.

Import:

```java
import static org.junit.jupiter.api.Assertions.*;
```

---

## assertEquals()

```java
assertEquals(5, result);
```

Checks exact equality.

---

## assertNotEquals()

```java
assertNotEquals(10, result);
```

---

## assertTrue()

```java
assertTrue(user.isActive());
```

---

## assertFalse()

```java
assertFalse(user.isDeleted());
```

---

## assertNull()

```java
assertNull(user.getAddress());
```

---

## assertNotNull()

```java
assertNotNull(user);
```

---

## assertThrows()

Verify exceptions.

```java
assertThrows(
    IllegalArgumentException.class,
    () -> service.save(null)
);
```

---

## assertAll()

Multiple assertions together.

```java
assertAll(
    () -> assertEquals("John", user.getName()),
    () -> assertEquals(25, user.getAge())
);
```

---

# 8. What is AssertJ?

AssertJ is a fluent assertion library.

Provides:

* Readable assertions
* Better error messages
* Powerful collection validation

Dependency comes automatically with Spring Boot Starter Test.

---

## Import

```java
import static org.assertj.core.api.Assertions.*;
```

---

## Example

JUnit

```java
assertEquals("John", user.getName());
```

AssertJ

```java
assertThat(user.getName())
        .isEqualTo("John");
```

Much more readable.

---

# 9. Why AssertJ?

Benefits:

* Fluent API
* Better readability
* Rich collection support
* Better exception testing
* Cleaner code

Example:

```java
assertThat(user)
        .isNotNull()
        .extracting(User::getName)
        .isEqualTo("John");
```

---

# 10. JUnit vs AssertJ

| Feature            | JUnit   | AssertJ   |
| ------------------ | ------- | --------- |
| Test Execution     | Yes     | No        |
| Lifecycle Methods  | Yes     | No        |
| Assertions         | Basic   | Advanced  |
| Readability        | Medium  | High      |
| Collection Support | Limited | Excellent |
| Fluent API         | No      | Yes       |
| Error Messages     | Basic   | Detailed  |

---

## Typical Spring Boot Usage

```java
@Test
void shouldCreateUser(){

    User user = service.create();

    assertThat(user)
            .isNotNull()
            .hasFieldOrProperty("id");
}
```

JUnit executes the test.

AssertJ performs assertions.

---

# 11. Common AssertJ Methods

---

## isEqualTo()

```java
assertThat(name)
        .isEqualTo("John");
```

---

## isNotEqualTo()

```java
assertThat(name)
        .isNotEqualTo("Mike");
```

---

## isNull()

```java
assertThat(address)
        .isNull();
```

---

## isNotNull()

```java
assertThat(user)
        .isNotNull();
```

---

## isTrue()

```java
assertThat(active)
        .isTrue();
```

---

## isFalse()

```java
assertThat(active)
        .isFalse();
```

---

## contains()

```java
assertThat("Spring Boot")
        .contains("Spring");
```

---

## startsWith()

```java
assertThat("Spring Boot")
        .startsWith("Spring");
```

---

## endsWith()

```java
assertThat("Spring Boot")
        .endsWith("Boot");
```

---

## hasSize()

```java
assertThat(users)
        .hasSize(3);
```

---

## containsExactly()

```java
assertThat(users)
        .containsExactly("A","B","C");
```

Order matters.

---

## contains()

```java
assertThat(users)
        .contains("A");
```

---

## doesNotContain()

```java
assertThat(users)
        .doesNotContain("X");
```

---

# 12. Advanced AssertJ Features

---

## extracting()

Extract field values.

```java
assertThat(user)
        .extracting(User::getName)
        .isEqualTo("John");
```

---

## filteredOn()

Filter collections.

```java
assertThat(users)
        .filteredOn(User::isActive)
        .hasSize(2);
```

---

## Exception Assertions

```java
assertThatThrownBy(
        () -> service.save(null))
        .isInstanceOf(
            IllegalArgumentException.class
        );
```

---

## Optional Assertions

```java
assertThat(optionalUser)
        .isPresent();
```

---

## Map Assertions

```java
assertThat(map)
        .containsKey("name");
```

---

# 13. Best Practices

---

## Use Meaningful Test Names

Bad

```java
test1()
```

Good

```java
shouldReturnUserWhenIdExists()
```

---

## Follow AAA Pattern

```text
Arrange
Act
Assert
```

---

## One Assertion Concept Per Test

Bad:

Testing multiple unrelated behaviors.

Good:

One responsibility per test.

---

## Prefer AssertJ

Because:

* Cleaner syntax
* Better readability
* Better error messages

---

# 14. Common Mistakes

---

## Forgetting @Test

```java
void shouldCreateUser()
```

JUnit won't execute it.

---

## Testing Multiple Scenarios Together

Bad:

```java
testEverything()
```

---

## Using Real Database in Unit Tests

Unit tests should be isolated.

Use mocks instead.

---

## Weak Test Names

Bad:

```java
test()
```

Good:

```java
shouldThrowExceptionWhenUserNotFound()
```

---

# Quick Revision Sheet

```text
@Test              -> Test Method
@BeforeEach        -> Before Every Test
@AfterEach         -> After Every Test
@BeforeAll         -> Once Before All Tests
@AfterAll          -> Once After All Tests
@ParameterizedTest -> Multiple Inputs
@DisplayName       -> Custom Name
@Disabled          -> Skip Test
```

---

# Interview Questions and Answers

---

## Q1. What is JUnit?

### Answer

JUnit is a Java testing framework used to write and execute automated test cases.

---

## Q2. What is the difference between JUnit 4 and JUnit 5?

### Answer

JUnit 5 introduces:

* Jupiter API
* Better extensions
* Parameterized tests
* Nested tests
* Improved architecture

---

## Q3. What is the purpose of @Test?

### Answer

Marks a method as a test case that JUnit should execute.

---

## Q4. Difference between @BeforeEach and @BeforeAll?

### Answer

@BeforeEach:

Runs before every test.

@BeforeAll:

Runs only once before all tests.

---

## Q5. Why are @BeforeAll methods static?

### Answer

Because JUnit executes them before creating test class instances.

---

## Q6. What is a Parameterized Test?

### Answer

A test that executes multiple times with different inputs.

---

## Q7. What is AssertJ?

### Answer

AssertJ is a fluent assertion library that provides readable and powerful assertions.

---

## Q8. Why is AssertJ preferred over JUnit Assertions?

### Answer

* Better readability
* Fluent syntax
* Rich collection support
* Better error messages

---

## Q9. What is assertThat()?

### Answer

Entry point of AssertJ assertions.

Example:

```java
assertThat(user).isNotNull();
```

---

## Q10. Difference between JUnit and AssertJ?

### Answer

JUnit:

* Executes tests
* Provides lifecycle annotations

AssertJ:

* Provides advanced assertions only

Both are commonly used together.

---

## Q11. How do you test exceptions in JUnit?

### Answer

```java
assertThrows(
    IllegalArgumentException.class,
    () -> service.save(null)
);
```

---

## Q12. How do you test exceptions in AssertJ?

### Answer

```java
assertThatThrownBy(
    () -> service.save(null)
).isInstanceOf(
    IllegalArgumentException.class
);
```

---

## Q13. What is the AAA Pattern?

### Answer

Arrange → Act → Assert

A standard structure for writing clean test cases.

---

## Q14. Which assertion library is included in Spring Boot Starter Test?

### Answer

AssertJ is included by default through:

```xml
spring-boot-starter-test
```

---

## Q15. In a Spring Boot project, do we use JUnit or AssertJ?

### Answer

Both.

JUnit:

* Test execution
* Lifecycle management

AssertJ:

* Assertions and validations
