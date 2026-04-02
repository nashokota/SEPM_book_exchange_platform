package com.team.book_exchange.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerApplicationRequest {

    @NotBlank(message = "Reason is required.")
    @Size(max = 500, message = "Reason must be at most 500 characters.")
    private String reason;

    @NotBlank(message = "Pickup location is required.")
    @Size(max = 255, message = "Pickup location must be at most 255 characters.")
    private String pickupLocation;
}
