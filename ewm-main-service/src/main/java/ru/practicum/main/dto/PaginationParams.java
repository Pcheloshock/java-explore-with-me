package ru.practicum.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationParams {
    
    @PositiveOrZero(message = "from must be greater than or equal to 0")
    private int from;
    
    @Positive(message = "size must be greater than 0")
    @Max(value = 100, message = "size must not exceed 100")
    private int size;
}
