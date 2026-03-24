package ru.practicum.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 250, message = "Name must be between 2 and 250 characters")
    private String name;
    
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(min = 6, max = 254, message = "Email must be between 6 and 254 characters")
    private String email;
}
