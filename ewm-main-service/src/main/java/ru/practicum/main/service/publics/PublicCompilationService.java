package ru.practicum.main.service.publics;

import ru.practicum.main.dto.CompilationDto;
import java.util.List;

public interface PublicCompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
    CompilationDto getCompilation(Long compId);
}
