package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint {
        if (id < 0) {
            throw new IllegalArgumentException("id는 음수일 수 없습니다.");
        }
        if (point < 0) {
            throw new IllegalArgumentException("point는 음수일 수 없습니다.");
        }
    }
}