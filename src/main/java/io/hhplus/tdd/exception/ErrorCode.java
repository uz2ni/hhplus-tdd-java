package io.hhplus.tdd.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 포인트 관련 에러
    INVALID_CHARGE_AMOUNT("H0001", "충전 금액은 100 이상이어야 합니다."),
    INVALID_USE_AMOUNT("H0002", "사용 금액은 100 이상이어야 합니다."),
    INSUFFICIENT_POINT("H0003", "포인트 잔액이 부족합니다."),

    // 도메인 검증 에러
    NEGATIVE_ID("H1001", "id는 음수일 수 없습니다."),
    NEGATIVE_POINT("H1002", "point는 음수일 수 없습니다."),
    NEGATIVE_USER_ID("H1003", "userId는 음수일 수 없습니다."),
    NEGATIVE_AMOUNT("H1004", "amount는 음수일 수 없습니다."),

    // 서버 에러
    SERVER_ERROR("H9999", "서버 에러가 발생했습니다.");

    private final String code;
    private final String message;
}