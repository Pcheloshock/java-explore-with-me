package ru.practicum.main.service.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCategoryServiceImpl implements PublicCategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " not found"));
        return mapToDto(category);
    }
    
    private CategoryDto mapToDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
