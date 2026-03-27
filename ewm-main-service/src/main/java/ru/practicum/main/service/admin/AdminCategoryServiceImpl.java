package ru.practicum.main.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        // Валидация длины имени
        validateCategoryName(newCategoryDto.getName());
        
        try {
            Category category = Category.builder()
                    .name(newCategoryDto.getName())
                    .build();

            Category saved = categoryRepository.save(category);
            log.info("Added category: {}", saved.getName());

            return new CategoryDto(saved.getId(), saved.getName());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category with name " + newCategoryDto.getName() + " already exists");
        }
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));

        // Валидация длины имени
        validateCategoryName(categoryDto.getName());

        try {
            category.setName(categoryDto.getName());
            Category updated = categoryRepository.save(category);
            log.info("Updated category: {}", updated.getName());

            return new CategoryDto(updated.getId(), updated.getName());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category with name " + categoryDto.getName() + " already exists");
        }
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("Cannot delete category with existing events");
        }

        categoryRepository.delete(category);
        log.info("Deleted category: {}", category.getName());
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Name cannot be blank");
        }
        if (name.length() > 50) {
            throw new BadRequestException("Name must be no more than 50 characters");
        }
        if (name.length() < 1) {
            throw new BadRequestException("Name must be at least 1 character");
        }
    }
}
