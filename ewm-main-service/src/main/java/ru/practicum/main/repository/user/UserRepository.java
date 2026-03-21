package ru.practicum.main.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
