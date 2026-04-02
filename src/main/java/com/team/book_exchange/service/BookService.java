package com.team.book_exchange.service;

import com.team.book_exchange.dto.book.BookRequest;
import com.team.book_exchange.entity.Book;

import java.util.List;

public interface BookService {

   List<Book> getBooksForSeller(String sellerEmail);

   Book getBookForSeller(Long bookId, String sellerEmail);

   Book createBook(String sellerEmail, BookRequest request);

   Book updateBook(Long bookId, String sellerEmail, BookRequest request);

   void deleteBook(Long bookId, String sellerEmail);

   List<Book> getPublicAvailableBooks();

   Book getPublicAvailableBook(Long bookId);
}
