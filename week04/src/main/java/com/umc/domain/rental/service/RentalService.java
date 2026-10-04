package com.umc.domain.rental.service;

import com.umc.domain.book.repository.BookRepository;
import com.umc.domain.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository; // BookRepository 주입

    public void createRental(Map<String, Object> body) {
        Long bookId = Long.valueOf(body.get("bookId").toString());

        boolean isAvailable = bookRepository.isBookAvailable(bookId);
        if (!isAvailable) {
            throw new IllegalArgumentException("이미 대여 중이거나 대여 불가능한 도서입니다.");
        }

        rentalRepository.saveRental(body);

        bookRepository.updateBookAvailability(bookId, false);
    }
}