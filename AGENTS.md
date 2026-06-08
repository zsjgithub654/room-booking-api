# AGENTS.md

## Naming

1. Use lower camel case for variable names.
2. Use explicit names and avoid single-letter naming.
3. Use name start with `is` for methods that return a boolean.

## Comments
1. Keep original comments on refactoring.

## Coding style
1. don't modify input parameters.

## Validation

1. Put request-shape validation on DTOs and controller method parameters.
2. Keep business-rule validation in the service layer.
3. Use `@Valid` for request DTOs and `@Validated` when controller scalar parameters need validation.

## Testing

1. Keep tests grouped by method.
