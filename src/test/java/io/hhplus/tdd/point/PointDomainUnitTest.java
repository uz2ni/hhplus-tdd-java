package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("UserPoint, UserPointHistory 도메인 단위 테스트")
public class PointDomainUnitTest { // Mock&Stub 없이 실제 객체 사용

    @Test
    @DisplayName("userId가 양수이면 UserPoint 생성이 성공한다")
    void userId_success() {
        // given
        long userId = 10L;
        long amount = 1000L;

        // when
        UserPoint result = new UserPoint(userId, amount, System.currentTimeMillis());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("userId가 음수이면 UserPoint 생성 시 예외가 발생한다")
    void userPoint_failWithNegativeUserId() {
        // given
        long userId = -10L;
        long amount = 1000L;

        // when & then
        assertThatThrownBy(() -> new UserPoint(userId, amount, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("UserPointHistory 생성이 성공한다")
    void userPointHistory_success() {
        // given
        long id = 1L;
        long userId = 10L;
        long amount = 500L;

        // when
        PointHistory history = new PointHistory(id, userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        // then
        assertThat(history.id()).isEqualTo(id);
        assertThat(history.userId()).isEqualTo(userId);
        assertThat(history.amount()).isEqualTo(amount);
        assertThat(history.type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("id가 음수이면 UserPointHistory 생성 시 예외가 발생한다")
    void userPointHistory_failWithNegativeId() {
        // given
        long id = -1L;
        long userId = 10L;
        long amount = 500L;

        // when & then
        assertThatThrownBy(() -> new PointHistory(id, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("userId가 음수이면 UserPointHistory 생성 시 예외가 발생한다")
    void userPointHistory_failWithNegativeUserId() {
        // given
        long id = 1L;
        long userId = -10L;
        long amount = 500L;

        // when & then
        assertThatThrownBy(() -> new PointHistory(id, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("amount가 음수이면 UserPointHistory 생성 시 예외가 발생한다")
    void userPointHistory_failWithNegativeAmount() {
        // given
        long id = 1L;
        long userId = 10L;
        long amount = -500L;

        // when
        assertThatThrownBy(() -> new PointHistory(id, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount는 음수일 수 없습니다");
    }


}
