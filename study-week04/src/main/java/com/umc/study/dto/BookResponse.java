package com.umc.study.dto;

import com.umc.study.entity.Book;

public record BookResponse(
        Long bookId,
        String categoryName,
        String title,
        String description,
        boolean isAvailable
) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getCategory().getName(),
                book.getTitle(), book.getDescription(), book.isAvailable());
    }
}
