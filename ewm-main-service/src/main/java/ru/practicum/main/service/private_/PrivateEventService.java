package ru.practicum.main.service.private_;

import ru.practicum.main.dto.*;

import java.util.List;

public interface PrivateEventService {
    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);
}
