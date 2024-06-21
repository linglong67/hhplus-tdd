package io.hhplus.tdd.point;

import io.hhplus.tdd.point.application.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointIntegrationTest {

    @Autowired
    private PointService pointService;

    @Test
    void 동시에_다수의_충전_사용_요청처리() {
        // given
        long id = 1L;
        pointService.chargePoint(id, 10000);

        // when
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    pointService.usePoint(id, 8000);
                }),
                CompletableFuture.runAsync(() -> {
                    pointService.chargePoint(id, 3000);
                }),
                CompletableFuture.runAsync(() -> {
                    pointService.usePoint(id, 4000);
                })
        ).join();

        // then
        assertThat(pointService.getPoint(id).point()).isEqualTo(10000 - 8000 + 3000 - 4000);
    }
}
