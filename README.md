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

#### Data Source
The application expects a PostgreSQL database. By default it connects to a local database named `room_booking`,
as configured in [application.properties](src/main/resources/application.properties):

- `spring.datasource.url=jdbc:postgresql://localhost:5432/room_booking`

The database needs to be created before the application starts. If a different name is used,
you need to override the default url, see [Properties Setting](#properties-setting).

#### Database Credentials
For the application to access the database, database credentials are required. See [Properties Setting](#properties-setting) for ways to provide these:

- `spring.datasource.username`
- `spring.datasource.password`

Example values:

```properties
spring.datasource.username=room_booking
spring.datasource.password=room_booking
```

### Bootstrap Admin

If there is no active admin in the database, the application requires bootstrap admin credentials to create one at startup. See [Properties Setting](#properties-setting) for ways to provide these:

- `app.bootstrap.admin.username`
- `app.bootstrap.admin.password`

Example values:

```properties
app.bootstrap.admin.username=admin
app.bootstrap.admin.password=password1!
```

If an active admin already exists, the bootstrap step will be skipped.

### Start the Application

```powershell
.\mvnw.cmd spring-boot:run
```
### Properties Setting
The properties can be set in multiple ways:
1. Before starting the application, set environment variables in PowerShell:

    ```powershell
    $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/room_booking"
    $env:SPRING_DATASOURCE_USERNAME="room_booking"
    $env:SPRING_DATASOURCE_PASSWORD="room_booking"
    $env:APP_BOOTSTRAP_ADMIN_USERNAME="admin"
    $env:APP_BOOTSTRAP_ADMIN_PASSWORD="password1!"
    .\mvnw.cmd spring-boot:run
    ```

2. Start the application with command-line arguments:

    ```powershell
    .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://localhost:5432/room_booking,--spring.datasource.username=room_booking,--spring.datasource.password=room_booking,--app.bootstrap.admin.username=admin,--app.bootstrap.admin.password=password1!"
    ```

3. Before starting the application, add an external configuration file:

   `./config/application.properties`

   and add the following properties:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/room_booking
    spring.datasource.username=room_booking
    spring.datasource.password=room_booking
    app.bootstrap.admin.username=admin
    app.bootstrap.admin.password=password1!
    ```

4. If running from source code, you can also add the properties directly into [application.properties](src/main/resources/application.properties):

    ```properties
    spring.datasource.url=your-database-url
    spring.datasource.username=room_booking
    spring.datasource.password=room_booking
    app.bootstrap.admin.username=admin
    app.bootstrap.admin.password=password1!
    ```

   Do not commit local credentials back into the repository.

### Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

### Demo Data:

- a ready-to-import demo dump is available at [database/demo.sql](database/demo.sql). For instructions and data summary
check out [database/demo.md](database/demo.md).

## Testing

Tests in [integration](src/test/java/com/zsj/roombooking/integration) are running with testcontainers, which require a docker environment.

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
