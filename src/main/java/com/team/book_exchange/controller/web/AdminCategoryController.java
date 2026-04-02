package com.team.book_exchange.controller.web;

import com.team.book_exchange.dto.category.CategoryRequest;
import com.team.book_exchange.exception.CategoryAlreadyExistsException;
import com.team.book_exchange.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

   private final CategoryService categoryService;

   @ModelAttribute("categoryRequest")
   public CategoryRequest categoryRequest() {
       return new CategoryRequest();
   }

   @GetMapping
   public String listCategories(Model model) {
       model.addAttribute("categories", categoryService.getAllCategories());
       return "admin/categories";
   }

   @PostMapping
   public String createCategory(
           @Valid @ModelAttribute("categoryRequest") CategoryRequest categoryRequest,
           BindingResult bindingResult,
           Model model,
           RedirectAttributes redirectAttributes
   ) {
       if (bindingResult.hasErrors()) {
           model.addAttribute("categories", categoryService.getAllCategories());
           return "admin/categories";
       }

       try {
           categoryService.createCategory(categoryRequest);
       } catch (CategoryAlreadyExistsException ex) {
           bindingResult.rejectValue("name", "duplicate", ex.getMessage());
           model.addAttribute("categories", categoryService.getAllCategories());
           return "admin/categories";
       }

       redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
       return "redirect:/admin/categories";
   }

   @GetMapping("/{categoryId}/edit")
   public String showEditPage(@PathVariable Long categoryId, Model model) {
       var category = categoryService.getCategoryById(categoryId);

       CategoryRequest categoryRequest = new CategoryRequest();
       categoryRequest.setName(category.getName());
       categoryRequest.setDescription(category.getDescription());

       model.addAttribute("categoryId", category.getId());
       model.addAttribute("categoryRequest", categoryRequest);

       return "admin/category-edit";
   }

   @PostMapping("/{categoryId}/edit")
   public String updateCategory(
           @PathVariable Long categoryId,
           @Valid @ModelAttribute("categoryRequest") CategoryRequest categoryRequest,
           BindingResult bindingResult,
           Model model,
           RedirectAttributes redirectAttributes
   ) {
       if (bindingResult.hasErrors()) {
           model.addAttribute("categoryId", categoryId);
           return "admin/category-edit";
       }

       try {
           categoryService.updateCategory(categoryId, categoryRequest);
       } catch (CategoryAlreadyExistsException ex) {
           bindingResult.rejectValue("name", "duplicate", ex.getMessage());
           model.addAttribute("categoryId", categoryId);
           return "admin/category-edit";
       }

       redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
       return "redirect:/admin/categories";
   }

   @PostMapping("/{categoryId}/delete")
   public String deleteCategory(@PathVariable Long categoryId, RedirectAttributes redirectAttributes) {
       categoryService.deleteCategory(categoryId);
       redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
       return "redirect:/admin/categories";
   }
}
