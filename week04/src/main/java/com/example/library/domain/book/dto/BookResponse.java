package com.example.library.domain.book.dto;

import com.example.library.domain.book.entity.Book;

public record BookResponse(
        Long id,
        String title,
        String description,
        String categoryName,
        boolean available
) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getDescription(),
                book.getCategory().getName(), book.isAvailable());
    }
}
