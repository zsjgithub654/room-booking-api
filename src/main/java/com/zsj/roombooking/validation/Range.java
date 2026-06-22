package com.zsj.roombooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation for a range.
 * Range can have one or two bounds, bounds should be ordered and can be equal.
 * Both bounds are null is allowed, not null should be validated by @NotNull if needed.
 * Example use case: filtering.
 */
@Documented
/* specify that this annotation is to be used on class or record */
@Target(ElementType.TYPE)
/* this annotation is to be retained at runtime */
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangeValidator.class)
public @interface Range {
    /* default message for validation failure */
    String message() default "{validation.range}";

    /* required by bean validation */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /* the fields to validate */
    String minField();

    String maxField();
}
