package ru.practicum.main.service.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.*;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.EventState;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.service.stats.StatsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsService statsService;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, String sort, int from, int size,
                                         HttpServletRequest request) {

        log.info("Getting events with text: {}, categories: {}, paid: {}, rangeStart: {}, rangeEnd: {}",
                text, categories, paid, rangeStart, rangeEnd);

        try {
            statsService.saveHit(request);
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
        }

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start date must be before end date");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllPublished(
                EventState.PUBLISHED, text, categories, paid, rangeStart, rangeEnd, pageable);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> {
                        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                        return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        // Используем unique=true для получения уникальных просмотров
        Map<Long, Long> viewsMap = statsService.getViewsForEventsUnique(eventIds, rangeStart, rangeEnd);

        return events.stream()
                .map(event -> mapToShortDto(event, viewsMap.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }

        try {
            statsService.saveHit(request);
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
        }

        // Используем unique=true для получения уникальных просмотров
        Long views = statsService.getViewsForEventUnique(id, event.getCreatedOn(), LocalDateTime.now());

        return mapToFullDto(event, views);
    }

    private EventShortDto mapToShortDto(Event event, Long views) {
        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                new CategoryDto(event.getCategory().getId(), event.getCategory().getName()),
                confirmedRequests,
                event.getEventDate(),
                new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    private EventFullDto mapToFullDto(Event event, Long views) {
        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                new CategoryDto(event.getCategory().getId(), event.getCategory().getName()),
                confirmedRequests,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()),
                new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }
}
