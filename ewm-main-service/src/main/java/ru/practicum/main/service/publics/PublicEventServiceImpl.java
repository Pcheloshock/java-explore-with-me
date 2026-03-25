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

        // Сохраняем запрос в статистику
        try {
            statsService.saveHit(request);
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
        }

        // Устанавливаем начальную дату, если не указана
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start date must be before end date");
        }

        // Делаем копии переменных для использования в лямбдах
        final int finalFrom = from;
        final int finalSize = size;

        // Настройка пагинации - для VIEWS сортировку делаем в памяти
        Pageable pageable = PageRequest.of(finalFrom / finalSize, finalSize);

        // Получаем события
        List<Event> events = eventRepository.findAllPublished(
                EventState.PUBLISHED, text, categories, paid, rangeStart, rangeEnd, pageable);

        // Фильтрация по доступности (onlyAvailable)
        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> {
                        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                        return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        // Получаем просмотры для всех событий
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> viewsMap;
        try {
            viewsMap = statsService.getViewsForEvents(
                    eventIds,
                    rangeStart != null ? rangeStart : LocalDateTime.now().minusYears(1),
                    rangeEnd != null ? rangeEnd : LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to get views: {}", e.getMessage());
            viewsMap = Map.of();
        }

        // Создаем финальную копию для использования в лямбде
        final Map<Long, Long> finalViewsMap = viewsMap;

        // Сортируем по просмотрам, если нужно
        if (sort != null && sort.equals("VIEWS")) {
            events.sort((e1, e2) -> {
                Long views1 = finalViewsMap.getOrDefault(e1.getId(), 0L);
                Long views2 = finalViewsMap.getOrDefault(e2.getId(), 0L);
                return views2.compareTo(views1);
            });
        } else {
            // Сортировка по дате
            events.sort(Comparator.comparing(Event::getEventDate));
        }

        // Применяем пагинацию после сортировки
        List<Event> paginatedEvents;
        if (events.size() > finalFrom + finalSize) {
            paginatedEvents = events.subList(finalFrom, finalFrom + finalSize);
        } else if (events.size() > finalFrom) {
            paginatedEvents = events.subList(finalFrom, events.size());
        } else {
            paginatedEvents = new ArrayList<>();
        }

        // Преобразуем в DTO
        return paginatedEvents.stream()
                .map(event -> mapToShortDto(event, finalViewsMap.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }

        // Сохраняем запрос в статистику
        try {
            statsService.saveHit(request);
        } catch (Exception e) {
            log.error("Failed to save hit: {}", e.getMessage());
        }

        // Получаем просмотры
        Long views;
        try {
            views = statsService.getViewsForEvent(
                    id,
                    event.getCreatedOn(),
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Failed to get views for event {}: {}", id, e.getMessage());
            views = 0L;
        }

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