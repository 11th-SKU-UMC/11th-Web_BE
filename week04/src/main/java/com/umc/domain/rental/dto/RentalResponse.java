package com.umc.domain.rental.dto;

import com.umc.domain.rental.entity.Rental;

import java.time.LocalDateTime;

public record RentalResponse(
        Long rentalId,
        Long userId,
        Long bookId,
        String bookTitle,
        LocalDateTime rentedAt,
        LocalDateTime dueAt
) {
    public static RentalResponse from(Rental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getUserId(),
                rental.getBook().getBookId(),
                rental.getBook().getTitle(),
                rental.getRentedAt(),
                rental.getDueAt()
        );
    }
}