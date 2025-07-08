package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;

import static io.hhplus.tdd.point.TransactionType.CHARGE;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    /**
     * 특정 유저의 포인트를 조회
     */
    public UserPoint getUserPoint(long id) {

        UserPoint userPoint = userPointTable.selectById(id);
        return userPoint;

    }

    /**
     * 특정 유저의 포인트를 충전
     */
    public UserPoint chargeUserPoint(long id, long amount) {

        // 예외 처리
        if(amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0 초과이어야 합니다.");
        }

        // 기존 포인트 조회
        UserPoint userPoint = userPointTable.selectById(id);

        // 포인트 이력 추가
        pointHistoryTable.insert(id, amount, CHARGE, System.currentTimeMillis());

        // 최종 포인트 업데이트
        long totalPoint = userPoint.point() + amount;
        userPoint = userPointTable.insertOrUpdate(id, totalPoint);

        return userPoint;
    }
}
