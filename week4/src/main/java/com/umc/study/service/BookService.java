package com.umc.study.service;

import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.entity.Book;
import com.umc.study.entity.Category;
import com.umc.study.repository.BookJpaRepository;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    // 기존 3주차 Raw SQL Repository
    private final BookRepository bookRepository;

    // 4주차 JPA Repository
    private final BookJpaRepository bookJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;


    // ===== 기존 3주차 기능 =====

    public List<Map<String, Object>> getAllBooks() {
        return bookRepository.findAll();
    }

    public void createBook(Map<String, Object> body) {
        bookRepository.save(body);
    }

    public List<Map<String, Object>> getBooksByCategoryId(Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }


    // ===== 4주차 실습 1 : GET /books =====

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        return bookJpaRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }


    // ===== 4주차 실습 2 : POST /books =====

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        Category category = categoryJpaRepository.findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Book book = new Book(
                category,
                request.title(),
                request.description()
        );

        return BookResponse.from(bookJpaRepository.save(book));
    }


    // ===== 선택 미션 2 : 도서 제목 검색 =====

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String keyword) {
        return bookJpaRepository.findByTitleContaining(keyword).stream()
                .map(BookResponse::from)
                .toList();
    }
}