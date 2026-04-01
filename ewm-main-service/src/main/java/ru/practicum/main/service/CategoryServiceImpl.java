package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    // Admin методы
    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding category with name: {}", newCategoryDto.getName());
        validateCategoryName(newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category with name " + newCategoryDto.getName() + " already exists");
        }

        Category category = Category.builder()
                .name(newCategoryDto.getName())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Added category: {} with id: {}", saved.getName(), saved.getId());

        return new CategoryDto(saved.getId(), saved.getName());
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with id: {} to name: {}", catId, categoryDto.getName());
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));

        validateCategoryName(categoryDto.getName());

        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Category with name " + categoryDto.getName() + " already exists");
        }

        category.setName(categoryDto.getName());
        Category updated = categoryRepository.save(category);
        log.info("Updated category: {}", updated.getName());

        return new CategoryDto(updated.getId(), updated.getName());
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("Cannot delete category with existing events");
        }

        categoryRepository.delete(category);
        log.info("Deleted category: {}", category.getName());
    }

    // Public методы
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Getting categories with from={}, size={}", from, size);

        // Защита от некорректных значений
        int safeFrom = Math.max(from, 0);
        int safeSize = size > 0 ? Math.min(size, 100) : 10;
        int page = safeFrom / safeSize;

        Pageable pageable = PageRequest.of(page, safeSize);
        log.debug("Pageable: page={}, size={}", page, safeSize);

        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        log.info("Found {} categories", categories.size());

        // Возвращаем пустой список вместо null
        if (categories == null || categories.isEmpty()) {
            log.debug("No categories found, returning empty list");
            return List.of();
        }

        return categories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        log.info("Getting category by id: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));
        return mapToDto(category);
    }

    private CategoryDto mapToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
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