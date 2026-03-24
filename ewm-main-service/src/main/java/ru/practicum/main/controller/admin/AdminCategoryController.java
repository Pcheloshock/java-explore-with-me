package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.service.admin.AdminCategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final AdminCategoryService categoryService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("POST /admin/categories with name: {}", newCategoryDto.getName());
        return categoryService.addCategory(newCategoryDto);
    }
    
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("PATCH /admin/categories/{} with name: {}", catId, categoryDto.getName());
        return categoryService.updateCategory(catId, categoryDto);
    }
    
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }
}
