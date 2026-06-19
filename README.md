# Room booking API

Room booking REST API built with Spring Boot with role-based access control, room availability search, reservation management, and closure management. It separates request validation from business-rule validation and includes repository, service, controller, and integration tests.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- H2 for repository tests
- Testcontainers for integration tests
- Springdoc OpenAPI / Swagger UI

## Main Features

- User registration and self-service account updates
- Admin user management
- Room create, update, delete, and search
- Reservation create, update, release, and search
- Room closure create, delete, and lookup
- Availability search by name, area, capacity, date range, and unavailable-room inclusion

## Business Rules

- Reservations must stay within room open hours
- Closures block overlapping reservations
- Overlapping reservations are prevented
- Updating a reservation after it starts is rejected
- The last active admin cannot be removed or closed
- Deleted rooms are hidden from normal users
- Passed closures cannot be deleted
- Duplicate usernames are translated into readable application errors

## Running Locally

### Prerequisites

- JDK 17+
- Maven wrapper
- PostgreSQL

### Database

The application expects a PostgreSQL database named `room_booking`.

Default local config in [application.properties](src/main/resources/application.properties):

- URL: `jdbc:postgresql://localhost:5432/room_booking`
- username: `postgres`
- password: `postgres`

Create the database before starting the application.

### Bootstrap Admin

If there is no active admin in the database, the application requires bootstrap admin credentials at startup:

- `app.bootstrap.admin.username`
- `app.bootstrap.admin.password`

Example with environment variables in PowerShell:

```powershell
$env:APP_BOOTSTRAP_ADMIN_USERNAME="admin"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD="password1!"
.\mvnw.cmd spring-boot:run
```

If an active admin already exists, the bootstrap step is skipped.

### Start the Application

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

## Testing

Run the full test suite:

```powershell
.\mvnw.cmd test
```

The project includes:

- repository tests
- service tests
- controller tests
- integration tests
- concurrency tests

## API Overview

Main endpoint groups:

- `/users`
- `/rooms`
- `/reservations`
- `/closures`

Security model:

- public registration for normal users
- authenticated self-service endpoints under `/me`
- admin-only management endpoints
- HTTP Basic authentication in Swagger/local testing

## Project Notes

- Request-shape validation is kept on DTOs and controller parameters
- Business-rule validation is kept in the service layer
- Search endpoints support paging and sorting
- Error responses are normalized through a global exception handler
