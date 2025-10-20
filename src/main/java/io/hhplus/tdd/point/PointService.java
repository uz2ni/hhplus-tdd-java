package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    /**
     * 특정 유저의 포인트를 조회합니다.
     */
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회합니다.
     */
    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 특정 유저의 포인트를 충전합니다.
     * 조건 : 충전 금액은 100보다 커야 함
     */
    public UserPoint chargePoint(long userId, long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("충전 금액은 100 이상이어야 합니다.");
        }

        UserPoint currentPoint = userPointTable.selectById(userId);
        long newAmount = currentPoint.point() + amount;

        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedPoint.updateMillis());

        return updatedPoint;
    }

    /**
     * 특정 유저의 포인트를 사용합니다.
     * 조건 : 사용 금액은 100보다 커야 함
     */
    public UserPoint usePoint(long userId, long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("사용 금액은 100 이상이어야 합니다.");
        }

        UserPoint currentPoint = userPointTable.selectById(userId);

        if (currentPoint.point() < amount) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
        }

        long newAmount = currentPoint.point() - amount;

        UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, updatedPoint.updateMillis());

        return updatedPoint;
    }
}
