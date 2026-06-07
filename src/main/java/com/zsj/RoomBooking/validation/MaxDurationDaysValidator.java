package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.time.Duration;
import java.time.LocalDateTime;

public class MaxDurationDaysValidator implements ConstraintValidator<MaxDurationDays, Object> {
    private String startField;
    private String endField;
    private long days;

    @Override
    public void initialize(MaxDurationDays constraintAnnotation) {
        startField = constraintAnnotation.startField();
        endField = constraintAnnotation.endField();
        days = constraintAnnotation.days();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        if (!beanWrapper.isReadableProperty(startField) || !beanWrapper.isReadableProperty(endField)) {
            throw new IllegalStateException("Invalid @MaxDurationDays configuration on " + value.getClass().getName());
        }

        Object start = beanWrapper.getPropertyValue(startField);
        Object end = beanWrapper.getPropertyValue(endField);
        if (start == null || end == null) {
            return true;
        }
        if (!(start instanceof LocalDateTime startTime) || !(end instanceof LocalDateTime endTime)) {
            throw new IllegalStateException("Fields in @MaxDurationDays must be LocalDateTime.");
        }

        boolean valid = Duration.between(startTime, endTime).compareTo(Duration.ofDays(days)) <= 0;
        if (valid) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(endField)
                .addConstraintViolation();
        return false;
    }
}
