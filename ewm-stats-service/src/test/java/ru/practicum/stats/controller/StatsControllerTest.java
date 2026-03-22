package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.HitDto;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private StatsService statsService;
    
    @Test
    void addHit_WithValidData_ShouldReturnCreated() throws Exception {
        HitDto hitDto = new HitDto(
                "test-app",
                "/events/1",
                "127.0.0.1",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated());
    }
    
    @Test
    void addHit_WithInvalidIp_ShouldReturnBadRequest() throws Exception {
        HitDto hitDto = new HitDto(
                "test-app",
                "/events/1",
                "invalid-ip",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getStats_WithStartAfterEnd_ShouldReturnBadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);
        
        mockMvc.perform(get("/stats")
                .param("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .param("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getStats_WithMissingStartDate_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                .param("end", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(status().isBadRequest());
    }
}
