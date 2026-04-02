package com.team.book_exchange.controller.api;

import com.team.book_exchange.dto.category.CategoryRequest;
import com.team.book_exchange.dto.category.CategoryResponse;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

   private final CategoryService categoryService;

   @GetMapping
   public ResponseEntity<List<CategoryResponse>> getAllCategories() {
       List<CategoryResponse> categories = categoryService.getAllCategories().stream()
               .map(this::mapCategory)
               .toList();

       return ResponseEntity.ok(categories);
   }

   @GetMapping("/{categoryId}")
   public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId) {
       return ResponseEntity.ok(mapCategory(categoryService.getCategoryById(categoryId)));
   }

   @PostMapping
   public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
       Category created = categoryService.createCategory(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(mapCategory(created));
   }

   @PutMapping("/{categoryId}")
   public ResponseEntity<CategoryResponse> updateCategory(
           @PathVariable Long categoryId,
           @Valid @RequestBody CategoryRequest request
   ) {
       Category updated = categoryService.updateCategory(categoryId, request);
       return ResponseEntity.ok(mapCategory(updated));
   }

   @DeleteMapping("/{categoryId}")
   public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
       categoryService.deleteCategory(categoryId);
       return ResponseEntity.noContent().build();
   }

   private CategoryResponse mapCategory(Category category) {
       return CategoryResponse.builder()
               .id(category.getId())
               .name(category.getName())
               .description(category.getDescription())
               .createdAt(category.getCreatedAt())
               .updatedAt(category.getUpdatedAt())
               .build();
   }
}
