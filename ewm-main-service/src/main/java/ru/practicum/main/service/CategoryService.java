package ru.practicum.main.service;

import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    // Admin методы
    CategoryDto addCategory(NewCategoryDto newCategoryDto);
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);
    void deleteCategory(Long catId);
    
    // Public методы
    List<CategoryDto> getCategories(int from, int size);
    CategoryDto getCategoryById(Long catId);
}
