package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for @TimeInterval.
 */
public class TimeIntervalValidator implements ConstraintValidator<TimeInterval, Object> {
    private String fromField;
    private String toField;
    private boolean allowEqual;

    @Override
    public void initialize(TimeInterval constraintAnnotation) {
        fromField = constraintAnnotation.fromField();
        toField = constraintAnnotation.toField();
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
        if (!beanWrapper.isReadableProperty(fromField) || !beanWrapper.isReadableProperty(toField)) {
            throw new IllegalStateException("Invalid @TimeInterval configuration on " + value.getClass().getName());
        }
        Object from = beanWrapper.getPropertyValue(fromField);
        Object to = beanWrapper.getPropertyValue(toField);
        if (from == null && to == null) {
            return true;
        }
        if (from == null || to == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("time interval fields must both be provided or both be omitted.")
                    .addPropertyNode(from == null ? fromField : toField)
                    .addConstraintViolation();
            return false;
        }
        /* from and to are of the same type and comparable */
        if (!from.getClass().isInstance(to) || !(from instanceof Comparable<?> comparableFrom)) {
            throw new IllegalStateException("Fields in @TimeInterval must be comparable and of the same type.");
        }
        /* validate */
        int comparison = ((Comparable<Object>) comparableFrom).compareTo(to);
        boolean valid = allowEqual ? comparison <= 0 : comparison < 0;
        if (valid) {
            return true;
        }
        /* disable default constraint violation, indicate error on specific field */
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(toField)
                .addConstraintViolation();
        return false;
    }
}
