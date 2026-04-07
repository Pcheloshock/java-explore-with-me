package ru.practicum.main.repository.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatusOrderByCreatedOnDesc(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findAllByStatus(CommentStatus status, Pageable pageable);

    boolean existsByIdAndAuthorId(Long commentId, Long authorId);
}
