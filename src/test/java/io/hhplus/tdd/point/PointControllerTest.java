package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트
 */
@SpringBootTest // Spring Boot 애플리케이션의 전체 컨텍스트(ApplicationContext) 를 로드함. 모든 빈(@Service, @Repository, @Component 등)을 실제로 주입받아서 테스트
                // 파라미터 기본값 Mock 사용 (이유: 흐름 테스트가 목적이기 때문에 서버 띄워지는 것은 상관 없어서)
@AutoConfigureMockMvc // 실제 HTTP 요청을 흉내 내는 테스트용 클라이언트로 컨트롤러를 호출. 없으면 @autowired MockMvc 주입 안됨. (이것 안쓰고 TestRestTemplate 주입받아 api 날리는 방법도 있음)
class PointControllerTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private MockMvc mockMvc;


    /**
     * 테스트 간의 데이터 간섭 방지를 위해 포인트 초기화
     */

    @BeforeEach
    void setUp() {
        pointService.chargeUserPoint(1, 3000);  // 미리 3000 포인트 세팅
    }

    /*
    @Test
    @DisplayName("유저 포인트 정상 조회")
    void getUserPointSuccess() {

        // Given (생략)

        // When
        UserPoint userPoint = pointService.getUserPoint(1);

        // Then
        assertThat(userPoint.point()).isEqualTo(3000);
    }
    */

    @Test
    @DisplayName("유저 포인트 조회 API - 정상")
    void getUserPointAPISuccess() throws Exception {
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(3000));
    }

    @Test
    @DisplayName("유저 포인트 조회 API - 실패 (id가 숫자가 아닌 경우)")
    void getUserPointAPIFailWithPath() throws Exception {
        String url = UriComponentsBuilder.fromPath("/point/{id}")
                .buildAndExpand("항해99")
                .encode()
                .toUriString();

        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

}