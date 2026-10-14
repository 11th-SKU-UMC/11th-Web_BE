package com.example.library.domain.book;

import com.example.library.domain.book.entity.Book;
import com.example.library.domain.book.repository.BookRepository;
import com.example.library.domain.category.entity.Category;
import com.example.library.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        category = categoryRepository.save(new Category("문학"));
    }

    @Test
    void 도서를_최신순으로_조회한다() throws Exception {
        Book first = bookRepository.save(new Book(category, "첫 번째 책", "설명 1"));
        Book latest = bookRepository.save(new Book(category, "최신 책", "설명 2"));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(latest.getId()))
                .andExpect(jsonPath("$[0].title").value("최신 책"))
                .andExpect(jsonPath("$[0].description").value("설명 2"))
                .andExpect(jsonPath("$[0].categoryName").value("문학"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(first.getId()));
    }

    @Test
    void 도서를_등록하면_201을_반환한다() throws Exception {
        String request = """
                {"categoryId": %d, "title": "JPA 입문", "description": "영속성 컨텍스트"}
                """.formatted(category.getId());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("JPA 입문"))
                .andExpect(jsonPath("$.categoryName").value("문학"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void 제목이_비어_있으면_400을_반환한다() throws Exception {
        String request = """
                {"categoryId": %d, "title": " ", "description": "설명"}
                """.formatted(category.getId());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("G_001"))
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    void 없는_카테고리면_404를_반환한다() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId": 999999, "title": "새 책", "description": "설명"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("B_001"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다."));
    }
}
