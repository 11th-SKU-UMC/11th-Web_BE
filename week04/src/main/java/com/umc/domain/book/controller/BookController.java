package com.umc.domain.book.controller;

import com.umc.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/category/{categoryId}")
    public List<Map<String, Object>> getBooksByCategory(@PathVariable Long categoryId) {
        return bookService.getBooksByCategoryId(categoryId);
    }
}