# Book Exchange Platform

Semester project for **Software Engineering and Project Management Lab**.

## Project summary
A full-stack web application where users can browse books, request exchanges or purchases, and apply to become sellers. Admins manage seller approvals, categories, and platform moderation.

## Planned tech stack
- Java 17
- Spring Boot
- Thymeleaf
- Spring Security
- PostgreSQL
- Docker
- GitHub Actions
- Render

## Planned roles
- Admin
- Buyer
- Seller

## Planned core features
- User registration and login
- Role-based authorization
- Seller application and admin approval
- Book listing management
- Search and filter books
- Exchange/purchase request workflow
- REST API for key operations
- Dockerized setup
- CI/CD and cloud deployment

## Planned documentation
- ER diagram
- Architecture diagram
- API endpoint documentation
- Local run instructions
- Docker instructions
- CI/CD explanation

## Team workflow
- `main` is protected
- active development happens through `develop`
- all features use feature branches
- changes go through pull requests with review

## Current implementation status
Implemented so far:
- Spring Boot project bootstrap
- PostgreSQL running in Docker for local development
- `User`, `Role`, and `SellerApplication` entities
- automatic seeding for roles and one admin user
- buyer registration
- login/logout
- authenticated dashboard
- admin-only protected page
- initial service unit tests

## Seeded admin account
This account is created automatically on first startup if it does not already exist.

Default local values:
- email: `admin@bookexchange.local`
- password: `Admin@12345`

## Local development bootstrap

### Start PostgreSQL in Docker
```bash
docker compose up -d postgres
Run the Spring Boot app
On Debian / Git Bash:
./mvnw spring-boot:run
On Windows CMD:
mvnw.cmd spring-boot:run
On Windows PowerShell:
.\mvnw.cmd spring-boot:run
Run tests
On Debian / Git Bash:
./mvnw test
On Windows CMD:
mvnw.cmd test
On Windows PowerShell:
.\mvnw.cmd test
Current local URLs
home: http://localhost:9090/
login: http://localhost:9090/login
register: http://localhost:9090/register
dashboard: http://localhost:9090/dashboard
admin: http://localhost:9090/admin

## CI workflow

GitHub Actions is configured to run the Maven build and tests on:
- pull requests to `develop`
- pull requests to `main`
- pushes to `develop`
- pushes to `main`

Current CI job:
- `build-and-test`

This workflow uses:
- Java 17
- Maven wrapper (`./mvnw`)
- dependency caching for Maven
