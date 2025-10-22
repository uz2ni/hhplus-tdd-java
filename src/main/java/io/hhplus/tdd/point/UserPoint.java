package io.hhplus.tdd.point;

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
            throw new IllegalArgumentException("id는 음수일 수 없습니다.");
        }
        if (point < 0) {
            throw new IllegalArgumentException("point는 음수일 수 없습니다.");
        }
    }
}