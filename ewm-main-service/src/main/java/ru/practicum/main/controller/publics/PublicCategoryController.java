package ru.practicum.main.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.service.publics.PublicCategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    
    private final PublicCategoryService categoryService;
    
    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /categories with from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }
    
    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return categoryService.getCategoryById(catId);
    }
}
