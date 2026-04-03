package ru.practicum.main.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.*;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.RequestStatus;
import ru.practicum.main.repository.ParticipationRequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrivateEventMapper {

    private final ParticipationRequestRepository requestRepository;

    public EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L)
                .build();
    }

    public List<EventShortDto> toShortDtoList(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        // Получаем количество подтверждённых запросов для всех событий одним запросом
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = requestRepository.countConfirmedRequestsByEventIds(eventIds);

        return events.stream()
                .map(event -> EventShortDto.builder()
                        .id(event.getId())
                        .annotation(event.getAnnotation())
                        .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                        .confirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L))
                        .eventDate(event.getEventDate())
                        .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                        .paid(event.getPaid())
                        .title(event.getTitle())
                        .views(0L)
                        .build())
                .collect(Collectors.toList());
    }

    public EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L)
                .build();
    }
}
