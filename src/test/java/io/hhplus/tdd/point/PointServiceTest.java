package io.hhplus.tdd.point;

import io.hhplus.tdd.point.application.PointService;
import io.hhplus.tdd.point.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO:: 주석 달기
public class PointServiceTest {
    private PointService pointService;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
    }

    @Test
    void 유효하지_않은_사용자_포인트_조회() {
        long id = -1L;

        assertThatThrownBy(() -> pointService.getPoint(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자입니다.");
    }

    @Test
    void 포인트_조회() {
        long id = 1L;
        long amount = 500L;
        UserPoint expected = new UserPoint(id, amount, 0);

        when(userPointRepository.selectById(id)).thenReturn(expected);

        assertThat(pointService.getPoint(id)).isEqualTo(expected);
    }

    @Test
    void 유효하지_않은_사용자_포인트_내역_조회() {
        long userId = -1L;

        assertThatThrownBy(() -> pointService.getPointHistories(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자입니다.");
    }

    @Test
    void 포인트_내역_조회() {
        long userId = 1L;
        List<PointHistory> expected = List.of(
                new PointHistory(1, 1, 500, TransactionType.CHARGE, 0),
                new PointHistory(2, 1, 200, TransactionType.USE, 0)
        );

        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(expected);

        assertThat(pointService.getPointHistories(userId)).isEqualTo(expected);
    }

    @Test
    void 유효하지_않은_사용자_포인트_충전() {
        long id = -1L;
        long amount = 100L;

        assertThatThrownBy(() -> pointService.chargePoint(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자입니다.");
    }

    @Test
    void 포인트_충전_0원_이하() {
        long id = 1L;
        long amount = -100L;

        assertThatThrownBy(() -> pointService.chargePoint(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("0원 이하로 요청할 수 없습니다.");
    }

    @Test
    void 포인트_충전() {
        long id = 1L;
        long amount = 1000L;
        long balance = 200L;
        UserPoint expected = new UserPoint(id, balance + amount, 0);

        when(userPointRepository.selectById(id)).thenReturn(new UserPoint(id, balance, 0));
        when(userPointRepository.insertOrUpdate(id, balance + amount)).thenReturn(expected);

        // 포인트 충전에 대한 검증
        assertThat(pointService.chargePoint(id, amount)).isEqualTo(expected);

        // 포인트 내역 추가에 대한 검증
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TransactionType> typeCaptor = ArgumentCaptor.forClass(TransactionType.class);
        ArgumentCaptor<Long> updateMillisCaptor = ArgumentCaptor.forClass(Long.class);

        verify(pointHistoryRepository).insert(
                userIdCaptor.capture(), amountCaptor.capture(), typeCaptor.capture(), updateMillisCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(id);
        assertThat(amountCaptor.getValue()).isEqualTo(amount);
        assertThat(typeCaptor.getValue()).isEqualTo(TransactionType.CHARGE);
        assertThat(updateMillisCaptor.getValue()).isNotNegative();

    }

    @Test
    void 유효하지_않은_사용자_포인트_사용() {
        long id = -1L;
        long amount = 100L;

        assertThatThrownBy(() -> pointService.usePoint(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 사용자입니다.");
    }

    @Test
    void 포인트_사용_0원_이하() {
        long id = 1L;
        long amount = -100L;

        assertThatThrownBy(() -> pointService.usePoint(id, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("0원 이하로 요청할 수 없습니다.");
    }

    @Test
    void 포인트_사용_잔액_초과() {
        long id = 1L;
        long amount = 2000L;
        long balance = 1000L;

        when(userPointRepository.selectById(id)).thenReturn(new UserPoint(id, balance, 0));

        assertThatThrownBy(() -> pointService.usePoint(id, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("잔액을 초과하여 사용할 수 없습니다.");
    }

    @Test
    void 포인트_사용() {
        long id = 1L;
        long amount = 100L;
        long balance = 1000L;
        UserPoint expected = new UserPoint(id, balance - amount, 0);

        when(userPointRepository.selectById(id)).thenReturn(new UserPoint(id, balance, 0));
        when(userPointRepository.insertOrUpdate(id, balance - amount)).thenReturn(expected);

        // 포인트 사용에 대한 검증
        assertThat(pointService.usePoint(id, amount)).isEqualTo(expected);

        // 포인트 내역 추가에 대한 검증
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> amountCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TransactionType> typeCaptor = ArgumentCaptor.forClass(TransactionType.class);
        ArgumentCaptor<Long> updateMillisCaptor = ArgumentCaptor.forClass(Long.class);

        verify(pointHistoryRepository).insert(
                userIdCaptor.capture(), amountCaptor.capture(), typeCaptor.capture(), updateMillisCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(id);
        assertThat(amountCaptor.getValue()).isEqualTo(amount);
        assertThat(typeCaptor.getValue()).isEqualTo(TransactionType.USE);
        assertThat(updateMillisCaptor.getValue()).isNotNegative();
    }
}