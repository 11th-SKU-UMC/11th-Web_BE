package com.example.library.domain.rental.service;

import com.example.library.domain.rental.exception.RentalError;
import com.example.library.domain.rental.repository.RentalRepository;
import com.example.library.global.exception.error.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public void createRental(Map<String, Object> body) {
        if (!(body.get("userId") instanceof Number userId) || userId.longValue() <= 0
                || !(body.get("bookId") instanceof Number bookId) || bookId.longValue() <= 0) {
            throw new BusinessException(RentalError.INVALID_RENTAL_REQUEST);
        }

        try {
            rentalRepository.save(body);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(RentalError.USER_OR_BOOK_NOT_FOUND);
        }
    }
}
