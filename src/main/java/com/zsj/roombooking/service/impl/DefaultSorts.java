package com.zsj.roombooking.service.impl;

import com.zsj.roombooking.model.Occupation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/*
  Provides default sorts for entities when no sort is specified.
 */

import java.util.Comparator;
final class DefaultSorts {
    private static final Sort occupationSort = Sort.by(
            Sort.Order.asc("startTime"),
            Sort.Order.asc("endTime"),
            Sort.Order.asc("id"));
    private static final Sort roomSort = Sort.by(
            Sort.Order.asc("id"),
            Sort.Order.asc("name"));
    private static final Sort userSort = Sort.by(
            Sort.Order.asc("id"),
            Sort.Order.asc("username"));

    private static final Comparator<Occupation> occupationComparator = Comparator
            .comparing(Occupation::getStartTime)
            .thenComparing(Occupation::getEndTime)
            .thenComparing(Occupation::getId, Comparator.nullsLast(Long::compareTo));

    private DefaultSorts() {
    }

    static Pageable addReservationDefaultSort(Pageable pageable) {
        return addDefaultSort(pageable, occupationSort);
    }

    static Pageable addRoomDefaultSort(Pageable pageable) {
        return addDefaultSort(pageable, roomSort);
    }

    static Pageable addUserDefaultSort(Pageable pageable) {
        return addDefaultSort(pageable, userSort);
    }

    static Comparator<Occupation> occupationComparator() {
        return occupationComparator;
    }

    static Sort occupationSort() {
        return occupationSort;
    }

    private static Pageable addDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable.isPaged()) {
            if (pageable.getSort().isSorted()) {
                return pageable;
            }
            /* Pageable is immutable */
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
        }
        return Pageable.unpaged(defaultSort);
    }
}
