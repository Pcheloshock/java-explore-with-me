package ru.practicum.main.model;

import lombok.*;

import javax.persistence.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    private Float lat;
    private Float lon;
}
