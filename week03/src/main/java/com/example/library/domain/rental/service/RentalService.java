package com.example.library.domain.rental.service;

import com.example.library.domain.rental.repository.RentalRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public void createRental(Map<String, Object> body) {
        rentalRepository.save(body);
    }
}
