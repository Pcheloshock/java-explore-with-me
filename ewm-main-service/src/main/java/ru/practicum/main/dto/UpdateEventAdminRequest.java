package ru.practicum.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.model.AdminStateAction;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    
    @Size(min = 20, max = 2000)
    private String annotation;
    
    private Long category;
    
    @Size(min = 20, max = 7000)
    private String description;
    
    private String eventDate;
    
    private LocationDto location;
    
    private Boolean paid;
    
    @PositiveOrZero
    private Integer participantLimit;
    
    private Boolean requestModeration;
    
    private AdminStateAction stateAction;
    
    @Size(min = 3, max = 120)
    private String title;
    
    public LocalDateTime getEventDateAsLocalDateTime() {
        if (eventDate == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException ex) {
                return LocalDateTime.parse(eventDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
    }
}
