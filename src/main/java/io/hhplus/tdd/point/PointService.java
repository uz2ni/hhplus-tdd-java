package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> userLockMap = new ConcurrentHashMap<>();

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
     * (공통 메서드) 유저 락 조회
     */
    private Object getUserLock(long userId) {
        return userLocks.computeIfAbsent(userId, k -> new Object());
    }

    /**
     * 특정 유저의 포인트를 충전합니다.
     * 조건 : 충전 금액은 100보다 커야 함
     */
    public UserPoint chargePoint(long userId, long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("충전 금액은 100 이상이어야 합니다.");
        }

        ReentrantLock lock = userLockMap.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();

        try {
            log.debug("포인트 충전 시작 - userId: {}, amount: {}", userId, amount);

            UserPoint currentPoint = userPointTable.selectById(userId);
            long newAmount = currentPoint.point() + amount;

            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedPoint.updateMillis());

            log.debug("포인트 충전 완료 - userId: {}, 이전: {}, 이후: {}",
                    userId, currentPoint.point(), updatedPoint.point());

            return updatedPoint;
        } finally {
            lock.unlock();
            log.debug("락 해제 - userId: {}", userId);
        }

    }

    /**
     * 특정 유저의 포인트를 사용합니다.
     * 조건 : 사용 금액은 100보다 커야 함
     */
    public UserPoint usePoint(long userId, long amount) {
        if (amount < 100) {
            throw new IllegalArgumentException("사용 금액은 100 이상이어야 합니다.");
        }

        ReentrantLock lock = userLockMap.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();

        try {
            log.debug("포인트 사용 시작 - userId: {}, amount: {}", userId, amount);

            UserPoint currentPoint = userPointTable.selectById(userId);

            if (currentPoint.point() < amount) {
                throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
            }

            long newAmount = currentPoint.point() - amount;

            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, newAmount);
            pointHistoryTable.insert(userId, amount, TransactionType.USE, updatedPoint.updateMillis());

            log.debug("포인트 사용 완료 - userId: {}, 이전: {}, 이후: {}",
                    userId, currentPoint.point(), updatedPoint.point());

            return updatedPoint;
        } finally {
            lock.unlock();
            log.debug("락 해제 - userId: {}", userId);
        }

    }
}
