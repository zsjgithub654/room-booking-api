package com.zsj.RoomBooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation for time intervals.
 * Time interval should have both lower and upper bounds, bounds should be ordered, equality is not allowed by default.
 * Both bounds are null is allowed, not null should be validated by @NotNull if needed.
 * Example use case: time periods.
 */
@Documented
/* specify that this annotation is to be used on class or record */
@Target(ElementType.TYPE)
/* this annotation is to be retained at runtime */
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeIntervalValidator.class)
public @interface TimeInterval {
    /* default message for validation failure */
    String message() default "{validation.time-interval}";

    /* required by bean validation */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /* the fields to validate */
    String fromField();

    String toField();

    /* whether equality between from and to is allowed */
    boolean allowEqual() default false;
}
