package ru.practicum.main.mapper;

import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.model.Category;

public class CategoryMapper {

    private CategoryMapper() {}

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}