# Project Structure

## Runtime Path

Typical request path:

`security -> controller -> validation -> service -> repository -> mapping -> response/error handling`

## Main Components

### Controller Layer
- endpoints: `controller`
- request DTOs: `model.dto.request`
- response DTOs: `model.dto.response`
- request validation:
  - DTO validation
  - controller method parameter validation
  - custom validation rules: `validation`
- controller tests: `src/test/java/.../controller`

### Service Layer
- service contracts: `service`
- business-rule implementation: `service.impl`
- service tests: `src/test/java/.../service`

### Persistence Layer
- repositories and specifications: `repository`
- repository tests: `src/test/java/.../repository`

This layer contains entity persistence plus filtering and search query logic.

### Domain Model
- entities: `model.entity`
- enums and shared domain types: `model`
- search criteria: `model.criteria`
- service result models: `model.result`
- domain rules and behavior notes: `docs/domain-rules.md`

Main entities:
- `User`
- `Room`
- `Reservation`
- `Closure`

### Mapping
- DTO, entity, and service-result conversions: `mapper`

This includes both request DTO -> entity mapping and entity/service result -> response DTO mapping.

### Security
- filter chain and security configuration: `config`
- authentication and authorization helpers: `security`
- method-level access rules: controller annotations such as `@PreAuthorize`

### Error Handling
- shared exceptions and global error handling: `exception`

This is where application failures are translated into API error responses.

### Bootstrap
- bootstrap admin initialization and related configuration: `bootstrap`

This handles first-run admin creation when the database has no active admin.
