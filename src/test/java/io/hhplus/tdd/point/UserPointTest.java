package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserPointTest {

    @Test
    @DisplayName("UserPoint 생성 - 성공")
    void UserPointDomainSuccess() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
        assertThat(userPoint.id()).isEqualTo(1L);
        assertThat(userPoint.point()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("UserPoint 생성 - 실패 (id 음수)")
    void UserPointDomainFail() {
        assertThatThrownBy(() -> new UserPoint(-1L, 1000L, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("UserPoint 생성 - 실패 (point 음수)")
    void UserPointDomainFail2() {
        assertThatThrownBy(() -> new UserPoint(1L, -1000L, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("point는 음수일 수 없습니다");
    }

}
