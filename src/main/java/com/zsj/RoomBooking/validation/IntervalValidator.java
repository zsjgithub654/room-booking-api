package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for @Interval.
 */
public class IntervalValidator implements ConstraintValidator<Interval, Object> {
    private String fromField;
    private String toField;

    @Override
    public void initialize(Interval constraintAnnotation) {
        fromField = constraintAnnotation.fromField();
        toField = constraintAnnotation.toField();
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
            throw new IllegalStateException("Invalid @Interval configuration on " + value.getClass().getName());
        }
        Object from = beanWrapper.getPropertyValue(fromField);
        Object to = beanWrapper.getPropertyValue(toField);
        if (from == null && to == null) {
            return true;
        }
        if (from == null || to == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("interval fields must both be provided or both be omitted.")
                    .addPropertyNode(from == null ? fromField : toField)
                    .addConstraintViolation();
            return false;
        }
        /* from and to are of the same type and comparable */
        if (!from.getClass().isInstance(to) || !(from instanceof Comparable<?> comparableFrom)) {
            throw new IllegalStateException("Fields in @Interval must be comparable and of the same type.");
        }
        /* validate */
        int comparison = ((Comparable<Object>) comparableFrom).compareTo(to);
        boolean valid = comparison < 0;
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
