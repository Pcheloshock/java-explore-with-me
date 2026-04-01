package ru.practicum.main.service.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.model.RequestStatus;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        // Возвращаем пустой список вместо null
        if (compilations == null || compilations.isEmpty()) {
            return List.of();
        }

        return compilations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        return mapToDto(compilation);
    }

    private CompilationDto mapToDto(Compilation compilation) {
        List<EventShortDto> events = compilation.getEvents().stream()
                .map(this::mapEventToShortDto)
                .collect(Collectors.toList());

        return new CompilationDto(
                compilation.getId(),
                events,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }

    private EventShortDto mapEventToShortDto(Event event) {
        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                new ru.practicum.main.dto.CategoryDto(event.getCategory().getId(), event.getCategory().getName()),
                confirmedRequests,
                event.getEventDate(),
                new ru.practicum.main.dto.UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()),
                event.getPaid(),
                event.getTitle(),
                0L
        );
    }
}
