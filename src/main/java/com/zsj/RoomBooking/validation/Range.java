package com.zsj.RoomBooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * A custom validation annotation indicating the values that should be validated as a range.
 */
@Documented
/* specify that this annotation is to be used on class or record */
@Target(ElementType.TYPE)
/* this annotation is to be retained at runtime */
@Retention(RetentionPolicy.RUNTIME)
/* allow multiple usage on single class */
@Repeatable(Ranges.class)
@Constraint(validatedBy = RangeValidator.class)
public @interface Range {
    /* default message for validation failure */
    String message() default "range from value must be smaller than range to value.";

    /* required by bean validation */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /* the fields to validate */
    String fromField();

    String toField();

    /* whether equality between from and to is allowed */
    boolean allowEqual() default false;
}
