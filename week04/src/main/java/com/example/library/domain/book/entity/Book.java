package com.example.library.domain.book.entity;

import com.example.library.domain.category.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    protected Book() {}

    public Book(Category category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isAvailable() { return available; }
}
