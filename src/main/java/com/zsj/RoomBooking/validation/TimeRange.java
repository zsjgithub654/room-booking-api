package com.zsj.RoomBooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation indicating the values that should be validated as a time range.
 */

@Documented
/* specify that this annotation is to be used on class or record */
@Target(ElementType.TYPE)
/* this annotation is to be retained at runtime */
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface TimeRange {
    /* default message for validation failure */
    String message() default "start time must be before end time.";

    /* required by bean validation */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /* the fields to validate */
    String startField();

    String endField();
}
