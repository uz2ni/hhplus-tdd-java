package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Point 통합 테스트")
class PointIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Test
    @DisplayName("포인트 충전 → 사용 → 조회 전체 플로우가 정상 동작한다")
    void fullFlow_ChargeUseAndQuery() {
        // given
        long userId = 100L;
        long chargeAmount = 1000L;
        long useAmount = 300L;

        // when - 충전
        UserPoint chargedPoint = pointService.chargePoint(userId, chargeAmount);

        // then
        assertThat(chargedPoint.point()).isEqualTo(1000L);

        // when - 사용
        UserPoint usedPoint = pointService.usePoint(userId, useAmount);

        // then
        assertThat(usedPoint.point()).isEqualTo(700L);

        // when - 포인트 조회
        UserPoint currentPoint = pointService.getUserPoint(userId);

        // then
        assertThat(currentPoint.point()).isEqualTo(700L);

        // when - 내역 조회
        List<PointHistory> histories = pointService.getPointHistories(userId);

        // then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(0).amount()).isEqualTo(1000L);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(histories.get(1).amount()).isEqualTo(300L);
    }

    @Test
    @DisplayName("여러 번 충전과 사용을 반복해도 정확한 잔액이 유지된다")
    void multipleTransactions_MaintainCorrectBalance() {
        // given
        long userId = 101L;

        // when & then
        pointService.chargePoint(userId, 1000L);
        assertThat(pointService.getUserPoint(userId).point()).isEqualTo(1000L);

        pointService.chargePoint(userId, 500L);
        assertThat(pointService.getUserPoint(userId).point()).isEqualTo(1500L);

        pointService.usePoint(userId, 300L);
        assertThat(pointService.getUserPoint(userId).point()).isEqualTo(1200L);

        pointService.usePoint(userId, 200L);
        assertThat(pointService.getUserPoint(userId).point()).isEqualTo(1000L);

        pointService.chargePoint(userId, 1000L);
        assertThat(pointService.getUserPoint(userId).point()).isEqualTo(2000L);

        // 내역 확인
        List<PointHistory> histories = pointService.getPointHistories(userId);
        assertThat(histories).hasSize(5);
    }

    @Test
    @DisplayName("잔액 부족 시 사용이 실패하고 잔액은 변경되지 않는다")
    void insufficientBalance_NoBalanceChange() {
        // given
        long userId = 102L;
        pointService.chargePoint(userId, 500L);

        // when & then
        assertThatThrownBy(() -> pointService.usePoint(userId, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 잔액이 부족합니다.");

        // 잔액이 변경되지 않았는지 확인
        UserPoint point = pointService.getUserPoint(userId);
        assertThat(point.point()).isEqualTo(500L);

        // 실패한 거래는 히스토리에 기록되지 않음
        List<PointHistory> histories = pointService.getPointHistories(userId);
        assertThat(histories).hasSize(1); // 충전 1건만
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("동시에 여러 사용자가 각자 포인트를 충전해도 정상 처리된다")
    void concurrentCharges_DifferentUsers() throws InterruptedException {
        // given
        int userCount = 10;
        long chargeAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(userCount); // 10개의 스레드로 동시에 작업 실행
        CountDownLatch latch = new CountDownLatch(userCount); // 모든 작업이 끝날 때까지 대기하기 위함
        AtomicInteger successCount = new AtomicInteger(0); // 성공적으로 chargePoint를 실행한 횟수 카운팅. 동시에 여러 스레드가 접근하면 덮어쓰일 수 있어서 thread-safe 한 AtomicInteger 사용

        // when - 10명의 사용자가 동시에 충전
        for (int i = 0; i < userCount; i++) {
            long userId = 200L + i;
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                    successCount.incrementAndGet(); // 성공한 경우 수 증가
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 작업 하나 끝남 (카운트다운 안하면 메인 스레드는 계속 대기중. 끝나지 않아서 테스트 멈추거나 타임아웃 발생)
                }
            });
        }

        latch.await(); // 모든 latch(count=10)가 줄어들 때까지 기다림
        executorService.shutdown(); // 스레드풀 종료

        // then - 모든 사용자의 충전이 성공
        assertThat(successCount.get()).isEqualTo(userCount);

        // 각 사용자의 잔액 확인
        for (int i = 0; i < userCount; i++) {
            long userId = 200L + i;
            UserPoint point = pointService.getUserPoint(userId);
            assertThat(point.point()).isEqualTo(chargeAmount);
        }
    }

    @Test
    @DisplayName("동일 사용자에 대한 동시 충전이 모두 반영된다")
    void concurrentCharges_SameUser() throws InterruptedException {
        // given
        long userId = 300L;
        int requestCount = 10;
        long chargeAmount = 100L;
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 동일 사용자에게 10번 동시 충전
        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(userId, chargeAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - 모든 충전이 성공
        assertThat(successCount.get()).isEqualTo(requestCount);
        System.out.println("successCount: " + successCount.get() + ", requestCount: " + requestCount);
        pointService.getPointHistories(userId).stream().forEach(histories -> {
            System.out.println(histories.id() + " : " + histories.amount());
        });

        // 최종 잔액 확인 (100 * 10 = 1000) -> 동시성 이슈인 부분
        UserPoint point = pointService.getUserPoint(userId);
        assertThat(point.point()).isEqualTo(chargeAmount * requestCount);
        System.out.println("totalPoint: " + point.point());

        // 히스토리 개수 확인
        List<PointHistory> histories = pointService.getPointHistories(userId);
        assertThat(histories).hasSize(requestCount);
        System.out.println("histories size: " + histories.size());

    }

    @Test
    @DisplayName("동일 사용자에 대한 동시 충전과 사용 요청이 정상 처리된다")
    void concurrentChargeAndUse_SameUser() throws InterruptedException {
        // given
        long userId = 400L;
        userPointTable.insertOrUpdate(userId, 10000L); // 초기 잔액 설정

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when - 충전 10번, 사용 10번 동시 실행
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // 짝수: 충전
                        pointService.chargePoint(userId, 100L);
                    } else {
                        // 홀수: 사용
                        pointService.usePoint(userId, 100L);
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - 충전과 사용이 정확히 반영되었는지 확인
        assertThat(successCount.get()).isEqualTo(threadCount);

        // 최종 잔액: 10000 + (100 * 10) - (100 * 10) = 10000
        UserPoint finalPoint = pointService.getUserPoint(userId);
        assertThat(finalPoint.point()).isEqualTo(10000L);

        // 히스토리 확인
        List<PointHistory> histories = pointService.getPointHistories(userId);
        assertThat(histories).hasSize(threadCount);

        long chargeCount = histories.stream()
                .filter(h -> h.type() == TransactionType.CHARGE)
                .count();
        long useCount = histories.stream()
                .filter(h -> h.type() == TransactionType.USE)
                .count();

        assertThat(chargeCount).isEqualTo(10);
        assertThat(useCount).isEqualTo(10);
    }

    @Test
    @DisplayName("여러 사용자가 동시에 충전,사용을 복잡하게 수행해도 각자의 잔액이 정확하다")
    void complexConcurrentScenario_MultipleUsers() throws InterruptedException {
        // given
        int userCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(userCount * 3);
        CountDownLatch latch = new CountDownLatch(userCount * 3);

        // when - 각 사용자당 충전, 사용, 충전 순서로 실행
        for (int i = 0; i < userCount; i++) {
            final long userId = 500L + i;

            // 충전 1000
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(userId, 1000L);
                } finally {
                    latch.countDown();
                }
            });

            // 사용 300
            executorService.submit(() -> {
                try {
                    Thread.sleep(50); // 충전 후 사용하도록 약간의 딜레이
                    pointService.usePoint(userId, 300L);
                } catch (Exception e) {
                    // 잔액 부족 시 무시
                } finally {
                    latch.countDown();
                }
            });

            // 추가 충전 500
            executorService.submit(() -> {
                try {
                    Thread.sleep(100); // 나중에 실행
                    pointService.chargePoint(userId, 500L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - 각 사용자의 최종 잔액 확인 (1000 - 300 + 500 = 1200)
        for (int i = 0; i < userCount; i++) {
            long userId = 500L + i;
            UserPoint point = pointService.getUserPoint(userId);
            assertThat(point.point()).isGreaterThanOrEqualTo(1200L);
        }
    }
}