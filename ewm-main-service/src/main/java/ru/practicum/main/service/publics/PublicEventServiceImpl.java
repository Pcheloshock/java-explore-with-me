package ru.practicum.main.service.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.stats.client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Удаляем eventMapper, используем статические методы

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, String sort, int from, int size,
                                         HttpServletRequest request) {

        log.info("Getting events with text: {}, categories: {}, paid: {}, rangeStart: {}, rangeEnd: {}",
                text, categories, paid, rangeStart, rangeEnd);

        // Сохраняем хит
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

        // Устанавливаем значения по умолчанию
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        // Проверка дат
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start date must be before end date");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Event> events = eventRepository.findAllPublished(
                EventState.PUBLISHED, text, categories, paid, rangeStart, rangeEnd, pageable);

        // Возвращаем пустой список, если нет событий
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        if (onlyAvailable) {
            List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequestsMap = requestRepository.countConfirmedRequestsByEventIds(eventIds);

            events = events.stream()
                    .filter(event -> {
                        long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                        return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());

            // Если после фильтрации не осталось событий
            if (events.isEmpty()) {
                return Collections.emptyList();
            }
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap = getViewsForEventsUnique(eventIds, rangeStart, rangeEnd);

        return EventMapper.toShortDtoList(events, viewsMap);
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }

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

        Long views = getViewsForEventUnique(id, event.getCreatedOn(), LocalDateTime.now());

        // Используем статический метод EventMapper.toFullDto
        return EventMapper.toFullDto(event, views);
    }

    private Map<Long, Long> getViewsForEventsUnique(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
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

    private Long getViewsForEventUnique(Long eventId, LocalDateTime start, LocalDateTime end) {
        return getViewsForEventsUnique(List.of(eventId), start, end).getOrDefault(eventId, 0L);
    }
}