# JaCoCo Test Report Generation in Spring Boot

---

# Table of Contents

1. Introduction to Code Coverage
2. What is JaCoCo?
3. Why Code Coverage Matters
4. Benefits of JaCoCo
5. How JaCoCo Works Internally
6. Understanding Coverage Metrics
7. Line Coverage
8. Branch Coverage
9. Method Coverage
10. Class Coverage
11. Cyclomatic Complexity
12. Adding JaCoCo to Maven
13. JaCoCo Maven Plugin Configuration
14. Generating JaCoCo Reports
15. Understanding Report Structure
16. Reading the HTML Report
17. Understanding Coverage Colors
18. Excluding Classes from Coverage
19. Coverage Rules and Build Failures
20. JaCoCo with CI/CD Pipelines
21. Common Coverage Mistakes
22. Best Practices
23. Quick Revision Sheet
24. Interview Questions and Answers

---

# 1. Introduction to Code Coverage

When we run tests, an important question arises:

```text
Are our tests actually testing the code?
```

Example:

```java
public int add(int a, int b) {
    return a + b;
}
```

Suppose we wrote:

```java
@Test
void testAdd() {
    calculator.add(2,3);
}
```

The test executes the method.

But what percentage of the application is tested?

Answer:

```text
Code Coverage
```

---

# What is Code Coverage?

Code Coverage measures how much application code is executed by tests.

Example:

```text
100 Classes
     ↓
80 Classes Tested
     ↓
80% Coverage
```

Coverage helps identify:

* Untested code
* Dead code
* Missing test cases

---

# 2. What is JaCoCo?

JaCoCo stands for:

```text
Java Code Coverage
```

It is the most widely used Java code coverage tool.

Used with:

* Spring Boot
* Maven
* Gradle
* JUnit
* Mockito
* Jenkins
* GitHub Actions

---

# Purpose

JaCoCo tracks:

```text
Which Classes Executed?
Which Methods Executed?
Which Branches Executed?
Which Lines Executed?
```

during test execution.

---

# Example

Suppose:

```java
public int max(int a, int b){

    if(a > b){
        return a;
    }

    return b;
}
```

If tests only execute:

```java
max(10,5);
```

Then:

```text
if branch tested
else branch not tested
```

Coverage will not be 100%.

---

# 3. Why Code Coverage Matters

Without coverage reports:

```text
Tests Pass
```

But we don't know:

```text
How Much Code Was Tested?
```

---

# Real Example

Application:

```text
Controller
Service
Repository
Security
```

Tests only cover:

```text
Controller
```

Result:

```text
Many Untested Areas
```

JaCoCo identifies these gaps.

---

# Benefits

* Detects untested code
* Improves confidence
* Improves maintainability
* Reduces production bugs
* Helps code reviews

---

# 4. Benefits of JaCoCo

---

## Visual Reports

Generates HTML reports.

Example:

```text
Green → Covered
Red   → Not Covered
Yellow→ Partially Covered
```

---

## Easy Integration

Works with:

```text
Maven
Gradle
Jenkins
GitHub Actions
SonarQube
```

---

## Coverage Enforcement

Can fail builds when coverage drops.

Example:

```text
Minimum Coverage
      ↓
80%
```

If coverage becomes:

```text
75%
```

Build fails.

---

## Team Visibility

Developers can easily see:

```text
What is tested?
What is not tested?
```

---

## Supports CI/CD

Coverage automatically generated after every build.

---

# 5. How JaCoCo Works Internally

During test execution:

```text
Compile Code
      ↓
Instrument Bytecode
      ↓
Run Tests
      ↓
Track Executed Lines
      ↓
Generate Coverage Report
```

---

# Internal Flow

```text
JUnit Tests
      ↓
JaCoCo Agent
      ↓
Collect Coverage Data
      ↓
Generate Reports
```

---

# 6. Understanding Coverage Metrics

JaCoCo provides multiple metrics:

```text
Line Coverage
Branch Coverage
Method Coverage
Class Coverage
Complexity Coverage
```

---

# 7. Line Coverage

Measures:

```text
How Many Lines Executed?
```

Example:

```java
public int add(int a, int b){
    return a + b;
}
```

If executed:

```text
100% Line Coverage
```

---

# Formula

```text
Executed Lines
---------------
Total Lines
```

---

# Example

```text
80 Lines Executed
100 Total Lines

Coverage = 80%
```

---

# 8. Branch Coverage

Measures decision paths.

Example:

```java
if(age >= 18){
    return true;
}else{
    return false;
}
```

Two branches:

```text
TRUE
FALSE
```

---

# Test Case 1

```java
isAdult(20);
```

Coverage:

```text
TRUE Covered
FALSE Not Covered
```

Branch Coverage:

```text
50%
```

---

# Test Case 2

```java
isAdult(20);
isAdult(10);
```

Coverage:

```text
100%
```

---

# Why Branch Coverage Matters

Most bugs occur inside:

```text
if
else
switch
loops
```

---

# 9. Method Coverage

Measures:

```text
How Many Methods Were Called?
```

Example:

```java
createUser()
deleteUser()
updateUser()
```

Only:

```java
createUser()
```

executed.

Coverage:

```text
1 / 3 = 33%
```

---

# 10. Class Coverage

Measures:

```text
How Many Classes Were Used?
```

Example:

```text
UserService
OrderService
PaymentService
```

Only:

```text
UserService
```

executed.

Coverage:

```text
33%
```

---

# 11. Cyclomatic Complexity

Measures complexity of code.

More:

```text
if
else
for
while
switch
```

means:

```text
Higher Complexity
```

Higher complexity requires:

```text
More Tests
```

---

# Example

```java
if(a > b){
}
else{
}
```

Two execution paths.

---

# 12. Adding JaCoCo to Maven

---

## Maven Plugin

Add inside:

```xml
<build>
    <plugins>
```

---

# JaCoCo Maven Plugin

(Baeldung's most commonly used setup)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>

    <executions>

        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>

        <execution>
            <id>report</id>

            <phase>test</phase>

            <goals>
                <goal>report</goal>
            </goals>
        </execution>

    </executions>

</plugin>
```

---

# What Each Goal Does

---

## prepare-agent

```text
Inject JaCoCo Agent
```

before tests run.

---

## report

```text
Generate Coverage Report
```

after tests finish.

---

# 13. JaCoCo Maven Plugin Configuration

Build Flow:

```text
mvn test
      ↓
Run Tests
      ↓
Collect Coverage
      ↓
Generate Report
```

---

# Common Commands

Run tests:

```bash
mvn test
```

---

Generate report:

```bash
mvn verify
```

---

Clean build:

```bash
mvn clean verify
```

---

# 14. Generating JaCoCo Reports

After running:

```bash
mvn clean verify
```

Generated folder:

```text
target/
   site/
      jacoco/
```

---

# Open Report

```text
target/site/jacoco/index.html
```

Open in browser.

---

# Report Structure

```text
Project
   ↓
Package
   ↓
Class
   ↓
Method
   ↓
Line Coverage
```

---

# 15. Understanding Report Structure

JaCoCo displays:

```text
Package Coverage
Class Coverage
Method Coverage
Line Coverage
Branch Coverage
```

---

# Example

```text
UserService

Lines:     95%
Branches:  80%
Methods:  100%
```

---

# Interpretation

Methods executed:

```text
Excellent
```

Branches missing:

```text
Need More Tests
```

---

# 16. Reading HTML Report

Open:

```text
target/site/jacoco/index.html
```

You'll see:

```text
Packages
Classes
Coverage %
```

---

# Click Class

Example:

```text
UserService.java
```

Shows actual source code.

---

# 17. Understanding Coverage Colors

---

## Green

```text
Fully Covered
```

---

## Yellow

```text
Partially Covered
```

Usually branch missing.

---

## Red

```text
Not Covered
```

No test executed.

---

# Example

```java
if(age >= 18){
    return true;
}
```

Green:

```text
if executed
```

Yellow:

```text
one branch missing
```

Red:

```text
never executed
```

---

# 18. Excluding Classes from Coverage

Sometimes we exclude:

```text
DTOs
Configuration Classes
Generated Code
```

---

# Example

```xml
<excludes>
    <exclude>
        **/config/**
    </exclude>

    <exclude>
        **/dto/**
    </exclude>
</excludes>
```

---

# 19. Coverage Rules and Build Failures

Minimum Coverage Example:

```xml
<rule>
    <element>BUNDLE</element>

    <limits>

        <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.80</minimum>
        </limit>

    </limits>
</rule>
```

---

# Result

Coverage:

```text
85%
```

Build:

```text
PASS
```

---

Coverage:

```text
70%
```

Build:

```text
FAIL
```

---

# 20. JaCoCo with CI/CD Pipelines

Common pipeline:

```text
Git Push
    ↓
GitHub Actions
    ↓
Run Tests
    ↓
Generate JaCoCo Report
    ↓
Upload Coverage
```

---

# Common Integrations

```text
GitHub Actions
GitLab CI
Jenkins
SonarQube
Azure DevOps
```

---

# SonarQube + JaCoCo

Most enterprise projects use:

```text
JUnit
Mockito
JaCoCo
SonarQube
```

together.

---

# 21. Common Coverage Mistakes

---

## Chasing 100% Coverage

Bad goal.

Example:

```text
100% Coverage
≠
Bug-Free Code
```

---

## Ignoring Branch Coverage

Line coverage alone is not enough.

---

## Testing Getters and Setters Excessively

Adds little value.

Focus on:

```text
Business Logic
```

---

## Ignoring Edge Cases

Example:

```java
null
empty
invalid input
exceptions
```

---

# 22. Best Practices

---

## Target 70-90% Coverage

Typical enterprise recommendation:

```text
70% - 90%
```

---

## Prioritize Critical Logic

Focus on:

```text
Service Layer
Security
Payment Logic
Authentication
```

---

## Review Uncovered Lines

Red lines indicate:

```text
Missing Tests
```

---

## Include Coverage in CI/CD

Generate reports automatically.

---

# 23. Quick Revision Sheet

```text
JaCoCo
------
Java Code Coverage Tool

Main Metrics
------------
Line Coverage
Branch Coverage
Method Coverage
Class Coverage

Commands
--------
mvn test
mvn verify
mvn clean verify

Report Location
---------------
target/site/jacoco/index.html

Colors
------
Green  = Covered
Yellow = Partial
Red    = Not Covered

Most Important
--------------
Branch Coverage
```

---

# 24. Interview Questions and Answers

---

## Q1. What is JaCoCo?

### Answer

JaCoCo is a Java code coverage tool that measures how much application code is executed during tests.

---

## Q2. Why do we use JaCoCo?

### Answer

To identify untested code and measure test coverage.

---

## Q3. What does JaCoCo stand for?

### Answer

Java Code Coverage.

---

## Q4. What coverage metrics does JaCoCo provide?

### Answer

* Line Coverage
* Branch Coverage
* Method Coverage
* Class Coverage
* Complexity Coverage

---

## Q5. What is Line Coverage?

### Answer

Measures how many lines of code were executed by tests.

---

## Q6. What is Branch Coverage?

### Answer

Measures whether all decision paths (if/else, switch, loops) were tested.

---

## Q7. Why is Branch Coverage more valuable than Line Coverage?

### Answer

Because it verifies all logical execution paths, not just executed lines.

---

## Q8. Where is the JaCoCo HTML report generated?

### Answer

```text
target/site/jacoco/index.html
```

---

## Q9. What Maven command generates JaCoCo reports?

### Answer

```bash
mvn clean verify
```

---

## Q10. What does prepare-agent do?

### Answer

It attaches the JaCoCo agent before tests run.

---

## Q11. What does report goal do?

### Answer

Generates coverage reports after tests finish.

---

## Q12. What do Green, Yellow, and Red colors mean?

### Answer

```text
Green  → Covered
Yellow → Partially Covered
Red    → Not Covered
```

---

## Q13. Can JaCoCo fail builds?

### Answer

Yes.

Coverage thresholds can be configured.

---

## Q14. Is 100% coverage necessary?

### Answer

No.

High-quality tests are more important than achieving 100% coverage.

---

## Q15. Which tools are commonly used with JaCoCo?

### Answer

```text
JUnit
Mockito
Maven
Gradle
Jenkins
GitHub Actions
SonarQube
```

These tools together provide testing, coverage analysis, and quality monitoring in modern Spring Boot projects.
