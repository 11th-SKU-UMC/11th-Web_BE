package com.umc.study;

import com.umc.study.dto.CreateBookRequest;
import com.umc.study.repository.BookRepository;
import com.umc.study.repository.CategoryRepository;
import com.umc.study.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {

    @Test
    void createBookRejectsUnknownCategoryWithoutSaving() {
        BookRepository bookRepository = mock(BookRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        BookService bookService = new BookService(bookRepository, categoryRepository);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookService.createBook(new CreateBookRequest(999L, "새 도서", "내용"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(bookRepository, never()).save(any());
    }
}
