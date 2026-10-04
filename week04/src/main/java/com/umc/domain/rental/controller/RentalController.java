package com.umc.domain.rental.controller;

import com.umc.domain.rental.dto.CreateRentalRequest;
import com.umc.domain.rental.dto.RentalResponse;
import com.umc.domain.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse createRental(@Valid @RequestBody CreateRentalRequest request) {
        return rentalService.createRental(request);
    }
}