package ru.practicum.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    @NotBlank(message = "Text cannot be blank")
    @Size(min = 1, max = 2000, message = "Text must be between 1 and 2000 characters")
    private String text;
}
