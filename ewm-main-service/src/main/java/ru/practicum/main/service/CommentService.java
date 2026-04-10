package ru.practicum.main.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentRequest;
import ru.practicum.main.model.CommentStatus;

import java.util.List;

public interface CommentService {
    // Admin методы
    List<CommentDto> getCommentsByStatus(CommentStatus status, Pageable pageable);
    CommentDto updateCommentStatus(Long commentId, CommentStatus status);
    void deleteComment(Long commentId);

    // Private методы
    CommentDto addComment(Long userId, NewCommentDto newCommentDto);
    List<CommentDto> getUserComments(Long userId, Pageable pageable);
    CommentDto updateComment(Long userId, UpdateCommentRequest updateRequest);
    void deleteUserComment(Long userId, Long commentId);

    // Public методы
    List<CommentDto> getPublishedCommentsByEvent(Long eventId, Pageable pageable);
    CommentDto getPublishedComment(Long commentId);
}