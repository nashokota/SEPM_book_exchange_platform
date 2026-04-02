package com.team.book_exchange.dto.book;

import com.team.book_exchange.enums.BookAvailabilityStatus;
import com.team.book_exchange.enums.BookCondition;
import com.team.book_exchange.enums.ListingMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title must be at most 150 characters.")
    private String title;

    @NotBlank(message = "Author is required.")
    @Size(max = 150, message = "Author must be at most 150 characters.")
    private String author;

    @Size(max = 20, message = "ISBN must be at most 20 characters.")
    private String isbn;

    @NotNull(message = "Condition is required.")
    private BookCondition condition;

    @NotNull(message = "Listing mode is required.")
    private ListingMode listingMode;

    @NotNull(message = "Availability status is required.")
    private BookAvailabilityStatus availabilityStatus;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0.")
    private BigDecimal price;

    @Size(max = 500, message = "Exchange preference must be at most 500 characters.")
    private String exchangePreference;

    @Size(max = 500, message = "Image URL must be at most 500 characters.")
    private String imageUrl;

    @Size(max = 2000, message = "Description must be at most 2000 characters.")
    private String description;

    @NotNull(message = "Category is required.")
    private Long categoryId;
}
