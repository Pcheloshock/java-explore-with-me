package ru.practicum.main.mapper;

import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {

    private CompilationMapper() {
        // Приватный конструктор для утилитарного класса
    }

    public static CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events != null ? events : Collections.emptyList())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}