package com.team.book_exchange.service;

import com.team.book_exchange.dto.auth.RegisterRequest;
import com.team.book_exchange.entity.User;

public interface AuthService {

   User registerBuyer(RegisterRequest registerRequest);
}
