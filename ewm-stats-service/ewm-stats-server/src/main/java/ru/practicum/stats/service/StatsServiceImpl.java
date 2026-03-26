package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void addHit(HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        statsRepository.save(hit);
        log.debug("Сохранена статистика для uri: {}", hit.getUri());
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        List<Object[]> results;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                results = statsRepository.getUniqueStats(start, end);
            } else {
                results = statsRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                results = statsRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                results = statsRepository.getAllStatsByUris(start, end, uris);
            }
        }

        return results.stream()
                .map(row -> new ViewStatsDto(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }
}
