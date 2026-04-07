package ru.practicum.main.repository.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatusOrderByCreatedOnDesc(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findAllByStatus(CommentStatus status, Pageable pageable);

    boolean existsByIdAndAuthorId(Long commentId, Long authorId);

    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdOn DESC")
    Page<Comment> findByAuthorId(@Param("userId") Long userId, Pageable pageable);
}