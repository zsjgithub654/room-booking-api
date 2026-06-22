package com.zsj.roombooking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** for validation on time range duration **/

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxDurationDaysValidator.class)
public @interface MaxDurationDays {
    String message() default "{validation.max-duration-days}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String startField();

    String endField();

    long days();
}
