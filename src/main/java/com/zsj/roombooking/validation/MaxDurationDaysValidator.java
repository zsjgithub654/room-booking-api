package com.zsj.roombooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        if (!(start instanceof LocalDate startDate) || !(end instanceof LocalDate endDate)) {
            throw new IllegalStateException("Fields in @MaxDurationDays must be LocalDate.");
        }

        boolean valid = ChronoUnit.DAYS.between(startDate, endDate) + 1 <= days;
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
