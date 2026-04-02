package com.team.book_exchange.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDecisionRequest {

   @Size(max = 500, message = "Remarks must be at most 500 characters.")
   private String remarks;
}
