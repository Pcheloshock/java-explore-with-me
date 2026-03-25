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
    }

    public void addHit(HitDto hitDto) {
        try {
            restTemplate.postForEntity("/hit", hitDto, Void.class);
            log.debug("Saved hit for URI: {}", hitDto.getUri());
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
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

    /**
     * Получить просмотры для списка событий
     */
    public Map<Long, Long> getViewsByEvents(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        List<ViewStatsDto> stats = getStats(start, end, uris, false);

        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> Long.parseLong(stat.getUri().replace("/events/", "")),
                        ViewStatsDto::getHits,
                        (a, b) -> a
                ));
    }
}