package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class PointControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PointController()).build();
    }

    @Test
    void point() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/point/1"))
               .andExpect(result -> result.equals(new UserPoint(0, 0, 0)));
    }

    @Test
    void history() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/point/1/histories"))
               .andExpect(result -> result.equals(List.of()));
    }

    @Test
    void charge() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/point/1/charge"))
               .andExpect(result -> result.equals(new UserPoint(0, 0, 0)));
    }

    @Test
    void use() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/point/1/use"))
               .andExpect(result -> result.equals(new UserPoint(0, 0, 0)));
    }
}