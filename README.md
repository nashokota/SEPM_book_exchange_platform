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
- `User`, `Role`, `SellerApplication`, `Category`, `Book`, and `ExchangeRequest` entities
- automatic seeding for roles and one admin user
- buyer registration
- login/logout
- authenticated dashboard
- admin-only protected page
- seller application submission
- admin approval/rejection for seller applications
- admin category management
- seller book listing management foundation
- public book browsing
- public book details page
- public book search by title/author
- public category filter
- public book pagination
- buyer request submission
- seller request approval/rejection flow
- buyer request history
- initial service unit tests


## Current local URLs
- home: `http://localhost:9090/`
- browse books: `http://localhost:9090/books`
- book details: `http://localhost:9090/books/{id}`
- create request: `http://localhost:9090/requests/create?bookId={id}`
- my requests: `http://localhost:9090/requests/my`
- incoming seller requests: `http://localhost:9090/seller/requests`
- login: `http://localhost:9090/login`
- register: `http://localhost:9090/register`
- dashboard: `http://localhost:9090/dashboard`
- admin: `http://localhost:9090/admin`
- apply seller: `http://localhost:9090/seller-applications/apply`
- my seller applications: `http://localhost:9090/seller-applications/mine`
- admin seller approvals: `http://localhost:9090/admin/seller-applications`
- admin categories: `http://localhost:9090/admin/categories`
- seller books: `http://localhost:9090/seller/books`

## Seeded admin account
This account is created automatically on first startup if it does not already exist.

Default local values:
- email: `admin@bookexchange.local`
- password: `Admin@12345`

## Local development bootstrap

### Start PostgreSQL in Docker
```bash
docker compose up -d postgres
```
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

## REST API foundation

Implemented REST controllers:
- `BookRestController`
- `CategoryRestController`
- `ExchangeRequestRestController`

Global REST error handling:
- `GlobalRestExceptionHandler`

### Example API endpoints

#### Books
- `GET /api/books`
- `GET /api/books/{id}`
- `GET /api/seller/books`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

#### Categories
- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

#### Requests
- `POST /api/requests`
- `GET /api/requests/my`
- `GET /api/seller/requests`
- `PATCH /api/seller/requests/{id}/approve`
- `PATCH /api/seller/requests/{id}/reject`
