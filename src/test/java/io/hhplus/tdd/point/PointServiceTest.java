package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // Spring Boot 애플리케이션의 전체 컨텍스트(ApplicationContext) 를 로드함. 모든 빈(@Service, @Repository, @Component 등)을 실제로 주입받아서 테스트
class PointServiceTest {

    @Autowired
    private PointService pointService;

    /**
     * 테스트 간의 데이터 간섭 방지를 위해 포인트 초기화
     */
    @BeforeEach
    void setUp() {
        pointService.chargeUserPoint(1, 3000);  // 미리 3000 포인트 세팅
    }

    @Test
    @DisplayName("유저 포인트 조회 (통합 테스트 ver)")
    void getUserPoint() {

        // Given (생략)

        // When
        UserPoint userPoint = pointService.getUserPoint(1);

        // Then
        assertThat(userPoint.point()).isEqualTo(3000);
    }
}