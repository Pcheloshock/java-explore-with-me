package ru.practicum.main.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.*;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.*;
import ru.practicum.main.repository.comment.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(AdminEventFilterParams params) {
        if (params.getSize() <= 0) {
            throw new BadRequestException("Size must be positive");
        }

        if (params.getFrom() < 0) {
            throw new BadRequestException("From must be non-negative");
        }

        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize());

        List<Event> events = eventRepository.findAllByAdmin(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable
        ).getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        Map<Long, Long> commentsCountMap = commentRepository.countPublishedCommentsByEventIds(eventIds);
        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);

        return events.stream()
                .map(event -> EventMapper.toFullDto(
                        event,
                        viewsMap.getOrDefault(event.getId(), 0L),
                        confirmedRequestsMap.getOrDefault(event.getId(), 0L),
                        commentsCountMap.getOrDefault(event.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    // Вспомогательный метод для получения просмотров
    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        // TODO: Реализовать через StatsClient
        // Пока возвращаем пустую карту
        return Map.of();
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + updateRequest.getCategory()));
            event.setCategory(category);
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = updateRequest.getEventDateAsLocalDateTime();
            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Event date cannot be in the past");
            }
            event.setEventDate(eventDate);
        }

        if (updateRequest.getLocation() != null) {
            event.setLocation(Location.builder()
                    .lat(updateRequest.getLocation().getLat())
                    .lon(updateRequest.getLocation().getLon())
                    .build());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        log.info("Admin updated event: {}", updated.getTitle());

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long commentsCount = commentRepository.countByEventIdAndStatus(eventId, CommentStatus.PUBLISHED);
        Long views = 0L;

        return EventMapper.toFullDto(updated, views, confirmedRequests, commentsCount);
    }
}