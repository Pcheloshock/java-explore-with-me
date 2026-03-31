package ru.practicum.main.mapper;

import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Location;

public class EventMapper {

    private EventMapper() {
        // Приватный конструктор для утилитарного класса
    }

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(0L) // Будет заполнено в сервисе
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L) // Будет заполнено в сервисе
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(0L) // Будет заполнено в сервисе
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .location(LocationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(0L) // Будет заполнено в сервисе
                .build();
    }

    public static EventFullDto toFullDto(Event event, Long views) {
        EventFullDto dto = toFullDto(event);
        if (dto != null && views != null) {
            dto.setViews(views);
        }
        return dto;
    }

    public static List<EventShortDto> toShortDtoList(List<Event> events, Map<Long, Long> viewsMap) {
        if (events == null) {
            return Collections.emptyList();
        }

        return events.stream()
                .map(event -> {
                    EventShortDto dto = toShortDto(event);
                    if (dto != null && viewsMap != null) {
                        dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}