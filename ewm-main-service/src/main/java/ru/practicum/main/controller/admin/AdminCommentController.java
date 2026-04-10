package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.model.CommentStatus;
import ru.practicum.main.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(
            @RequestParam(required = false) CommentStatus status,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /admin/comments with status={}, from={}, size={}", status, from, size);

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
        return commentService.getCommentsByStatus(status, PageRequest.of(page, size));
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentStatus(@PathVariable Long commentId,
                                          @RequestParam @NotNull CommentStatus status) {
        log.info("PATCH /admin/comments/{} with status={}", commentId, status);
        return commentService.updateCommentStatus(commentId, status);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE /admin/comments/{}", commentId);
        commentService.deleteComment(commentId);
    }
}