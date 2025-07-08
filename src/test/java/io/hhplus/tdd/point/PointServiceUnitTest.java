package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @Mock // 가짜 객체 정의. Mock은 아무 기능이 없는 마네킹이다. when~then 을 통해 지시 해주어야 한다.
    private UserPointTable userPointTable;
    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks // 실제 테스트 대상 클래스(진짜 객체). Mock 객체들을 알아서 주입해줌. new PointService(userPointTable)
    private PointService pointService;

    @Test
    @DisplayName("유저 포인트 조회 - 성공")
    void getUserPointSuccess() {
        // given (테스트에 필요한 가짜 객체(userPointTable)의 동작을 정의함)
        long id = 1L;
        UserPoint current = new UserPoint(id, 3000, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(current); // 가짜 객체가 낼 결과를 정의해놓는 것. 아래 when 절 로직에서 selectById(id)를 호출할 때 current 객체를 리턴하게 됨

        // when (테스트 대상 메서드 호출)
        UserPoint result = pointService.getUserPoint(id);

        // then (결과 상태 검증)
        assertThat(result.point()).isEqualTo(3000);
    }

    @Test
    @DisplayName("유저 포인트 충전 - 성공")
    void chargeUserPointSuccess() {
        // given
        long id = 1L;
        long initAmount = 3000L;
        long amount = 1000L;
        long totalAmount = 4000L;

        PointHistory currentInsert = new PointHistory(1234L, id, amount, CHARGE, System.currentTimeMillis());
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(CHARGE), anyLong())).thenReturn(currentInsert);

        UserPoint currentSelect = new UserPoint(id, initAmount, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(currentSelect);

        UserPoint currentInsertOrUpdate = new UserPoint(id, totalAmount, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(id, totalAmount)).thenReturn(currentInsertOrUpdate);

        // when
        UserPoint result = pointService.chargeUserPoint(id, amount);

        // then 1. 포인트 충전 이력이 업데이트 되었는지 확인
        verify(pointHistoryTable).insert(eq(id), eq(amount), eq(CHARGE), anyLong());

        // then 2. 포인트 최종 결과가 n인지 확인
        assertThat(result.point()).isEqualTo(4000);
    }

    @Test
    @DisplayName("유저 포인트 충전 - 실패 (0 이하 금액 예외 발생)")
    void chargeUserPointFail() {
        // given
        long id = 1L;
        long negativeAmount = -1000L;

        // when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(id, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0 초과이어야 합니다.");
    }

    @Test
    @DisplayName("유저 포인트 사용 - 성공")
    void useUserPointSuccess() {
        // given
        long id = 1L;
        long initAmount = 3000L;
        long amount = 1000L;
        long totalAmount = 4000L;

        PointHistory currentInsert = new PointHistory(1234L, id, amount, CHARGE, System.currentTimeMillis());
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(CHARGE), anyLong())).thenReturn(currentInsert);

        UserPoint currentSelect = new UserPoint(id, initAmount, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(currentSelect);

        UserPoint currentInsertOrUpdate = new UserPoint(id, totalAmount, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(id, totalAmount)).thenReturn(currentInsertOrUpdate);

        // when
        UserPoint result = pointService.chargeUserPoint(id, amount);

        // then 1. 포인트 충전 이력이 업데이트 되었는지 확인
        verify(pointHistoryTable).insert(eq(id), eq(amount), eq(CHARGE), anyLong());

        // then 2. 포인트 최종 결과가 n인지 확인
        assertThat(result.point()).isEqualTo(4000);
    }

    @Test
    @DisplayName("유저 포인트 사용 - 실패 (0 이하 금액 예외 발생)")
    void useUserPointFail() {
        // given
        long id = 1L;
        long negativeAmount = -1000L;

        // when & then
        assertThatThrownBy(() -> pointService.chargeUserPoint(id, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0 초과이어야 합니다.");
    }
}