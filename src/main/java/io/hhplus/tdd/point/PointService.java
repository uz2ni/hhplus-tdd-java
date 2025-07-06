package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;

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
        // 기존 포인트 조회
        UserPoint userPoint = userPointTable.selectById(id);
        // 최종 포인트 업데이트
        long totalPoint = userPoint.point() + amount;
        userPoint = userPointTable.insertOrUpdate(id, totalPoint);
        return userPoint;
    }
}
