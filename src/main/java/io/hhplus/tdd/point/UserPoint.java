package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.exception.HanghaeException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint { // record 방식 사용 시 생성자 통해 유효값 검증 추가
        if (id < 0) {
            throw new HanghaeException(ErrorCode.NEGATIVE_ID);
        }
        if (point < 0) {
            throw new HanghaeException(ErrorCode.NEGATIVE_POINT);
        }
    }
}