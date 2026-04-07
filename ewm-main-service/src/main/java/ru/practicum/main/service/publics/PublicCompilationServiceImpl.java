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
        log.info("Getting compilations with pinned={}, from={}, size={}", pinned, from, size);

        // Защита от деления на ноль и некорректных значений
        int safeFrom = Math.max(from, 0);
        int safeSize = size > 0 ? Math.min(size, 100) : 10;
        int page = safeFrom / safeSize;

        Pageable pageable = PageRequest.of(page, safeSize);
        log.debug("Pageable: page={}, size={}", page, safeSize);

        List<Compilation> compilations;
        if (pinned != null) {
            log.debug("Finding compilations with pinned={}", pinned);
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            log.debug("Finding all compilations");
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        log.info("Found {} compilations", compilations.size());

        // Возвращаем пустой список вместо null
        if (compilations == null || compilations.isEmpty()) {
            log.debug("No compilations found, returning empty list");
            return List.of();
        }

        return compilations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Getting compilation with id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found with id: " + compId));

        return mapToDto(compilation);
    }

    private CompilationDto mapToDto(Compilation compilation) {
        log.debug("Mapping compilation to DTO: id={}, title={}", compilation.getId(), compilation.getTitle());
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