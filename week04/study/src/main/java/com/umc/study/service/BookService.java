// src/main/java/.../service/BookService.java
package com.umc.study.service;

import com.umc.study.dto.request.CreateBookRequest;
import com.umc.study.dto.response.BookResponse;
import com.umc.study.entity.Book;
import com.umc.study.entity.Category;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import com.umc.study.repository.JdbcBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service // 비즈니스 로직을 수행하는 메인 셰프 계층
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcBookRepository jdbcBookRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }

    public List<Map<String, Object>> getBooksByCategory(long categoryId) {
        return jdbcBookRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "존재하지 않는 카테고리입니다."
                ));

        Book book = new Book(category, request.title(), request.description());
        return BookResponse.from(bookRepository.save(book));
    }
}
