package io.hhplus.tdd.point.presentation;

import io.hhplus.tdd.point.application.PointService;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc 사용하여 엔드포인트 요청 테스트 및 정상 응답에 대한 status, json 값 검증
 */
@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PointService pointService;

    @Test
    void 포인트_조회() throws Exception {
        // given
        long id = 1L;
        long amount = 1000L;

        // when
        when(pointService.getPoint(anyLong())).thenReturn(new UserPoint(id, amount, 0));

        // then
        mockMvc.perform(get("/point/{id}", id))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(id))
               .andExpect(jsonPath("$.point").value(amount));

        verify(pointService).getPoint(id);
    }

    @Test
    void 포인트_내역_조회() throws Exception {
        // given
        long id = 1L;
        List<PointHistory> expected = List.of(
                new PointHistory(1, 1, 500, TransactionType.CHARGE, 0),
                new PointHistory(2, 1, 200, TransactionType.USE, 0)
        );

        // when
        when(pointService.getPointHistories(anyLong())).thenReturn(expected);

        // then
        mockMvc.perform(get("/point/{id}/histories", id))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].userId").value(1))
               .andExpect(jsonPath("$[0].amount").value(500))
               .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.name()))
               .andExpect(jsonPath("$[1].id").value(2))
               .andExpect(jsonPath("$[1].userId").value(1))
               .andExpect(jsonPath("$[1].amount").value(200))
               .andExpect(jsonPath("$[1].type").value(TransactionType.USE.name()));

        verify(pointService).getPointHistories(id);
    }

    @Test
    void 포인트_충전() throws Exception {
        // given
        long id = 1L;
        long amount = 1000L;
        long balance = 2000L;

        // when
        when(pointService.chargePoint(anyLong(), anyLong())).thenReturn(new UserPoint(id, balance + amount, 0));

        // then
        mockMvc.perform(patch("/point/{id}/charge", id)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(String.valueOf(amount)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(id))
               .andExpect(jsonPath("$.point").value(balance + amount));

        verify(pointService).chargePoint(id, amount);
    }

    @Test
    void 포인트_사용() throws Exception {
        // given
        long id = 1L;
        long amount = 1000L;
        long balance = 2000L;

        // when
        when(pointService.usePoint(anyLong(), anyLong())).thenReturn(new UserPoint(id, balance - amount, 0));

        // then
        mockMvc.perform(patch("/point/{id}/use", id)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(String.valueOf(amount)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(id))
               .andExpect(jsonPath("$.point").value(balance - amount));

        verify(pointService).usePoint(id, amount);
    }
}