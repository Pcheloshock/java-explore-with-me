package ru.practicum.main.service.admin;

import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.model.EventState;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getEvents(List<Long> users, List<EventState> states,
                                  List<Long> categories, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, int from, int size);
    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);
}
