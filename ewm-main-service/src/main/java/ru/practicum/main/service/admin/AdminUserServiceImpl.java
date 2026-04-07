package ru.practicum.main.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("User with email " + newUserRequest.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();

        User saved = userRepository.save(user);
        log.info("Added user: {}", saved.getName());

        return new UserDto(saved.getId(), saved.getName(), saved.getEmail());
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        // Защита от некорректных значений
        int safeFrom = Math.max(from, 0);
        int safeSize = size > 0 ? Math.min(size, 100) : 10;
        int page = safeFrom / safeSize;

        Pageable pageable = PageRequest.of(page, safeSize);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findAllById(ids);
        }

        // Возвращаем пустой список вместо null
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        return users.stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        userRepository.delete(user);
        log.info("Deleted user: {}", user.getName());
    }
}