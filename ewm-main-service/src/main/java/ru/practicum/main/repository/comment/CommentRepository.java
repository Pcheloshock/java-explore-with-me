package ru.practicum.main.repository.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatusOrderByCreatedOnDesc(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findAllByStatus(CommentStatus status, Pageable pageable);

    boolean existsByIdAndAuthorId(Long commentId, Long authorId);

    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdOn DESC")
    Page<Comment> findByAuthorId(@Param("userId") Long userId, Pageable pageable);

    // Подсчет опубликованных комментариев по списку событий
    @Query("SELECT c.event.id, COUNT(c) FROM Comment c " +
            "WHERE c.event.id IN :eventIds AND c.status = :status " +
            "GROUP BY c.event.id")
    List<Object[]> countCommentsByEventIdsGrouped(@Param("eventIds") List<Long> eventIds,
                                                  @Param("status") CommentStatus status);

    // Вспомогательный метод для получения Map<eventId, count>
    default Map<Long, Long> countPublishedCommentsByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> results = countCommentsByEventIdsGrouped(eventIds, CommentStatus.PUBLISHED);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    // Подсчет комментариев для одного события
    long countByEventIdAndStatus(Long eventId, CommentStatus status);
}