package com.umc.study.controller;

import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 4주차 실습 1 + 선택 미션 2
    @GetMapping
    public List<BookResponse> getBooks(
            @RequestParam(required = false) String keyword) {

        if (keyword != null && !keyword.isBlank()) {
            return bookService.searchBooks(keyword);
        }

        return bookService.getBooks();
    }

    // 4주차 실습 2 - 도서 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(
            @Valid @RequestBody CreateBookRequest request) {
        return bookService.createBook(request);
    }

    // 기존 3주차 카테고리별 조회
    @GetMapping("/category/{categoryId}")
    public List<Map<String, Object>> getBooksByCategoryId(
            @PathVariable Long categoryId) {

        return bookService.getBooksByCategoryId(categoryId);
    }
}