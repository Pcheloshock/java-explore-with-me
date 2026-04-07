package ru.practicum.main.mapper;

import ru.practicum.main.dto.LocationDto;
import ru.practicum.main.model.Location;

public class LocationMapper {

    private LocationMapper() {}

    public static LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }

        // Создаем через конструктор вместо билдера
        LocationDto dto = new LocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }

    public static Location toEntity(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }
}