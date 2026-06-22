package com.zsj.roombooking.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/* To validate pageable sorting parameters */

public final class PageableValidation {
    private static final String INVALID_SORTING_PARAMETERS = "Invalid sorting parameters.";

    private PageableValidation() {
    }

    public static void validateSort(Pageable pageable, Set<String> allowedProperties) {
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (property.isBlank() || !allowedProperties.contains(property)) {
                throw new IllegalArgumentException(INVALID_SORTING_PARAMETERS);
            }
        }
    }
}
