package com.umc.study.dto.response;

import com.umc.study.entity.Book;

public record BookResponse(
        Long bookId,
        String title,
        String description,
        String categoryName,
        boolean isAvailable
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getDescription(),
                book.getCategory().getName(),
                book.isAvailable()
        );
    }
}
