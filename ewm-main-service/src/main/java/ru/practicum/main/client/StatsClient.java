package ru.practicum.main.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
        log.info("StatsClient initialized with URL: {}", serverUrl);
    }

    public void addHit(HitDto hitDto) {
        try {
            log.info("Sending hit to stats-service: app={}, uri={}, ip={}, timestamp={}",
                    hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());

            // Важно: реальный HTTP запрос!
            ResponseEntity<Void> response = restTemplate.postForEntity("/hit", hitDto, Void.class);

            log.info("Hit saved successfully, status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage(), e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            StringBuilder urlBuilder = new StringBuilder("/stats?start={start}&end={end}&unique={unique}");
            if (uris != null && !uris.isEmpty()) {
                urlBuilder.append("&uris={uris}");
            }

            Map<String, Object> parameters = Map.of(
                    "start", start.format(FORMATTER),
                    "end", end.format(FORMATTER),
                    "unique", unique,
                    "uris", uris != null ? String.join(",", uris) : ""
            );

            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {},
                    parameters
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get stats: {}", e.getMessage());
            return List.of();
        }
    }
}