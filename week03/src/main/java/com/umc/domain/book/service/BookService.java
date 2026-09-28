package com.umc.domain.book.service;

import com.umc.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Map<String, Object>> getBooksByCategoryId(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }
}