package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentRequest;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.repository.comment.CommentRepository; // <-- ИСПРАВЛЕН ИМПОРТ

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    // ========== Admin методы ==========
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByStatus(CommentStatus status, Pageable pageable) {
        log.info("Getting comments by status: {}", status);
        List<Comment> comments;
        if (status != null) {
            comments = commentRepository.findAllByStatus(status, pageable);
        } else {
            comments = commentRepository.findAll(pageable).getContent();
        }
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateCommentStatus(Long commentId, CommentStatus status) {
        log.info("Admin updating comment {} status to {}", commentId, status);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (status == CommentStatus.DELETED) {
            throw new BadRequestException("Use DELETE endpoint to delete comments");
        }

        comment.setStatus(status);
        if (status == CommentStatus.PUBLISHED) {
            comment.setEditedOn(null);
        }

        Comment updated = commentRepository.save(comment);
        return CommentMapper.toDto(updated);
    }

    @Override
    public void deleteComment(Long commentId) {
        log.info("Admin deleting comment {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
        log.info("Comment {} marked as DELETED", commentId);
    }

    // ========== Private методы ==========
    @Override
    public CommentDto addComment(Long userId, NewCommentDto newCommentDto) {
        log.info("User {} adding comment to event {}", userId, newCommentDto.getEventId());

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Event event = eventRepository.findById(newCommentDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + newCommentDto.getEventId()));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        // Проверяем, что пользователь участвовал в событии или является его инициатором
        boolean canComment = event.getInitiator().getId().equals(userId) ||
                requestRepository.findByRequesterIdAndEventId(userId, event.getId())
                        .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                        .isPresent();

        if (!canComment) {
            throw new ConflictException("Only participants or initiator can comment on event");
        }

        // Используем маппер для создания сущности
        Comment comment = CommentMapper.toEntity(newCommentDto, event, author);

        Comment saved = commentRepository.save(comment);
        log.info("Created comment {} for event {}", saved.getId(), event.getId());

        return CommentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, Pageable pageable) {
        log.info("Getting comments for user {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Используем метод findByAuthorId из репозитория
        return commentRepository.findByAuthorId(userId, pageable).getContent().stream()
                .filter(c -> c.getStatus() != CommentStatus.DELETED)
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateComment(Long userId, UpdateCommentRequest updateRequest) {
        Long commentId = updateRequest.getCommentId();
        log.info("User {} updating comment {}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User is not the author of this comment");
        }

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new ConflictException("Cannot update deleted comment");
        }

        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new ConflictException("Cannot update published comment");
        }

        comment.setText(updateRequest.getText());
        comment.setEditedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        Comment updated = commentRepository.save(comment);
        log.info("Updated comment {}", commentId);

        return CommentMapper.toDto(updated);
    }

    @Override
    public void deleteUserComment(Long userId, Long commentId) {
        log.info("User {} deleting comment {}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("User is not the author of this comment");
        }

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
        log.info("Comment {} marked as DELETED by user {}", commentId, userId);
    }

    // ========== Public методы ==========
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getPublishedCommentsByEvent(Long eventId, Pageable pageable) {
        log.info("Getting published comments for event {}", eventId);

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        return commentRepository.findByEventIdAndStatusOrderByCreatedOnDesc(eventId, CommentStatus.PUBLISHED, pageable)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getPublishedComment(Long commentId) {
        log.info("Getting published comment {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Comment not found");
        }

        return CommentMapper.toDto(comment);
    }
}