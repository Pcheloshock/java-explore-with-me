package ru.practicum.main.service.admin;

import ru.practicum.main.dto.AdminEventFilterParams;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getEvents(AdminEventFilterParams params);
    
    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);
}
