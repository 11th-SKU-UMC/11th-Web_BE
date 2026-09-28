package com.example.library.domain.book.service;

import com.example.library.domain.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Map<String, Object>> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }
}
