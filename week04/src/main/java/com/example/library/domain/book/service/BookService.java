package com.example.library.domain.book.service;

import com.example.library.domain.book.dto.BookCreateRequest;
import com.example.library.domain.book.dto.BookResponse;
import com.example.library.domain.book.entity.Book;
import com.example.library.domain.book.exception.CategoryNotFoundException;
import com.example.library.domain.book.repository.BookRepository;
import com.example.library.domain.category.entity.Category;
import com.example.library.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<BookResponse> getBooks() {
        return bookRepository.findAllByOrderByIdDesc().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(CategoryNotFoundException::new);
        Book book = new Book(category, request.title(), request.description());
        return BookResponse.from(bookRepository.save(book));
    }
}
