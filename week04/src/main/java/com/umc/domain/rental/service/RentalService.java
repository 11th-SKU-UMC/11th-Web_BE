package com.umc.domain.rental.service;

import com.umc.domain.book.entity.Book;
import com.umc.domain.book.repository.BookRepository;
import com.umc.domain.rental.dto.CreateRentalRequest;
import com.umc.domain.rental.dto.RentalResponse;
import com.umc.domain.rental.entity.Rental;
import com.umc.domain.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;

    @Transactional
    public RentalResponse createRental(CreateRentalRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));

        if (!book.getIsAvailable()) {
            throw new IllegalArgumentException("이미 대여 중이거나 대여 불가능한 도서입니다.");
        }

        // 도서 대여 상태 변경 (변경 감지 적용)
        book.updateAvailability(false);

        // 대여 정보 저장
        Rental rental = new Rental(request.userId(), book);
        Rental savedRental = rentalRepository.save(rental);

        return RentalResponse.from(savedRental);
    }
}