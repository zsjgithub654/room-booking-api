package com.zsj.roombooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

/**
 * Validator for minute precision, make sure seconds and nanoseconds are zero.
 */
public class MinutePrecisionValidator implements ConstraintValidator<MinutePrecision, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.getSecond() == 0 && value.getNano() == 0;
    }
}
