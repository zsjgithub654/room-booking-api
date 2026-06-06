package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for time range, make sure the end time is greater than the start time, null is not checked here.
 */
public class TimeRangeValidator implements ConstraintValidator<TimeRange, Object> {
    private String startField;
    private String endField;

    @Override
    public void initialize(TimeRange constraintAnnotation) {
        startField = constraintAnnotation.startField();
        endField = constraintAnnotation.endField();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        /* read the properties to validate from the annotated object */
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        if (!beanWrapper.isReadableProperty(startField) || !beanWrapper.isReadableProperty(endField)) {
            throw new IllegalStateException("Invalid @TimeRange configuration on " + value.getClass().getName());
        }
        Object start = beanWrapper.getPropertyValue(startField);
        Object end = beanWrapper.getPropertyValue(endField);
        if (start == null || end == null) {
            return true;
        }
        /* start and end are of the same type and comparable */
        if (!start.getClass().isInstance(end) || !(start instanceof Comparable<?> comparableStart)) {
            throw new IllegalStateException("Fields in @TimeRange must be comparable and of the same type.");
        }
        /* validate */
        boolean valid = ((Comparable<Object>) comparableStart).compareTo(end) < 0;
        if (valid) {
            return true;
        }
        /* disable default constraint violation, indicate error on specific field */
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(endField)
                .addConstraintViolation();
        return false;
    }}
