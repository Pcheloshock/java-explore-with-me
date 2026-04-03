package ru.practicum.main.mapper;

import ru.practicum.main.dto.UserShortDto;
import ru.practicum.main.model.User;

public class UserMapper {

    private UserMapper() {}

    public static UserShortDto toShortDto(User user) {
        if (user == null) {
            return null;
        }

        // Создаем через конструктор вместо билдера
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }
}
