package ru.practicum.main.mapper;

import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventMapper {

    private EventMapper() {
        // Приватный конструктор для утилитарного класса
    }

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toDto(event.getCategory()));
        dto.setConfirmedRequests(0L);
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(0L);

        return dto;
    }

    public static EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toDto(event.getCategory()));
        dto.setConfirmedRequests(0L);
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        dto.setLocation(LocationMapper.toDto(event.getLocation()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setViews(0L);

        return dto;
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