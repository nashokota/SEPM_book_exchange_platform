package com.team.book_exchange.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

   @NotBlank(message = "Category name is required.")
   @Size(max = 100, message = "Category name must be at most 100 characters.")
   private String name;

   @Size(max = 255, message = "Description must be at most 255 characters.")
   private String description;
}
