package ru.practicum.main.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.service.CategoryService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated  // ДОБАВИТЬ
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @Min(0) Integer from,  // ИЗМЕНИТЬ int -> Integer
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {  // ДОБАВИТЬ @Min, @Max
        log.info("GET /categories with from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return categoryService.getCategoryById(catId);
    }
}