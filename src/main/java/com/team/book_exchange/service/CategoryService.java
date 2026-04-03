package com.team.book_exchange.service;

import com.team.book_exchange.dto.category.CategoryRequest;
import com.team.book_exchange.entity.Category;

import java.util.List;

public interface CategoryService {

   List<Category> getAllCategories();

   Category getCategoryById(Long categoryId);

   Category createCategory(CategoryRequest request);

   Category updateCategory(Long categoryId, CategoryRequest request);

   void deleteCategory(Long categoryId);
}
