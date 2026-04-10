package ru.practicum.main.mapper;

import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    private CommentMapper() {
    }

    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setEventId(comment.getEvent().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreatedOn(comment.getCreatedOn());
        dto.setEditedOn(comment.getEditedOn());
        dto.setStatus(comment.getStatus());

        return dto;
    }

    public static Comment toEntity(NewCommentDto newCommentDto, Event event, User author) {
        if (newCommentDto == null) {
            return null;
        }

        return Comment.builder()
                .text(newCommentDto.getText())
                .event(event)
                .author(author)
                .createdOn(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();
    }
}