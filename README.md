# Room booking API

Room booking REST API for managing users, rooms, reservations, and room closures. It supports availability search, access control, and booking workflows that prevent conflicts.

## Main Features

- User registration and account management
- Admin management for users and rooms
- Room search and availability search
- Reservation lifecycle management
- Room closure management
- Role-based and ownership-based access control
- Conflict handling for overlapping reservations and closures
- Concurrency protection for competing updates

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

## Documentation

- Project structure: [docs/project-structure.md](docs/project-structure.md)
- Domain rules: [docs/domain-rules.md](docs/domain-rules.md)
- Demo data: [database/demo.md](database/demo.md)

## Running Locally

### Prerequisites

- Java 17
- PostgreSQL
- Docker, only if you want to run integration tests

### Database

#### Data Source
The application expects a PostgreSQL database. By default it connects to a local database named `room_booking`,
as configured in [application.properties](src/main/resources/application.properties):

- `spring.datasource.url=jdbc:postgresql://localhost:5432/room_booking`

Make sure the PostgreSQL server is running and the database is created before the application starts.
If a different host, port, or database name is used, you need to override the datasource properties,
see [Properties Setting](#properties-setting).

Example database creation command:

```powershell
psql -U postgres -c "CREATE DATABASE room_booking;"
```

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

On the first run, the Maven wrapper may download Maven and project dependencies.

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

## API And Access Overview

### Endpoint Groups

- `/users`
- `/rooms`
- `/reservations`
- `/closures`

### Access Model

- public registration for normal users
- authenticated self-service endpoints under `/me`
- admin-only management endpoints
- HTTP Basic authentication in Swagger/local testing

## Testing

- Unit and slice tests for repository, service, controller, and security logic
- Integration tests for cross-layer workflows
- Concurrency tests for concurrent access and locking behavior

Run the full test suite:

```powershell
.\mvnw.cmd test
```

Docker is needed for tests in `integration` that run with Testcontainers.