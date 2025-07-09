package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserPointHistoryTest {

    @Test
    @DisplayName("UserPointHistory 생성 - 성공")
    void UserPointHistoryDomainSuccess() {
        PointHistory history = new PointHistory(1L, 10L, 500L, TransactionType.CHARGE, System.currentTimeMillis());
        assertThat(history.id()).isEqualTo(1L);
        assertThat(history.userId()).isEqualTo(10L);
        assertThat(history.amount()).isEqualTo(500L);
        assertThat(history.type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("UserPointHistory 생성 - 실패 (id 음수)")
    void UserPointHistoryDomainFail() {
        assertThatThrownBy(() -> new PointHistory(-1L, 10L, 500L, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("UserPointHistory 생성 - 실패 (userId 음수)")
    void UserPointHistoryDomainFail2() {
        assertThatThrownBy(() -> new PointHistory(1L, -10L, 500L, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("UserPointHistory 생성 - 실패 (amount 음수)")
    void UserPointHistoryDomainFail3() {
        assertThatThrownBy(() -> new PointHistory(1L, 10L, -500L, TransactionType.CHARGE, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount는 음수일 수 없습니다");
    }

}
