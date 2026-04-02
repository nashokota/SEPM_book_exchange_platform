package com.team.book_exchange.service;

import com.team.book_exchange.dto.category.CategoryRequest;
import com.team.book_exchange.entity.Category;
import com.team.book_exchange.exception.CategoryAlreadyExistsException;
import com.team.book_exchange.exception.ResourceNotFoundException;
import com.team.book_exchange.repository.CategoryRepository;
import com.team.book_exchange.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

   @Mock
   private CategoryRepository categoryRepository;

   @InjectMocks
   private CategoryServiceImpl categoryService;

   @Test
   void createCategory_shouldSaveCategory_whenNameIsUnique() {
       CategoryRequest request = new CategoryRequest();
       request.setName("Fiction");
       request.setDescription("Story books");

       when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(false);
       when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

       Category saved = categoryService.createCategory(request);

       assertEquals("Fiction", saved.getName());
       assertEquals("Story books", saved.getDescription());
       verify(categoryRepository).save(any(Category.class));
   }

   @Test
   void createCategory_shouldThrowException_whenNameAlreadyExists() {
       CategoryRequest request = new CategoryRequest();
       request.setName("Fiction");

       when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(true);

       assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(request));
       verify(categoryRepository, never()).save(any(Category.class));
   }

   @Test
   void updateCategory_shouldUpdateCategory_whenNameIsAvailable() {
       Category existing = Category.builder()
               .id(1L)
               .name("Old Name")
               .description("Old description")
               .build();

       CategoryRequest request = new CategoryRequest();
       request.setName("New Name");
       request.setDescription("New description");

       when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
       when(categoryRepository.existsByNameIgnoreCaseAndIdNot("New Name", 1L)).thenReturn(false);
       when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

       Category updated = categoryService.updateCategory(1L, request);

       assertEquals("New Name", updated.getName());
       assertEquals("New description", updated.getDescription());
       verify(categoryRepository).save(existing);
   }

   @Test
   void getCategoryById_shouldThrowException_whenCategoryDoesNotExist() {
       when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

       assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
   }
}
