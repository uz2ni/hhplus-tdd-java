package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.exception.HanghaeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceUnitTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("특정 유저의 포인트를 조회할 수 있다")
    void getUserPoint_Success() {
        // given
        long userId = 1L;
        UserPoint expectedPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(expectedPoint);

        // when
        UserPoint result = pointService.getUserPoint(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(1000L);
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("특정 유저의 포인트 내역을 조회할 수 있다")
    void getPointHistories_Success() {
        // given
        long userId = 1L;
        List<PointHistory> expectedHistories = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 500L, TransactionType.USE, System.currentTimeMillis())
        );
        given(pointHistoryTable.selectAllByUserId(userId)).willReturn(expectedHistories);

        // when
        List<PointHistory> result = pointService.getPointHistories(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName("포인트를 충전할 수 있다")
    void chargePoint_Success() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long chargeAmount = 500L;
        long expectedAmount = 1500L;

        UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(currentPoint);
        given(userPointTable.insertOrUpdate(userId, expectedAmount)).willReturn(updatedPoint);
        given(pointHistoryTable.insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong())) // 매처(eq(),anyLong()) 사용 시에는 모두 매처 구문으로 사용해야 함. anyLong() 사용이 필요해서 eq도 사용하고 있음
                .willReturn(new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(result.point()).isEqualTo(expectedAmount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, expectedAmount);
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("충전 금액이 100 미만/음수 이면 예외가 발생한다")
    void chargePoint_ThrowsException_WhenAmountIsZeroOrNegative() {
        // given
        long userId = 1L;

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(userId, 99))
                .isInstanceOf(HanghaeException.class)
                .hasMessage("충전 금액은 100 이상이어야 합니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CHARGE_AMOUNT);

        assertThatThrownBy(() -> pointService.chargePoint(userId, -100))
                .isInstanceOf(HanghaeException.class)
                .hasMessage("충전 금액은 100 이상이어야 합니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CHARGE_AMOUNT);

        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("포인트를 사용할 수 있다")
    void usePoint_Success() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long useAmount = 300L;
        long expectedAmount = 700L;

        UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, expectedAmount, System.currentTimeMillis());

        given(userPointTable.selectById(userId)).willReturn(currentPoint);
        given(userPointTable.insertOrUpdate(userId, expectedAmount)).willReturn(updatedPoint);
        given(pointHistoryTable.insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong()))
                .willReturn(new PointHistory(1L, userId, useAmount, TransactionType.USE, System.currentTimeMillis()));

        // when
        UserPoint result = pointService.usePoint(userId, useAmount);

        // then
        assertThat(result.point()).isEqualTo(expectedAmount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, expectedAmount);
        verify(pointHistoryTable, times(1)).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("사용 금액이 100 미만이면 예외가 발생한다")
    void usePoint_ThrowsException_WhenAmountIsZeroOrNegative() {
        // given
        long userId = 1L;

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, 99))
                .isInstanceOf(HanghaeException.class)
                .hasMessage("사용 금액은 100 이상이어야 합니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_USE_AMOUNT);

        assertThatThrownBy(() -> pointService.usePoint(userId, -100))
                .isInstanceOf(HanghaeException.class)
                .hasMessage("사용 금액은 100 이상이어야 합니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_USE_AMOUNT);

        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("잔액이 부족하면 포인트 사용 시 예외가 발생한다")
    void usePoint_ThrowsException_WhenInsufficientBalance() {
        // given
        long userId = 1L;
        long currentAmount = 500L;
        long useAmount = 1000L;

        UserPoint currentPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(currentPoint);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
                .isInstanceOf(HanghaeException.class)
                .hasMessage("포인트 잔액이 부족합니다.")
                .extracting("errorCode").isEqualTo(ErrorCode.INSUFFICIENT_POINT);

        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong()); // never() 실행 안되었는지 검증
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong()); // never() 실행 안되었는지 검증
    }
}