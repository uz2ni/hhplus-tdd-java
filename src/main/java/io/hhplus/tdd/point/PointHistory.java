package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.exception.HanghaeException;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public PointHistory {
        if (id < 0) {
            throw new HanghaeException(ErrorCode.NEGATIVE_ID);
        }
        if (userId < 0) {
            throw new HanghaeException(ErrorCode.NEGATIVE_USER_ID);
        }
        if (amount < 0) {
            throw new HanghaeException(ErrorCode.NEGATIVE_AMOUNT);
        }
    }

}
