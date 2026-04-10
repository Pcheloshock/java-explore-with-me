package ru.practicum.main.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getEventComments(@PathVariable Long eventId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.info("GET /events/{}/comments with from={}, size={}", eventId, from, size);

        // Валидация
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        if (size > 100) {
            throw new BadRequestException("Parameter 'size' cannot exceed 100");
        }

        int page = from / size;
        return commentService.getPublishedCommentsByEvent(eventId, PageRequest.of(page, size));
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long eventId,
                                 @PathVariable Long commentId) {
        log.info("GET /events/{}/comments/{}", eventId, commentId);
        return commentService.getPublishedComment(commentId);
    }
}