package com.example.library.domain.rental.controller;

import com.example.library.domain.rental.service.RentalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/rentals")
    public String createRental(@RequestBody Map<String, Object> body) {
        rentalService.createRental(body);
        return "도서 대여가 완료되었습니다.";
    }
}
