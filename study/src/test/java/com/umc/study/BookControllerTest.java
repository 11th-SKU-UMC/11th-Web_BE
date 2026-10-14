package com.umc.study;

import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.controller.BookController;
import com.umc.study.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void getBooksReturnsTheResponseDtoInServiceOrder() throws Exception {
        when(bookService.getBooks()).thenReturn(java.util.List.of(
                new BookResponse(10L, "최신 도서", "설명", "소설", true),
                new BookResponse(8L, "이전 도서", null, "과학", false)
        ));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(10))
                .andExpect(jsonPath("$[0].title").value("최신 도서"))
                .andExpect(jsonPath("$[0].categoryName").value("소설"))
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andExpect(jsonPath("$[0].categoryId").doesNotExist())
                .andExpect(jsonPath("$[1].bookId").value(8));
    }

    @Test
    void createBookRejectsBlankTitleBeforeCallingService() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":1,"title":"  ","description":"내용"}
                                """))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any(CreateBookRequest.class));
    }

    @Test
    void createBookReturnsNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(bookService.createBook(any(CreateBookRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "존재하지 않는 카테고리입니다."
                ));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":999,"title":"새 도서","description":"내용"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookReturnsCreatedWithResponseBody() throws Exception {
        when(bookService.createBook(any(CreateBookRequest.class)))
                .thenReturn(new BookResponse(10L, "새 도서", "내용", "소설", true));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":1,"title":"새 도서","description":"내용"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(10))
                .andExpect(jsonPath("$.categoryName").value("소설"));

        verify(bookService).createBook(any(CreateBookRequest.class));
    }
}
