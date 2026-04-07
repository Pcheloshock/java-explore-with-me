package ru.practicum.main.service.admin;

import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;

public interface AdminCompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    void deleteCompilation(Long compId);
}
