package ru.practicum.main.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.service.publics.PublicCompilationService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated  // ДОБАВИТЬ
public class PublicCompilationController {

    private final PublicCompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,  // ИЗМЕНИТЬ int -> Integer
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {  // ДОБАВИТЬ @Min, @Max
        log.info("GET /compilations with pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        return compilationService.getCompilation(compId);
    }
}