package ru.practicum.main.service.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main.client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveHit(HttpServletRequest request) {
        try {
            HitDto hitDto = new HitDto(
                    "ewm-main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(FORMATTER)
            );
            statsClient.addHit(hitDto);
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
        }
    }

    public Map<Long, Long> getViewsForEvents(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        return getViewsForEventsWithUnique(eventIds, start, end, false);
    }

    public Map<Long, Long> getViewsForEventsUnique(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        return getViewsForEventsWithUnique(eventIds, start, end, true);
    }

    private Map<Long, Long> getViewsForEventsWithUnique(List<Long> eventIds, LocalDateTime start, LocalDateTime end, boolean unique) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, unique);
            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> {
                                String uri = stat.getUri();
                                if (uri.startsWith("/events/")) {
                                    return Long.parseLong(uri.substring(8));
                                }
                                return 0L;
                            },
                            ViewStatsDto::getHits,
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            log.error("Failed to get views: {}", e.getMessage());
            return Map.of();
        }
    }

    public Long getViewsForEvent(Long eventId, LocalDateTime start, LocalDateTime end) {
        return getViewsForEventsWithUnique(List.of(eventId), start, end, false).getOrDefault(eventId, 0L);
    }

    public Long getViewsForEventUnique(Long eventId, LocalDateTime start, LocalDateTime end) {
        return getViewsForEventsWithUnique(List.of(eventId), start, end, true).getOrDefault(eventId, 0L);
    }
}
