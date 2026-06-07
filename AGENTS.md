# AGENTS.md

## Naming Rules

1. Use lower camel case for variable names.
2. Use explicit names and avoid single-letter naming.

## Validation Rules

1. Put request-shape validation on DTOs and controller method parameters.
2. Keep business-rule validation in the service layer.
3. Use `@Valid` for request DTOs and `@Validated` when controller scalar parameters need validation.

## Testing Rules

1. Keep tests grouped by method.
