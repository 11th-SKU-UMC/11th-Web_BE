package com.umc.domain.rental.entity;

import com.umc.domain.book.entity.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "rented_at", nullable = false)
    private LocalDateTime rentedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    public Rental(Long userId, Book book) {
        this.userId = userId;
        this.book = book;
        this.rentedAt = LocalDateTime.now();
        this.dueAt = this.rentedAt.plusDays(7);
    }
}