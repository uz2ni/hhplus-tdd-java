package io.hhplus.tdd.point;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

    public PointHistory {
        if (id < 0) {
            throw new IllegalArgumentException("id는 음수일 수 없습니다.");
        }
        if (userId < 0) {
            throw new IllegalArgumentException("userId는 음수일 수 없습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount는 음수일 수 없습니다.");
        }
    }

}
