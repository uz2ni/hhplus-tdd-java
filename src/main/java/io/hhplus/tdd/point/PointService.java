package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.exception.HanghaeException;
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
     * 특정 유저의 포인트를 충전합니다.
     * 조건 : 충전 금액은 100보다 커야 함
     */
    public UserPoint chargePoint(long userId, long amount) {
        if (amount < 100) {
            throw new HanghaeException(ErrorCode.INVALID_CHARGE_AMOUNT);
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
            throw new HanghaeException(ErrorCode.INVALID_USE_AMOUNT);
        }

        ReentrantLock lock = userLockMap.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();

        try {
            log.debug("포인트 사용 시작 - userId: {}, amount: {}", userId, amount);

            UserPoint currentPoint = userPointTable.selectById(userId);

            if (currentPoint.point() < amount) {
                throw new HanghaeException(ErrorCode.INSUFFICIENT_POINT);
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
