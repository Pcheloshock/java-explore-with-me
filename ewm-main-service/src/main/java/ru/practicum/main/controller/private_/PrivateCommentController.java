package ru.practicum.main.controller.private_;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.dto.UpdateCommentRequest;
import ru.practicum.main.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("POST /users/{}/comments for event={}", userId, newCommentDto.getEventId());
        return commentService.addComment(userId, newCommentDto);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /users/{}/comments with from={}, size={}", userId, from, size);

        // Валидация параметров пагинации
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
        return commentService.getUserComments(userId, PageRequest.of(page, size));
    }

    @PatchMapping
    public CommentDto updateComment(@PathVariable Long userId,
                                    @Valid @RequestBody UpdateCommentRequest updateRequest) {
        log.info("PATCH /users/{}/comments with commentId={}", userId, updateRequest.getCommentId());
        return commentService.updateComment(userId, updateRequest);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("DELETE /users/{}/comments/{}", userId, commentId);
        commentService.deleteUserComment(userId, commentId);
    }
}