package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    @DisplayName("유저 포인트 조회 - 성공")
    void getUserPointAPISuccess() throws Exception {
        // given
        pointService.chargeUserPoint(1, 3000); // 초기값

        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(3000));
    }

    @Test
    @DisplayName("유저 포인트 조회 - 실패 (id가 숫자가 아닌 경우)")
    void getUserPointAPIFailWithPath() throws Exception {
        String url = UriComponentsBuilder.fromPath("/point/{id}")
                .buildAndExpand("항해99")
                .encode()
                .toUriString();

        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 포인트 충전 - 성공")
    void getChargePointAPISuccess() throws Exception {
        // given
        pointService.chargeUserPoint(2, 4000); // 초기값

        mockMvc.perform(patch("/point/2/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.point").value(5000));
    }

    @Test
    @DisplayName("유저 포인트 충전 - 실패 (충전 금액이 0이하인 경우)")
    void getChargePointAPIFail() throws Exception {
        mockMvc.perform(patch("/point/3/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-1000")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 포인트 사용 - 성공")
    void getUsePointAPISuccess() throws Exception {
        // given
        pointService.chargeUserPoint(4, 4000); // 초기값

        mockMvc.perform(patch("/point/4/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.point").value(3000));
    }

    @Test
    @DisplayName("유저 포인트 사용 - 실패 (충전 금액이 0이하인 경우)")
    void getUsePointAPIFail() throws Exception {
        mockMvc.perform(patch("/point/5/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-1000")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 포인트 사용 - 실패 (잔액 부족 시 예외 발생)")
    void getUsePointAPIFail2() throws Exception {
        // given
        pointService.chargeUserPoint(6, 5000); // 초기값

        mockMvc.perform(patch("/point/6/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("6000")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("포인트 충전,사용 후 유저 포인트 이력 목록 조회 - 성공")
    void getUserPointHistoriesAPISuccess() throws Exception {

        pointService.chargeUserPoint(7, 3000);
        pointService.chargeUserPoint(7, 2000);
        pointService.useUserPoint(7, 500);

        mockMvc.perform(get("/point/7/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)) // 응답 배열 크기 확인
                .andExpect(jsonPath("$[0].userId").value(7))
                .andExpect(jsonPath("$[0].amount").value(3000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].userId").value(7))
                .andExpect(jsonPath("$[1].amount").value(2000))
                .andExpect(jsonPath("$[1].type").value("CHARGE"))
                .andExpect(jsonPath("$[2].userId").value(7))
                .andExpect(jsonPath("$[2].amount").value(500))
                .andExpect(jsonPath("$[2].type").value("USE"));
    }

}