package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for @TimeInterval.
 */
public class TimeIntervalValidator implements ConstraintValidator<TimeInterval, Object> {
    private String startField;
    private String endField;
    private boolean allowEqual;

    @Override
    public void initialize(TimeInterval constraintAnnotation) {
        startField = constraintAnnotation.startField();
        endField = constraintAnnotation.endField();
        allowEqual = constraintAnnotation.allowEqual();
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
            throw new IllegalStateException("Invalid @TimeInterval configuration on " + value.getClass().getName());
        }
        Object start = beanWrapper.getPropertyValue(startField);
        Object end = beanWrapper.getPropertyValue(endField);
        if (start == null && end == null) {
            return true;
        }
        if (start == null || end == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("time interval fields must both be provided or both be omitted.")
                    .addPropertyNode(start == null ? startField : endField)
                    .addConstraintViolation();
            return false;
        }
        /* start and end are of the same type and comparable */
        if (!start.getClass().isInstance(end) || !(start instanceof Comparable<?> comparableStart)) {
            throw new IllegalStateException("Fields in @TimeInterval must be comparable and of the same type.");
        }
        /* validate */
        int comparison = ((Comparable<Object>) comparableStart).compareTo(end);
        boolean valid = allowEqual ? comparison <= 0 : comparison < 0;
        if (valid) {
            return true;
        }
        /* disable default constraint violation, indicate error on specific field */
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(endField)
                .addConstraintViolation();
        return false;
    }
}
