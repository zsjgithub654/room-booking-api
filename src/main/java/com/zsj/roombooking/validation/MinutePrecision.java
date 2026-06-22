package com.zsj.roombooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation indicating the value should use minute precision.
 */
@Documented
/* specify that this annotation is to be used on field, record component or method parameter */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinutePrecisionValidator.class)
public @interface MinutePrecision {
    String message() default "{validation.minute-precision}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
