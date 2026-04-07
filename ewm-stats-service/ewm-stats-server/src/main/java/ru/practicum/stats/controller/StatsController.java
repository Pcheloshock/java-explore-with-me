package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@Valid @RequestBody HitDto hitDto) {
        log.info("Получен запрос на сохранение статистики: {}", hitDto);
        statsService.addHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @NotNull(message = "Start date cannot be null")
            LocalDateTime start,

            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @NotNull(message = "End date cannot be null")
            LocalDateTime end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            Boolean unique) {

        if (start.isAfter(end)) {
            log.error("Start date {} is after end date {}", start, end);
            throw new IllegalArgumentException("Start date must be before end date");
        }

        log.info("Получен запрос на получение статистики с {} по {}, uris: {}, unique: {}",
                start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}
