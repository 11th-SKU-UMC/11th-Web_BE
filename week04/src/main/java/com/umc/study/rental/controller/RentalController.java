package com.umc.study.rental.controller;

import com.umc.study.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // POST /rentals : 성공 시 201 Created와 생성된 대여 기록(JSON)을 반환합니다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createRental(@RequestBody Map<String, Object> body) {
        return rentalService.createRental(body);
    }
}