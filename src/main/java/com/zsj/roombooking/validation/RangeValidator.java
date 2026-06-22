package com.zsj.roombooking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Validator for @Range.
 */
public class RangeValidator implements ConstraintValidator<Range, Object> {
    private String minField;
    private String maxField;

    @Override
    public void initialize(Range constraintAnnotation) {
        minField = constraintAnnotation.minField();
        maxField = constraintAnnotation.maxField();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        /* read the properties to validate from the annotated object */
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        if (!beanWrapper.isReadableProperty(minField) || !beanWrapper.isReadableProperty(maxField)) {
            throw new IllegalStateException("Invalid @Range configuration on " + value.getClass().getName());
        }
        Object min = beanWrapper.getPropertyValue(minField);
        Object max = beanWrapper.getPropertyValue(maxField);
        if (min == null || max == null) {
            return true;
        }
        /* minimum and maximum are of the same type and comparable */
        if (!min.getClass().isInstance(max) || !(min instanceof Comparable<?> comparableMin)) {
            throw new IllegalStateException("Fields in @Range must be comparable and of the same type.");
        }
        /* validate */
        int comparison = ((Comparable<Object>) comparableMin).compareTo(max);
        boolean valid = comparison <= 0;
        if (valid) {
            return true;
        }
        /* disable default constraint violation, indicate error on specific field */
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(maxField)
                .addConstraintViolation();
        return false;
    }
}
