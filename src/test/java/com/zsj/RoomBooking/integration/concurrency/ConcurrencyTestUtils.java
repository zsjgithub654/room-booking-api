package com.zsj.RoomBooking.integration.concurrency;

import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import jakarta.persistence.OptimisticLockException;

/** A utility class for concurrency tests. **/

final class ConcurrencyTestUtils {
    /* as this class only acts as utility, replace default constructor, avoid instantiation */
    private ConcurrencyTestUtils() {
    }

    static void assertOptimisticLockingFailure(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof ObjectOptimisticLockingFailureException
                    || current instanceof StaleObjectStateException
                    || current instanceof OptimisticLockException) {
                return;
            }
            current = current.getCause();
        }
        throw new AssertionError("Expected optimistic locking failure but got: " + failure);
    }
}
