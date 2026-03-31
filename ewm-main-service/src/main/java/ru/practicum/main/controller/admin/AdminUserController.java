package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.dto.UserDto;
import ru.practicum.main.service.admin.AdminUserService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated  // <-- ДОБАВИТЬ ЭТУ АННОТАЦИЮ
public class AdminUserController {

    private final AdminUserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("POST /admin/users");
        return userService.addUser(newUserRequest);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,           // <-- ДОБАВИТЬ ВАЛИДАЦИЮ
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) { // <-- ДОБАВИТЬ ВАЛИДАЦИЮ

        log.info("GET /admin/users with from={}, size={}", from, size);

        List<UserDto> users = userService.getUsers(ids, from, size);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }
}