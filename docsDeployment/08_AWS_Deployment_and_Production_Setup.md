# AWS Deployment & Production Setup — Complete Notes

> **Project context:** `TestingApp_week7` — a Spring Boot 4.1 + Java 21 application with PostgreSQL, Flyway migrations, Spring Profiles, and AWS deployment (RDS, Elastic Beanstalk, CodePipeline/CodeBuild).

---

# Table of Contents

1. [Overview — From Local Dev to Production](#1-overview--from-local-dev-to-production)
2. [8.1 — Setup Production Database with RDS in AWS](#2-81--setup-production-database-with-rds-in-aws)
3. [8.2 — Spring Profiles: Creating dev & prod Environments](#3-82--spring-profiles-creating-dev--prod-environments)
4. [8.3 — Deploy on Elastic Beanstalk with Load Balancer](#4-83--deploy-on-elastic-beanstalk-with-load-balancer)
5. [8.4 — Setup CI/CD with AWS CodePipeline and CodeBuild](#5-84--setup-cicd-with-aws-codepipeline-and-codebuild)
6. [8.5 — Database Migration in Production using Flyway](#6-85--database-migration-in-production-using-flyway)
7. [End-to-End Deployment Flow (Putting It All Together)](#7-end-to-end-deployment-flow-putting-it-all-together)
8. [Common Production Issues & Troubleshooting](#8-common-production-issues--troubleshooting)
9. [Interview Questions & Answers (Java — 2 YOE)](#9-interview-questions--answers-java--2-yoe)

---

# 1. Overview — From Local Dev to Production

When you develop a Spring Boot app on your laptop, everything runs locally:

- Database: PostgreSQL on `localhost:5432`
- App runs on `http://localhost:8080`
- You manually start/stop the app

**Production** is different. You need:

| Concern | Local (Dev) | Production |
|---------|-------------|------------|
| Database | Local PostgreSQL / H2 | **AWS RDS** (managed PostgreSQL) |
| App hosting | Your machine | **Elastic Beanstalk** (managed servers) |
| Traffic handling | Single user | **Load Balancer** (distributes requests) |
| Configuration | Hardcoded / local props | **Environment variables + Spring Profiles** |
| Schema changes | Hibernate `ddl-auto=update` | **Flyway migrations** (versioned SQL) |
| Deployments | Manual `mvn spring-boot:run` | **CI/CD Pipeline** (automated build & deploy) |

### High-Level Architecture

```
Developer pushes code to GitHub
        │
        ▼
┌───────────────────┐
│  CodePipeline     │  ← Orchestrates the pipeline
│  (Source Stage)   │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  CodeBuild        │  ← Runs buildspec.yml (mvn package)
│  (Build Stage)    │
└────────┬──────────┘
         │  produces JAR artifact
         ▼
┌───────────────────┐
│  Elastic          │  ← Runs your Spring Boot JAR
│  Beanstalk        │  ← Load Balancer distributes traffic
└────────┬──────────┘
         │  JDBC connection
         ▼
┌───────────────────┐
│  AWS RDS          │  ← Managed PostgreSQL database
│  (PostgreSQL)     │  ← Flyway applies migrations on startup
└───────────────────┘
```

---

# 2. 8.1 — Setup Production Database with RDS in AWS

## 2.1 What is AWS RDS?

**RDS (Relational Database Service)** is AWS's managed database service. Instead of installing PostgreSQL on an EC2 server yourself, AWS handles:

- **Provisioning** — creates the database instance
- **Backups** — automated daily snapshots
- **Patching** — OS and engine updates
- **High Availability** — Multi-AZ deployments for failover
- **Scaling** — storage auto-scaling, read replicas

### Why RDS instead of a database on EC2?

| Self-managed DB on EC2 | AWS RDS |
|------------------------|---------|
| You install PostgreSQL | AWS installs it |
| You configure backups | Automated backups |
| You handle failover | Multi-AZ failover |
| You patch security updates | Managed patching |
| You monitor disk space | Storage auto-scaling |

For a 2 YOE developer building production apps, **RDS is the standard choice**.

---

## 2.2 Step-by-Step: Creating an RDS PostgreSQL Instance

### Step 1 — Open RDS in AWS Console

1. Log in to [AWS Console](https://console.aws.amazon.com/)
2. Search for **RDS** → Click **Create database**

### Step 2 — Choose Engine

- **Engine type:** PostgreSQL
- **Version:** PostgreSQL 16.x (or latest stable)
- **Templates:** Free tier (for learning) or Production

### Step 3 — Settings

```
DB instance identifier: testingapp-prod-db
Master username:        postgres
Master password:        <strong-password>   ← store securely!
```

> **Important:** Never commit database passwords to Git. Use environment variables in production.

### Step 4 — Instance Configuration

| Setting | Dev/Learning | Production |
|---------|--------------|------------|
| DB instance class | `db.t3.micro` (free tier) | `db.t3.medium` or higher |
| Storage type | General Purpose SSD (gp3) | gp3 with auto-scaling |
| Allocated storage | 20 GB | 100+ GB with auto-scaling |

### Step 5 — Connectivity (Critical!)

```
VPC:                    Default VPC (or your custom VPC)
Public access:          No   ← NEVER expose DB to internet in production
VPC security group:     Create new → rds-sg
Availability Zone:      No preference
Database port:          5432
```

### Step 6 — Database Authentication

- **Initial database name:** `testingapp_db`

### Step 7 — Create the Database

Click **Create database**. Provisioning takes 5–10 minutes.

---

## 2.3 Security Group Configuration

The RDS security group controls **who can connect** to your database.

### Rule you need:

| Type | Protocol | Port | Source | Description |
|------|----------|------|--------|-------------|
| Inbound | TCP | 5432 | `sg-elastic-beanstalk` | Allow EB app servers |

**Why?** Only your Elastic Beanstalk application servers should reach the database — not the public internet.

### Example scenario

```
Internet User  ──X──►  RDS (port 5432 blocked)
EB App Server  ──✓──►  RDS (port 5432 allowed via security group)
Your Laptop    ──X──►  RDS (blocked in production — use VPN/bastion if needed)
```

---

## 2.4 Connecting Spring Boot to RDS

After RDS is created, AWS gives you an **endpoint**:

```
testingapp-prod-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com
```

### In this project — `application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST_URL}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### Environment variables set in Elastic Beanstalk:

| Variable | Example Value |
|----------|---------------|
| `DB_HOST_URL` | `testingapp-prod-db.xxx.us-east-1.rds.amazonaws.com` |
| `DB_NAME` | `testingapp_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `your-secure-password` |

### Why use `${}` placeholders?

- Passwords never appear in source code or JAR files
- Different values per environment (dev/staging/prod)
- Easy rotation without redeploying code

### JDBC URL breakdown

```
jdbc:postgresql://testingapp-prod-db.xxx.rds.amazonaws.com:5432/testingapp_db
       │              │                                      │        │
    protocol      hostname (RDS endpoint)                  port   database name
```

---

## 2.5 Example: Testing the Connection Locally

Before deploying, you can test RDS connectivity using any PostgreSQL client:

```bash
psql -h testingapp-prod-db.xxx.us-east-1.rds.amazonaws.com \
     -U postgres \
     -d testingapp_db \
     -p 5432
```

Or run Spring Boot locally with prod profile (only if your IP is allowed — not recommended for real prod):

```bash
set DB_HOST_URL=testingapp-prod-db.xxx.rds.amazonaws.com
set DB_NAME=testingapp_db
set DB_USERNAME=postgres
set DB_PASSWORD=your-password

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 2.6 RDS Best Practices (Production)

1. **Never enable public access** on production RDS
2. **Use Secrets Manager** instead of plain environment variables for passwords
3. **Enable automated backups** (default: 7-day retention)
4. **Use Multi-AZ** for high availability in real production
5. **Monitor** with CloudWatch alarms (CPU, storage, connections)
6. **Use `ddl-auto=validate`** — let Flyway manage schema, not Hibernate

---

# 3. 8.2 — Spring Profiles: Creating dev & prod Environments

## 3.1 What is a Spring Profile?

A **Spring Profile** lets you define **environment-specific configuration** without changing code.

Think of it as a switch:

```
Profile = "dev"  → use application-dev.properties, dev beans
Profile = "prod" → use application-prod.properties, prod beans
```

### Real-world analogy

A restaurant has the same kitchen (your app code) but different menus for lunch and dinner (profiles). The kitchen doesn't change — only the configuration does.

---

## 3.2 How Profiles Work in This Project

### File structure

```
src/main/resources/
├── application.properties          ← shared/default config
├── application-dev.properties      ← dev-only overrides
└── application-prod.properties     ← prod-only overrides
```

### `application.properties` (shared config)

```properties
spring.application.name=TestingApp_week7

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.flyway.baseline-on-migrate=true

my.variable=global
```

### `application-dev.properties` (local development)

```properties
spring.jpa.hibernate.ddl-auto=update   ← Hibernate auto-creates/alters tables locally
my.variable=dev
```

> In dev, `ddl-auto=update` is convenient — Hibernate creates tables from your `@Entity` classes automatically. You don't need Flyway running locally (though you can).

### `application-prod.properties` (production)

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST_URL}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate   ← Hibernate only VALIDATES, never modifies schema
spring.flyway.baseline-on-migrate=true

my.variable=prod
```

> In prod, `ddl-auto=validate` means Hibernate checks that entities match the DB schema but **never changes it**. Schema changes are Flyway's job.

---

## 3.3 Activating a Profile

### Method 1 — Command line (local)

```bash
# Windows
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Linux/Mac
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Method 2 — Environment variable

```bash
set SPRING_PROFILES_ACTIVE=prod
java -jar target/TestingApp_week7-0.0.1-SNAPSHOT.jar
```

### Method 3 — `application.properties`

```properties
spring.profiles.active=dev
```

> Avoid hardcoding in `application.properties` for production apps. Use environment variables instead.

### Method 4 — Elastic Beanstalk environment variables

In EB Console → Configuration → Software → Environment properties:

```
SPRING_PROFILES_ACTIVE = prod
```

This is how production deployments activate the `prod` profile automatically.

---

## 3.4 Profile-Specific Beans with `@Profile`

You can create different implementations of the same interface for different environments.

### Interface

```java
public interface DataService {
    String getData();
}
```

### Dev implementation

```java
@Service
@Profile("dev")
public class DataServiceDevImpl implements DataService {
    @Override
    public String getData() {
        return "dev data";
    }
}
```

### Prod implementation

```java
@Service
@Profile("prod")
public class DataServiceProdImpl implements DataService {
    @Override
    public String getData() {
        return "Prod Data";
    }
}
```

### What happens at runtime?

| Active Profile | Bean Loaded | `getData()` returns |
|----------------|-------------|---------------------|
| `dev` | `DataServiceDevImpl` | `"dev data"` |
| `prod` | `DataServiceProdImpl` | `"Prod Data"` |

Spring's IoC container only creates the bean matching the active profile.

### Common `@Profile` patterns

```java
@Profile("dev")           // only in dev
@Profile("prod")           // only in prod
@Profile("!prod")          // everywhere EXCEPT prod
@Profile({"dev", "test"})  // dev OR test
```

---

## 3.5 Property Resolution Order

Spring resolves properties in this order (later overrides earlier):

1. `application.properties`
2. `application-{profile}.properties`
3. Environment variables
4. Command-line arguments

### Example

```
application.properties:       my.variable=global
application-prod.properties:  my.variable=prod
EB env var:                    MY_VARIABLE=overridden

Final value in prod: "overridden"
```

> Environment variables use `UPPER_SNAKE_CASE`. Spring converts `MY_VARIABLE` → `my.variable`.

---

## 3.6 Dev vs Prod Configuration Summary

| Setting | Dev | Prod |
|---------|-----|------|
| Database | Local PostgreSQL | AWS RDS |
| `ddl-auto` | `update` (Hibernate manages schema) | `validate` (Flyway manages schema) |
| Credentials | Hardcoded in properties (local only) | Environment variables |
| SQL logging | `show-sql=true` | `show-sql=false` (recommended) |
| Profile activation | IDE / command line | EB environment variable |
| Flyway | Optional locally | **Required** — runs on every startup |

---

# 4. 8.3 — Deploy on Elastic Beanstalk with Load Balancer

## 4.1 What is AWS Elastic Beanstalk?

**Elastic Beanstalk (EB)** is a Platform-as-a-Service (PaaS) that handles:

- Provisioning EC2 instances
- Deploying your application
- Load balancing
- Auto-scaling
- Health monitoring

You upload a JAR file — AWS handles the infrastructure.

### What EB does vs what you do

| AWS (EB) handles | You handle |
|------------------|------------|
| EC2 instances | Your Spring Boot JAR |
| Load Balancer | Application code |
| Auto Scaling | Environment variables |
| Health checks | Database connection config |
| Platform (Java 21) | `buildspec.yml` for CI/CD |

---

## 4.2 Step-by-Step: Deploy Spring Boot to Elastic Beanstalk

### Step 1 — Build the JAR

```bash
mvn clean package -DskipTests
```

Output: `target/TestingApp_week7-0.0.1-SNAPSHOT.jar`

### Step 2 — Create Elastic Beanstalk Application

1. AWS Console → **Elastic Beanstalk** → **Create application**
2. **Application name:** `TestingApp`
3. **Platform:** Java → **Corretto 21 running on 64bit Amazon Linux 2023**
4. **Application code:** Upload your JAR (or connect to CI/CD later)

### Step 3 — Configure Environment

```
Environment name:     TestingApp-prod-env
Domain:               testingapp-prod.us-east-1.elasticbeanstalk.com
```

### Step 4 — Set Environment Variables

In **Configuration → Software → Environment properties**:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_HOST_URL` | `testingapp-prod-db.xxx.rds.amazonaws.com` |
| `DB_NAME` | `testingapp_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `<your-password>` |
| `SERVER_PORT` | `5000` |

> **Why port 5000?** Elastic Beanstalk's Java platform expects your app on port **5000** by default (not 8080). EB's Nginx reverse proxy forwards port 80 → 5000.

Add to `application-prod.properties` if needed:

```properties
server.port=${SERVER_PORT:5000}
```

### Step 5 — Configure Load Balancer

When you create an EB environment with the **Load balanced** environment type (default for web apps):

```
                    ┌─────────────────┐
  User Request ───► │  Application    │
  (port 80/443)     │  Load Balancer  │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
         EC2 Instance   EC2 Instance   EC2 Instance
         (port 5000)    (port 5000)    (port 5000)
              │              │              │
              └──────────────┴──────────────┘
                             │
                             ▼
                        AWS RDS
```

### What the Load Balancer does

1. **Distributes traffic** across multiple EC2 instances
2. **Health checks** — sends requests to `/` (your `HealthCheckController`)
3. **SSL termination** — handles HTTPS certificates
4. **Auto Scaling** — adds/removes instances based on load

### Health Check in this project

```java
@RestController
public class HealthCheckController {

    @GetMapping("/")
    ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
```

EB's load balancer hits `GET /` every 30 seconds. If it gets `200 OK`, the instance is healthy. If not, EB replaces it.

---

## 4.3 Deployment Methods

### Manual deployment (learning)

1. Build JAR locally
2. EB Console → Upload and Deploy
3. Select JAR → Deploy

### Automated deployment (production — via CI/CD)

CodePipeline builds the JAR → automatically deploys to EB. Covered in section 8.4.

---

## 4.4 Elastic Beanstalk Deployment Lifecycle

```
1. Upload JAR to S3
2. EB downloads JAR to EC2 instance(s)
3. EB stops old application
4. EB starts new application: java -jar app.jar
5. Spring Boot starts → Flyway runs migrations → App ready
6. Load balancer health check passes → traffic routed to new version
7. Old version terminated
```

### Rolling deployment (default)

EB updates instances one at a time:

```
Instance 1: v1 (serving traffic)    Instance 2: v1 (serving traffic)
Instance 1: v2 (deploying...)       Instance 2: v1 (serving traffic)
Instance 1: v2 (healthy ✓)          Instance 2: v1 (serving traffic)
Instance 1: v2 (serving traffic)    Instance 2: v2 (deploying...)
Instance 1: v2 (serving traffic)    Instance 2: v2 (healthy ✓)
```

Zero downtime — users never see an outage.

---

## 4.5 Key EB Configuration Tips

| Setting | Recommendation |
|---------|----------------|
| Instance type | `t3.small` minimum for Spring Boot |
| Min instances | 1 (dev) / 2+ (prod) |
| Max instances | 4 (adjust based on traffic) |
| Health check URL | `/` |
| Health check grace period | 300 seconds (Spring Boot needs startup time) |
| Rolling update | Enabled (zero downtime) |
| Log streaming | Enable CloudWatch Logs |

---

# 5. 8.4 — Setup CI/CD with AWS CodePipeline and CodeBuild

## 5.1 What is CI/CD?

| Term | Meaning | Example |
|------|---------|---------|
| **CI** (Continuous Integration) | Automatically build & test on every code push | Push to GitHub → Maven builds & runs tests |
| **CD** (Continuous Deployment) | Automatically deploy after successful build | Build passes → JAR deployed to EB |

### Without CI/CD

```
Developer → manually builds JAR → manually uploads to EB → hopes nothing breaks
```

Problems: human error, inconsistent builds, slow releases, no automated testing.

### With CI/CD

```
Developer → git push → Pipeline automatically builds, tests, deploys
```

Benefits: fast, repeatable, reliable, auditable deployments.

---

## 5.2 AWS CI/CD Services

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ CodePipeline│────►│  CodeBuild  │────►│  CodeDeploy │
│ (Orchestrator)    │  (Build/Test)│     │  (Deploy)   │
└─────────────┘     └─────────────┘     └─────────────┘
       │                                         │
       │                                         ▼
  GitHub/CodeCommit                    Elastic Beanstalk
  (Source)                             (Target)
```

| Service | Role |
|---------|------|
| **CodePipeline** | Orchestrates the entire pipeline (source → build → deploy) |
| **CodeBuild** | Compiles code, runs tests, creates artifacts (JAR) |
| **CodeDeploy** | Deploys artifacts to EB (often integrated directly with EB) |
| **S3** | Stores build artifacts between stages |

---

## 5.3 The `buildspec.yml` File

This file tells CodeBuild **how to build your project**. It lives in the root of your repository.

### This project's `buildspec.yml`:

```yaml
version: 0.2

phases:
  install:
    runtime-versions:
      java: corretto21
    commands:
      - echo "Installing/Verifying Java version..."
      - java -version
      - chmod +x ./mvnw || true

  pre_build:
    commands:
      - echo "Pre-build phase started..."

  build:
    commands:
      - echo "Building, Testing, and packaging the application..."
      # Option 1: Run all tests & package (recommended for CI)
      # - mvn clean package

      # Option 2: Skip tests (faster, use cautiously)
      - mvn clean package -DskipTests

artifacts:
  files:
    - target/*.jar
  discard-paths: yes

cache:
  paths:
    - '/root/.m2/**/*'
```

### Phase breakdown

| Phase | Purpose | What happens |
|-------|---------|--------------|
| `install` | Setup runtime | Install Java 21 (Corretto) |
| `pre_build` | Preparations | Any setup before building |
| `build` | Compile & test | `mvn clean package` → creates JAR |
| `post_build` | After build | Optional notifications, tagging |
| `artifacts` | Output files | JAR file sent to next pipeline stage |
| `cache` | Speed up builds | Cache Maven dependencies (`.m2` folder) |

### Why cache Maven dependencies?

First build: downloads all dependencies (~2-5 min).
Subsequent builds: uses cached dependencies (~30 sec).

---

## 5.4 Step-by-Step: Creating the CI/CD Pipeline

### Step 1 — Connect Source (GitHub)

1. AWS Console → **CodePipeline** → **Create pipeline**
2. **Pipeline name:** `TestingApp-pipeline`
3. **Source provider:** GitHub (Version 2)
4. Connect your GitHub account → select repository & branch (`main`)

### Step 2 — Create CodeBuild Project

1. **Build provider:** AWS CodeBuild
2. **Project name:** `TestingApp-build`
3. **Environment:**
   - Operating system: Amazon Linux
   - Runtime: Standard
   - Image: `aws/codebuild/amazonlinux-x86_64-standard:5.0`
   - Environment variables: (if needed)
   - **Privileged:** Enable if using Testcontainers in tests

4. **Buildspec:** Use `buildspec.yml` from source code

### Step 3 — Configure Deploy Stage

1. **Deploy provider:** AWS Elastic Beanstalk
2. **Application name:** `TestingApp`
3. **Environment name:** `TestingApp-prod-env`

### Step 4 — Review and Create

Pipeline is ready. Every push to `main` triggers:

```
Push to GitHub
    → CodePipeline detects change
    → CodeBuild runs buildspec.yml
    → JAR artifact stored in S3
    → Elastic Beanstalk deploys new JAR
    → Health check passes
    → Deployment complete ✓
```

---

## 5.5 Pipeline Stages Visualized

```
Stage 1: SOURCE                Stage 2: BUILD               Stage 3: DEPLOY
┌─────────────────┐           ┌─────────────────┐           ┌─────────────────┐
│  GitHub          │           │  CodeBuild       │           │  Elastic         │
│  (main branch)   │──────────►│  mvn package     │──────────►│  Beanstalk       │
│                  │           │  → target/*.jar  │           │  → rolling update│
└─────────────────┘           └─────────────────┘           └─────────────────┘
     Trigger:                      Artifact:                     Result:
     git push                      S3 bucket                     Live app updated
```

---

## 5.6 CI/CD Best Practices

1. **Run tests in CI** — uncomment `mvn clean package` (without `-DskipTests`) when Testcontainers is configured
2. **Never commit secrets** — use AWS Secrets Manager or Parameter Store
3. **Use separate pipelines** for dev/staging/prod
4. **Add manual approval stage** before production deployment
5. **Monitor pipeline** with CloudWatch alarms on failures
6. **Keep buildspec.yml in version control** — it's part of your codebase

### Enabling tests with Testcontainers in CodeBuild

If your tests use Testcontainers (Docker containers for PostgreSQL):

1. CodeBuild project → Environment → **Enable Privileged mode**
2. Change buildspec to: `mvn clean package` (without skipTests)

```yaml
build:
  commands:
    - mvn clean package   # runs unit + integration tests with Testcontainers
```

---

# 6. 8.5 — Database Migration in Production using Flyway

## 6.1 The Problem: Managing Database Schema Changes

As your app evolves, your database schema changes:

```
V1: Create employee table
V2: Add department table + FK
V3: Add phone column to employee
V4: Create index on email
...
```

### Bad approaches

| Approach | Problem |
|----------|---------|
| Hibernate `ddl-auto=update` in prod | Unpredictable changes, no audit trail, can cause data loss |
| Manual SQL on production | Error-prone, not repeatable, no version tracking |
| Copy schema from dev | Dev and prod drift apart over time |

### Good approach: Flyway

**Flyway** applies **versioned SQL migration scripts** automatically on application startup.

---

## 6.2 What is Flyway?

Flyway is a database migration tool that:

1. Tracks which migrations have been applied (in a `flyway_schema_history` table)
2. Runs pending migrations in order (V1 → V2 → V3...)
3. Fails fast if a migration has issues
4. Never runs the same migration twice

### How it works on startup

```
Spring Boot starts
    → Flyway checks flyway_schema_history table
    → Finds pending migrations (V3, V4 not yet applied)
    → Runs V3__add_phone.sql
    → Runs V4__add_email_index.sql
    → Records both in flyway_schema_history
    → Hibernate validates entities match schema (ddl-auto=validate)
    → App is ready ✓
```

---

## 6.3 Migration Files in This Project

### Location

```
src/main/resources/db/migration/
├── V1__initialize.sql
└── V2__initialize.sql
```

### Naming convention

```
V{version}__{description}.sql

V1__initialize.sql       ← version 1, description "initialize"
V2__initialize.sql       ← version 2
V3__add_phone_column.sql ← version 3 (future)
```

Rules:
- **`V`** prefix = versioned migration (required)
- **Version number** = must be unique and sequential
- **Double underscore** `__` = separator between version and description
- **Description** = human-readable, use underscores instead of spaces
- **`.sql`** extension

### V1__initialize.sql

```sql
CREATE TABLE employee (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    email VARCHAR(255),
    name VARCHAR(255),
    salary BIGINT,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

ALTER TABLE employee ADD CONSTRAINT uc_employee_email UNIQUE (email);
```

### V2__initialize.sql

```sql
CREATE TABLE department (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(255),
    CONSTRAINT pk_department PRIMARY KEY (id)
);

ALTER TABLE employee ADD department_id BIGINT;

ALTER TABLE employee ADD CONSTRAINT FK_EMPLOYEE_ON_DEPARTMENT
    FOREIGN KEY (department_id) REFERENCES department (id);
```

---

## 6.4 Flyway Configuration

### Maven dependencies (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### Application properties

```properties
# Shared (application.properties)
spring.flyway.baseline-on-migrate=true

# Prod (application-prod.properties)
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.baseline-on-migrate=true
```

### Key properties explained

| Property | Value | Meaning |
|----------|-------|---------|
| `spring.flyway.baseline-on-migrate` | `true` | If DB already has tables but no Flyway history, create baseline instead of failing |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate checks schema matches entities — never modifies it |
| `spring.flyway.locations` | `classpath:db/migration` | Default location for migration scripts |
| `spring.flyway.enabled` | `true` | Default — Flyway runs on startup |

---

## 6.5 The `flyway_schema_history` Table

After Flyway runs, it creates this tracking table:

| installed_rank | version | description | type | script | checksum | installed_on | success |
|----------------|---------|-------------|------|--------|----------|--------------|---------|
| 1 | 1 | initialize | SQL | V1__initialize.sql | 123456789 | 2026-06-26 10:00:00 | true |
| 2 | 2 | initialize | SQL | V2__initialize.sql | 987654321 | 2026-06-26 10:00:01 | true |

- Flyway checks this table on every startup
- Only runs migrations with version > highest applied version
- Checksum ensures migration file hasn't been modified after application

---

## 6.6 Dev vs Prod: Who Manages the Schema?

| Environment | Schema Manager | `ddl-auto` | Flyway |
|-------------|----------------|------------|--------|
| **Dev** | Hibernate (`update`) | `update` | Optional |
| **Prod** | Flyway | `validate` | **Required** |

### Why this split?

- **Dev:** Speed matters. Hibernate auto-creates tables from `@Entity` classes so you iterate fast.
- **Prod:** Safety matters. Every schema change is a reviewed, versioned SQL file with an audit trail.

---

## 6.7 Adding a New Migration (Example Workflow)

Suppose you need to add a `phone` column to `employee`:

### Step 1 — Create migration file

```
src/main/resources/db/migration/V3__add_phone_to_employee.sql
```

```sql
ALTER TABLE employee ADD COLUMN phone VARCHAR(20);
```

### Step 2 — Update Entity class

```java
@Entity
public class Employee {
    // existing fields...
    
    private String phone;  // new field
}
```

### Step 3 — Test locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# Flyway applies V3 locally if using Flyway in dev
```

### Step 4 — Commit and push

```bash
git add src/main/resources/db/migration/V3__add_phone_to_employee.sql
git commit -m "Add phone column to employee table"
git push origin main
```

### Step 5 — CI/CD deploys automatically

```
CodePipeline triggers
    → CodeBuild creates JAR (includes V3 migration)
    → EB deploys JAR
    → Spring Boot starts with prod profile
    → Flyway sees V3 is pending → runs V3__add_phone_to_employee.sql
    → Hibernate validates Employee entity matches schema ✓
    → App serves traffic ✓
```

---

## 6.8 Flyway Rules & Best Practices

### DO

- **Never modify** a migration that has already been applied to any environment
- **Always increment** the version number (V3, V4, V5...)
- **Test migrations** on a staging database before production
- **Keep migrations small** — one logical change per file
- **Use descriptive names** — `V3__add_phone_to_employee.sql` not `V3__update.sql`
- **Back up the database** before deploying migrations to production

### DON'T

- **Never use `ddl-auto=update`** in production
- **Never delete** old migration files
- **Never change version numbers** of existing migrations
- **Never run Flyway migrations manually** on production (let the app do it on startup)

### Fixing a bad migration

If V3 has a bug and hasn't reached production yet:

```
❌ Don't edit V3__add_phone.sql (if already applied anywhere)
✅ Create V4__fix_phone_column.sql with the correction
```

If V3 was already applied to production:

```
✅ Create V4__fix_phone_column.sql
   ALTER TABLE employee ALTER COLUMN phone TYPE VARCHAR(15);
```

---

## 6.9 Flyway + Hibernate: The Complete Production Startup Sequence

```
1. Spring Boot application starts
2. Flyway auto-configuration kicks in
3. Flyway connects to RDS using prod datasource config
4. Flyway reads db/migration/ folder from JAR
5. Flyway checks flyway_schema_history table
6. Pending migrations applied in order (V1, V2, V3...)
7. Hibernate starts with ddl-auto=validate
8. Hibernate compares @Entity classes with actual DB schema
9. If mismatch → Application FAILS to start (this is good — catches bugs early)
10. If match → Application ready, serves traffic
```

---

# 7. End-to-End Deployment Flow (Putting It All Together)

Here is the complete journey from code change to live production:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DEVELOPER WORKFLOW                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Developer writes code + Flyway migration (V3__new_feature.sql) │
│  2. Tests locally with dev profile (ddl-auto=update)               │
│  3. git push origin main                                           │
│                                                                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        CI/CD PIPELINE (AWS)                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  4. CodePipeline detects push to main                               │
│  5. CodeBuild runs buildspec.yml:                                   │
│     - Java 21 (Corretto) installed                                  │
│     - mvn clean package → compiles, tests, packages JAR            │
│     - JAR includes migration files in db/migration/                 │
│  6. JAR artifact uploaded to S3                                     │
│  7. CodeDeploy / EB deploys JAR to EC2 instances                   │
│                                                                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     ELASTIC BEANSTALK (Runtime)                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  8. EB starts: java -jar TestingApp_week7.jar                      │
│  9. SPRING_PROFILES_ACTIVE=prod → loads application-prod.properties│
│ 10. Flyway connects to RDS, applies pending migrations              │
│ 11. Hibernate validates schema (ddl-auto=validate)                  │
│ 12. Spring context starts, @Profile("prod") beans loaded            │
│ 13. App listens on port 5000                                        │
│ 14. Load Balancer health check GET / → 200 OK                      │
│ 15. Traffic routed to new version ✓                                 │
│                                                                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        AWS RDS (Database)                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  - PostgreSQL with employee & department tables                      │
│  - flyway_schema_history tracks applied migrations                  │
│  - Only accessible from EB security group (not public)              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

# 8. Common Production Issues & Troubleshooting

## 8.1 Application fails to start on EB

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Health check failing | App not on port 5000 | Set `SERVER_PORT=5000` |
| `Connection refused` to DB | Security group misconfigured | Allow EB SG → RDS SG on port 5432 |
| `Schema validation failed` | Entity doesn't match DB | Add/fix Flyway migration |
| `Flyway migration failed` | SQL syntax error in migration | Fix SQL, create new migration version |
| Out of memory | Instance too small | Increase EB instance type |

## 8.2 Checking EB Logs

```bash
# Via EB CLI
eb logs

# Or in AWS Console:
# Elastic Beanstalk → Your Environment → Logs → Request Logs → Full Log
```

Look for:
```
Flyway migration successful
Started TestingAppWeek7Application in X seconds
```

Or errors:
```
Schema-validation: missing table [department]
FlywayException: Migration V2__initialize.sql failed
```

## 8.3 Database connection issues

```
Test from EB instance (SSH):
  psql -h <RDS-endpoint> -U postgres -d testingapp_db

Check:
  1. RDS security group allows inbound from EB security group
  2. RDS is in same VPC as EB
  3. Environment variables are set correctly in EB
  4. RDS instance is "Available" (not rebooting)
```

## 8.4 CI/CD pipeline failures

| Stage | Common Failure | Fix |
|-------|---------------|-----|
| Source | GitHub connection expired | Reconnect in CodePipeline |
| Build | Maven compilation error | Fix code, push again |
| Build | Tests failing | Fix tests or check Testcontainers config |
| Deploy | EB health check timeout | Increase grace period to 300s |

---

# 9. Interview Questions & Answers (Java — 2 YOE)

> These questions cover deployment, Spring Boot production concepts, AWS services, and database migrations — typical for a Java developer with ~2 years of experience.

---

## Section A: Spring Boot & Spring Profiles

### Q1. What are Spring Profiles and why are they used?

**Answer:**

Spring Profiles provide a way to segregate parts of the application configuration and make them available only in certain environments. For example, you can have different database configurations for development and production.

```properties
# application-dev.properties
spring.jpa.hibernate.ddl-auto=update

# application-prod.properties
spring.jpa.hibernate.ddl-auto=validate
spring.datasource.url=jdbc:postgresql://${DB_HOST_URL}:5432/${DB_NAME}
```

Activate with: `spring.profiles.active=prod` or environment variable `SPRING_PROFILES_ACTIVE=prod`.

---

### Q2. What is the difference between `@Profile("dev")` and `@ConditionalOnProperty`?

**Answer:**

- `@Profile("dev")` — bean is created only when the `dev` profile is active
- `@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")` — bean is created based on a specific property value, independent of profiles

Profiles are for **environment-level** switching. Conditional properties are for **feature flags**.

---

### Q3. How do you externalize configuration in Spring Boot for production?

**Answer:**

Three main approaches (in order of preference for production):

1. **Environment variables** — set in Elastic Beanstalk, Kubernetes, etc.
2. **External config server** — Spring Cloud Config
3. **AWS Secrets Manager / Parameter Store** — for sensitive values like DB passwords

```properties
spring.datasource.password=${DB_PASSWORD}
```

Never hardcode production credentials in `application.properties`.

---

### Q4. What is the difference between `ddl-auto=update` and `ddl-auto=validate`?

**Answer:**

| Value | Behavior | Use Case |
|-------|----------|----------|
| `update` | Hibernate compares entities with DB and applies changes (add columns, tables) | Local development only |
| `validate` | Hibernate checks schema matches entities — throws exception if mismatch | Production (with Flyway) |
| `create` | Drops and recreates schema on every startup | Testing only |
| `none` | Hibernate does nothing with schema | When using Flyway/Liquibase |

In production, always use `validate` + Flyway for schema management.

---

## Section B: AWS & Deployment

### Q5. What is AWS RDS and why use it instead of a database on EC2?

**Answer:**

AWS RDS (Relational Database Service) is a managed database service. AWS handles provisioning, patching, backups, and failover.

Advantages over self-managed DB on EC2:
- Automated backups and point-in-time recovery
- Multi-AZ deployment for high availability
- Automated software patching
- Monitoring via CloudWatch
- No server administration overhead

---

### Q6. What is Elastic Beanstalk and what problem does it solve?

**Answer:**

Elastic Beanstalk is a PaaS that automatically handles deployment, capacity provisioning, load balancing, and health monitoring. You provide your application (JAR/WAR), and EB manages the underlying infrastructure (EC2, ALB, Auto Scaling).

It solves the problem of manually managing servers, load balancers, and deployment processes.

---

### Q7. Why does Spring Boot on Elastic Beanstalk run on port 5000 instead of 8080?

**Answer:**

Elastic Beanstalk's Java platform uses Nginx as a reverse proxy. Nginx listens on port 80 (HTTP) and forwards requests to your application on port 5000. This is an EB convention — the platform expects your app on port 5000 by default.

Configure with: `server.port=${SERVER_PORT:5000}` or EB environment variable `SERVER_PORT=5000`.

---

### Q8. What is the purpose of a Load Balancer in Elastic Beanstalk?

**Answer:**

The Application Load Balancer (ALB):

1. **Distributes incoming traffic** across multiple EC2 instances
2. **Performs health checks** — routes traffic only to healthy instances
3. **Enables zero-downtime deployments** — during rolling updates, healthy instances continue serving
4. **Terminates SSL/TLS** — handles HTTPS certificates
5. **Enables auto-scaling** — adds instances when load increases

---

### Q9. What is CI/CD and what AWS services are used for it?

**Answer:**

**CI (Continuous Integration):** Automatically build and test code on every commit.
**CD (Continuous Deployment):** Automatically deploy to production after successful build.

AWS services:
- **CodePipeline** — orchestrates the pipeline (source → build → deploy)
- **CodeBuild** — compiles, tests, and packages the application
- **CodeDeploy** — deploys artifacts to target (EB, EC2, Lambda)
- **S3** — stores build artifacts between stages

---

### Q10. What is a `buildspec.yml` file?

**Answer:**

A `buildspec.yml` file is a build specification document that tells AWS CodeBuild how to build, test, and package your application. It defines:

- Runtime versions (Java, Node, etc.)
- Build commands (`mvn clean package`)
- Artifacts to output (JAR files)
- Cache paths (Maven `.m2` directory)

It must be in the root of your repository.

---

## Section C: Database & Flyway

### Q11. What is Flyway and why is it used in production?

**Answer:**

Flyway is a database migration tool that manages schema changes using versioned SQL scripts. In production, it:

- Applies migrations automatically on application startup
- Tracks applied migrations in `flyway_schema_history` table
- Ensures schema changes are repeatable, auditable, and version-controlled
- Prevents Hibernate from making unpredictable schema changes

---

### Q12. Explain Flyway migration naming convention.

**Answer:**

```
V{version}__{description}.sql
```

- `V` — prefix indicating a versioned migration
- `{version}` — unique version number (1, 2, 3 or 1.1, 1.2)
- `__` — double underscore separator
- `{description}` — descriptive name with underscores
- `.sql` — SQL file extension

Examples:
```
V1__create_employee_table.sql
V2__add_department_table.sql
V3__add_phone_column_to_employee.sql
```

---

### Q13. What happens if you modify a Flyway migration that was already applied to production?

**Answer:**

Flyway will **fail on startup** because it calculates a checksum of each migration file and stores it in `flyway_schema_history`. If the file is modified after being applied, the checksum won't match.

**Correct approach:** Never modify applied migrations. Create a new migration file (e.g., `V4__fix_column_type.sql`) with the corrective SQL.

---

### Q14. What is `spring.flyway.baseline-on-migrate=true`?

**Answer:**

When `true`, if Flyway finds a non-empty database (tables exist) but no `flyway_schema_history` table, it creates a baseline instead of throwing an error. This is useful when:

- Adding Flyway to an existing project that already has tables
- The database was created manually or by Hibernate before Flyway was introduced

Flyway baselines at version 1 and only runs migrations with version > 1.

---

### Q15. What is the difference between Flyway and Liquibase?

**Answer:**

Both are database migration tools. Key differences:

| Feature | Flyway | Liquibase |
|---------|--------|-----------|
| Migration format | SQL files | SQL, XML, YAML, JSON |
| Approach | Version-based (V1, V2) | ChangeSet-based |
| Learning curve | Simpler | More complex |
| Spring Boot integration | First-class support | Supported |
| Rollback | Manual (down migrations) | Built-in rollback support |

For most Spring Boot projects, Flyway is the default choice due to simplicity.

---

## Section D: Architecture & Best Practices

### Q16. How do you secure database credentials in a production Spring Boot application?

**Answer:**

1. **Never hardcode** credentials in source code or properties files committed to Git
2. Use **environment variables** injected at runtime (EB environment properties)
3. Use **AWS Secrets Manager** — stores credentials encrypted, rotates automatically
4. Use **Spring Cloud AWS** to fetch secrets at startup
5. Use **IAM database authentication** for RDS (no password needed)

```properties
# application-prod.properties — placeholders only
spring.datasource.url=jdbc:postgresql://${DB_HOST_URL}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

---

### Q17. Explain the deployment strategy "Rolling Deployment" in Elastic Beanstalk.

**Answer:**

Rolling deployment updates instances in batches rather than all at once:

1. Batch 1 instances get the new version → health check → serve traffic
2. Batch 2 instances get the new version → health check → serve traffic
3. Continue until all instances are updated

Benefits:
- **Zero downtime** — old instances serve traffic while new ones deploy
- **Automatic rollback** — if new version fails health check, EB keeps old version
- **Configurable batch size** — e.g., 30% of instances at a time

---

### Q18. What is a health check and why is it important in production?

**Answer:**

A health check is an endpoint (usually `GET /` or `GET /actuator/health`) that returns the application's status. The load balancer periodically calls this endpoint.

- **200 OK** → instance is healthy, receives traffic
- **Non-200 or timeout** → instance is unhealthy, removed from rotation

In this project:

```java
@GetMapping("/")
ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("OK");
}
```

Without health checks, traffic could be routed to a crashed or starting application.

---

### Q19. What happens during a Spring Boot application startup in production with Flyway?

**Answer:**

The startup sequence:

1. Spring Boot loads `application.properties` + `application-prod.properties`
2. DataSource connects to RDS using environment variables
3. **Flyway runs** — checks `flyway_schema_history`, applies pending migrations
4. **Hibernate validates** schema against `@Entity` classes (`ddl-auto=validate`)
5. Spring creates beans (only `@Profile("prod")` beans are loaded)
6. Embedded Tomcat starts on port 5000
7. Application is ready — load balancer health check passes

If any step fails (DB unreachable, migration error, schema mismatch), the application **fails to start** — preventing a broken app from serving traffic.

---

### Q20. How would you handle a failed database migration in production?

**Answer:**

1. **Application won't start** — Flyway fails fast, EB health check fails, old version keeps running (rolling deployment protects you)
2. **Check logs** — `eb logs` or CloudWatch for the exact SQL error
3. **Fix the migration** — create a new version (never modify the failed one if partially applied)
4. **Test on staging** — apply the fix to a staging database first
5. **Backup database** — take an RDS snapshot before retrying
6. **If migration partially applied** — may need manual cleanup:
   ```sql
   -- Check flyway_schema_history
   SELECT * FROM flyway_schema_history;
   
   -- If migration recorded as failed, remove the entry
   DELETE FROM flyway_schema_history WHERE success = false;
   ```
7. **Redeploy** with the fixed migration

---

## Section E: Scenario-Based Questions

### Q21. Your app works locally but fails on Elastic Beanstalk with "Connection refused" to the database. How do you debug?

**Answer:**

Checklist:
1. Verify RDS endpoint is correct in EB environment variables
2. Check RDS security group — must allow inbound on port 5432 from EB's security group
3. Confirm RDS and EB are in the **same VPC**
4. Verify RDS is in "Available" state (not rebooting/modifying)
5. Check if `SPRING_PROFILES_ACTIVE=prod` is set (loads correct datasource config)
6. Test connectivity: SSH into EB instance and run `psql -h <endpoint> -U postgres -d dbname`
7. Check EB logs for the exact connection error message

---

### Q22. A developer accidentally pushed `ddl-auto=create` in the prod profile. What could happen?

**Answer:**

On the next deployment:
1. Hibernate would **drop all existing tables**
2. Recreate tables from `@Entity` classes
3. **All production data would be lost**
4. Flyway's `flyway_schema_history` would also be wiped

Prevention:
- Always use `ddl-auto=validate` in prod
- Code review before merging profile changes
- Use Flyway for all schema changes
- Regular RDS automated backups (point-in-time recovery)

---

### Q23. How do you add a new column to an existing table in production safely?

**Answer:**

1. Create a Flyway migration:
   ```sql
   -- V3__add_phone_to_employee.sql
   ALTER TABLE employee ADD COLUMN phone VARCHAR(20);
   ```
2. Update the `@Entity` class with the new field
3. Test locally and on staging
4. Deploy via CI/CD pipeline
5. Flyway applies V3 on startup automatically

For zero-downtime with large tables:
- Add column as nullable first
- Deploy code that writes to new column
- Backfill data
- Add NOT NULL constraint in a later migration

---

### Q24. What is the difference between horizontal and vertical scaling in the context of this architecture?

**Answer:**

- **Vertical scaling (scale up):** Increase instance size — e.g., EB instance from `t3.small` to `t3.large`, RDS from `db.t3.micro` to `db.t3.medium`. More CPU/RAM on a single machine.

- **Horizontal scaling (scale out):** Add more instances — e.g., EB auto-scaling adds more EC2 instances behind the load balancer, RDS read replicas for read-heavy workloads.

For a Spring Boot web app:
- Horizontal scaling at the app layer (EB auto-scaling) is preferred — load balancer distributes traffic
- Vertical scaling at the database layer (RDS instance class) is common first step before read replicas

---

### Q25. Explain how environment variables work with Spring Boot property placeholders.

**Answer:**

Spring Boot automatically maps environment variables to property placeholders:

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST_URL}:5432/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Environment variable mapping rules:
- `DB_HOST_URL` → `${DB_HOST_URL}`
- `DB_USERNAME` → `${DB_USERNAME}`
- Spring converts dots to underscores and uppercase: `my.variable` → `MY_VARIABLE`

Set in Elastic Beanstalk:
```
DB_HOST_URL = testingapp-prod-db.xxx.rds.amazonaws.com
DB_NAME     = testingapp_db
DB_USERNAME = postgres
DB_PASSWORD = secure-password
```

Spring resolves these at runtime — values never appear in the JAR file.

---

> **End of Notes**
>
> **Related project files:**
> - `src/main/resources/application.properties`
> - `src/main/resources/application-dev.properties`
> - `src/main/resources/application-prod.properties`
> - `src/main/resources/db/migration/V1__initialize.sql`
> - `src/main/resources/db/migration/V2__initialize.sql`
> - `buildspec.yml`
> - `pom.xml`
