package com.umc.study.controller;

import com.umc.study.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public String createRental(@RequestBody Map<String, Object> body) {

        rentalService.createRental(body);

        return "도서 대여 기록이 등록되었습니다!";
    }

    @PatchMapping("/{rentalId}/return")
    public String returnRental(@PathVariable Long rentalId) {
        rentalService.returnRental(rentalId);

        return "도서 반납 처리가 완료되었습니다!";
    }
}