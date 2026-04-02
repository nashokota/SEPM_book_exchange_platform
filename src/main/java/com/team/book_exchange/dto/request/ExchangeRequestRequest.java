package com.team.book_exchange.dto.request;

import com.team.book_exchange.enums.RequestType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRequestRequest {

    @NotNull(message = "Book is required.")
    private Long bookId;

    @NotNull(message = "Request type is required.")
    private RequestType requestType;

    @Size(max = 255, message = "Offered book title must be at most 255 characters.")
    private String offeredBookTitle;

    @Size(max = 1000, message = "Offered book details must be at most 1000 characters.")
    private String offeredBookDetails;

    @Size(max = 1000, message = "Message must be at most 1000 characters.")
    private String message;
}
