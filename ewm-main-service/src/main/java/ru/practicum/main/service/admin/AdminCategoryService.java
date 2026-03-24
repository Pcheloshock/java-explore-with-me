package ru.practicum.main.service.admin;

import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);
    void deleteCategory(Long catId);
}
