package com.zsj.RoomBooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for range, make sure the to value is greater than the from value, null is not checked here.
 */
public class RangeValidator implements ConstraintValidator<Range, Object> {
    private String fromField;
    private String toField;
    private boolean allowEqual;

    @Override
    public void initialize(Range constraintAnnotation) {
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
            throw new IllegalStateException("Invalid @Range configuration on " + value.getClass().getName());
        }
        Object from = beanWrapper.getPropertyValue(fromField);
        Object to = beanWrapper.getPropertyValue(toField);
        if (from == null || to == null) {
            return true;
        }
        /* from and to are of the same type and comparable */
        if (!from.getClass().isInstance(to) || !(from instanceof Comparable<?> comparableFrom)) {
            throw new IllegalStateException("Fields in @Range must be comparable and of the same type.");
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
