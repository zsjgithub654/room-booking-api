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

Create the database before starting the application, and provide datasource credentials externally.

Example with environment variables in PowerShell:

```powershell
$env:SPRING_DATASOURCE_USERNAME="room_booking"
$env:SPRING_DATASOURCE_PASSWORD="room_booking"
```

Local-only option:

You can also put the datasource credentials directly into [application.properties](src/main/resources/application.properties) while developing locally:

```properties
spring.datasource.username=room_booking
spring.datasource.password=room_booking
```

Do not commit local credentials back into the repository.

Example with command-line arguments:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.username=room_booking,--spring.datasource.password=room_booking"
```

Example with external configuration file:

`config/application.properties`

```properties
spring.datasource.username=room_booking
spring.datasource.password=room_booking
```

Then run:

```powershell
java -jar target/RoomBooking-0.0.1-SNAPSHOT.jar --spring.config.additional-location=file:./config/
```

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

Local-only option:

You can also add the bootstrap admin values directly into [application.properties](src/main/resources/application.properties) for local development:

```properties
app.bootstrap.admin.username=admin
app.bootstrap.admin.password=password1!
```

Do not commit local credentials back into the repository.

Example with command-line arguments:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.bootstrap.admin.username=admin,--app.bootstrap.admin.password=password1!"
```

Example with external configuration file:

`config/application.properties`

```properties
app.bootstrap.admin.username=admin
app.bootstrap.admin.password=password1!
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
