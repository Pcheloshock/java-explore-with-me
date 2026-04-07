package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();
    }

    public static HitDto toHitDto(Hit hit) {
        return new HitDto(
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp().format(FORMATTER)
        );
    }
}
