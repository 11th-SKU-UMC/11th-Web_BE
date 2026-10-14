package com.example.library.domain.book.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super(BookError.CATEGORY_NOT_FOUND.getMessage());
    }
}
