package com.umc.study.service;

import com.umc.study.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;

    public Map<String, Object> createRental(Map<String, Object> body) {
        // INSERT 후 생성된 id로 다시 조회해서, DB가 채운 rented_at/due_at까지 응답에 담습니다.
        Long rentalId = rentalRepository.save(body);
        return rentalRepository.findById(rentalId);
    }
}