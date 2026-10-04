package com.umc.domain.book.service;

import com.umc.domain.book.dto.BookResponse;
import com.umc.domain.book.dto.CreateBookRequest;
import com.umc.domain.book.entity.Book;
import com.umc.domain.book.entity.Category;
import com.umc.domain.book.repository.BookRepository;
import com.umc.domain.book.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks() {
        return bookRepository.findAllByOrderByBookIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Book book = new Book(category, request.title(), request.description());
        Book savedBook = bookRepository.save(book);

        return BookResponse.from(savedBook);
    }
}