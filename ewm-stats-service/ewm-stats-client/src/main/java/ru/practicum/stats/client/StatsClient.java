package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
        log.info("StatsClient initialized with URL: {}", serverUrl);
    }

    public void addHit(HitDto hitDto) {
        try {
            log.info("Sending hit to stats-service: {}", hitDto);
            restTemplate.postForEntity("/hit", hitDto, Void.class);
            log.info("Hit saved successfully");
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            String url = "/stats?start={start}&end={end}&unique={unique}";
            Map<String, Object> params = Map.of(
                    "start", start.format(FORMATTER),
                    "end", end.format(FORMATTER),
                    "unique", unique
            );

            if (uris != null && !uris.isEmpty()) {
                url += "&uris={uris}";
                params = Map.of(
                        "start", start.format(FORMATTER),
                        "end", end.format(FORMATTER),
                        "unique", unique,
                        "uris", String.join(",", uris)
                );
            }

            ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {},
                    params
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get stats: {}", e.getMessage(), e);
            throw e;
        }
    }
}
