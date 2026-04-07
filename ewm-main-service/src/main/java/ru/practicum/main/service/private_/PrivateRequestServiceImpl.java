package ru.practicum.main.service.private_;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return requestRepository.findByRequesterId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        log.info("ADD REQUEST: userId={}, eventId={}, requestModeration={}", 
            userId, eventId, event.getRequestModeration());

        Optional<ParticipationRequest> existing = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (existing.isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit has been reached");
        }

        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Saved request: id={}, status={}", saved.getId(), saved.getStatus());

        return mapToDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found for this user");
        }

        log.info("Cancel request: id={}, status={}", requestId, request.getStatus());
        
        // CONFIRMED заявки нельзя отменить
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.warn("Cannot cancel confirmed request {}", requestId);
            throw new ConflictException("Cannot cancel already confirmed request");
        }
        
        // PENDING заявки можно отменить
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceled = requestRepository.save(request);
        log.info("Request canceled, new status: {}", canceled.getStatus());

        return mapToDto(canceled);
    }

    // Добавляем метод для отмены заявки по eventId (для совместимости с тестами)
    public ParticipationRequestDto cancelRequestByEventId(Long userId, Long eventId) {
        log.info("Cancel request by eventId: userId={}, eventId={}", userId, eventId);
        
        ParticipationRequest request = requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Request not found for event " + eventId));
        
        return cancelRequest(userId, request.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User is not the initiator of this event");
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User is not the initiator of this event");
        }

        //if (event.getParticipantLimit() == 0) {
        //    throw new BadRequestException("Event has no participant limit");
        //}

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - confirmedRequests;

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED && availableSlots < updateRequest.getRequestIds().size()) {
            throw new ConflictException("Not enough available slots");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                // Было BadRequestException, должно быть ConflictException
                throw new ConflictException("Request status must be PENDING");
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (availableSlots > 0) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(mapToDto(request));
                    availableSlots--;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(mapToDto(request));
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(mapToDto(request));
            }
        }

        requestRepository.saveAll(requests);
        log.info("Updated request statuses for event {}", eventId);

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    private ParticipationRequestDto mapToDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }
}
