package com.example.library.domain.book.service;

import com.example.library.domain.book.exception.BookError;
import com.example.library.domain.book.repository.BookRepository;
import com.example.library.global.exception.error.BusinessException;
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
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(BookError.INVALID_CATEGORY_ID);
        }

        return bookRepository.findByCategoryId(categoryId);
    }
}
