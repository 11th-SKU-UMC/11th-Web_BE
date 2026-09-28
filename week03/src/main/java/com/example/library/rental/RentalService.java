package com.example.library.rental;

import org.springframework.stereotype.Service;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public void createRental(RentalRequest request) {
        if (request.userId() == null || request.bookId() == null) {
            throw new IllegalArgumentException("userId와 bookId는 필수입니다.");
        }
        rentalRepository.save(request);
    }
}
