package com.example.library.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
        @NotNull(message = "categoryId는 필수입니다.")
        @Positive(message = "categoryId는 양수여야 합니다.")
        Long categoryId,

        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description
) {
}
