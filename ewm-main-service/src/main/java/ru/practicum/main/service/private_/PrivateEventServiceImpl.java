package ru.practicum.main.service.private_;

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
import ru.practicum.main.model.*;
import ru.practicum.main.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable).stream()
                .map(this::mapToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        LocalDateTime eventDate = newEventDto.getEventDateAsLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        if (eventDate.isBefore(now.plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Location location = Location.builder()
                .lat(newEventDto.getLocation().getLat())
                .lon(newEventDto.getLocation().getLon())
                .build();

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(now)
                .description(newEventDto.getDescription())
                .eventDate(eventDate)
                .initiator(initiator)
                .location(location)
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .state(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();

        Event saved = eventRepository.save(event);
        log.info("Created event: {} by user: {}", saved.getTitle(), userId);

        return mapToFullDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found for this user");
        }

        return mapToFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found for this user");
        }

        // Проверка: нельзя изменять опубликованное событие
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = updateRequest.getEventDateAsLocalDateTime();
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours from now");
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
            if (updateRequest.getStateAction() == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updated = eventRepository.save(event);
        log.info("Updated event: {} by user: {}", updated.getTitle(), userId);

        return mapToFullDto(updated);
    }

    private EventShortDto mapToShortDto(Event event) {
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
                0L
        );
    }

    private EventFullDto mapToFullDto(Event event) {
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
                0L
        );
    }
}