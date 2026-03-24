package ru.practicum.main.service.admin;

import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import java.util.List;

public interface AdminUserService {
    UserDto addUser(NewUserRequest newUserRequest);
    List<UserDto> getUsers(List<Long> ids, int from, int size);
    void deleteUser(Long userId);
}
