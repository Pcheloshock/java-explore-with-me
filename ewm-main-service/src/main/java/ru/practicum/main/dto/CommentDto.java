package ru.practicum.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.model.CommentStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdOn;
    private LocalDateTime editedOn;
    private CommentStatus status;
}