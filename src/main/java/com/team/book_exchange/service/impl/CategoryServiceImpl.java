package com.team.book_exchange.service.impl;

import com.team.book_exchange.dto.category.CategoryRequest;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.exception.CategoryAlreadyExistsException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

   private final CategoryRepository categoryRepository;

   @Override
   @Transactional(readOnly = true)
   public List<Category> getAllCategories() {
       return categoryRepository.findAllByOrderByNameAsc();
   }

   @Override
   @Transactional(readOnly = true)
   public Category getCategoryById(Long categoryId) {
       return categoryRepository.findById(categoryId)
               .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
   }

   @Override
   public Category createCategory(CategoryRequest request) {
       String normalizedName = normalizeRequired(request.getName());

       if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
           throw new CategoryAlreadyExistsException("A category with this name already exists.");
       }

       Category category = Category.builder()
               .name(normalizedName)
               .description(normalizeOptional(request.getDescription()))
               .build();

       return categoryRepository.save(category);
   }

   @Override
   public Category updateCategory(Long categoryId, CategoryRequest request) {
       Category category = getCategoryById(categoryId);
       String normalizedName = normalizeRequired(request.getName());

       if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, categoryId)) {
           throw new CategoryAlreadyExistsException("A category with this name already exists.");
       }

       category.setName(normalizedName);
       category.setDescription(normalizeOptional(request.getDescription()));

       return categoryRepository.save(category);
   }

   @Override
   public void deleteCategory(Long categoryId) {
       Category category = getCategoryById(categoryId);
       categoryRepository.delete(category);
   }

   private String normalizeRequired(String value) {
       return value == null ? null : value.trim();
   }

   private String normalizeOptional(String value) {
       if (value == null) {
           return null;
       }

       String trimmed = value.trim();
       return trimmed.isEmpty() ? null : trimmed;
   }
}
