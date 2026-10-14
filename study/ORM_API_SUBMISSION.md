# ORM 기반 도서 API 실습 제출

## 구현 요약

- Entity: `src/main/java/com/umc/study/entity/Book.java`, `Category.java`
- DTO: `src/main/java/com/umc/study/dto/CreateBookRequest.java`, `BookResponse.java`
- Repository: `src/main/java/com/umc/study/repository/BookRepository.java`, `CategoryRepository.java`
- Service / Controller: `src/main/java/com/umc/study/service/BookService.java`, `src/main/java/com/umc/study/controller/BookController.java`
- Postman collection: `postman/ORM Book API.postman_collection.json`

`GET /books`는 `bookId` 내림차순으로 조회하며 `bookId`, `title`, `description`, `categoryName`, `isAvailable`만 응답합니다. `POST /books`는 `categoryId`, `title`, `description`을 받고 제목과 카테고리 ID를 검증합니다. 등록에 성공하면 응답 DTO와 `201 Created`를 반환하고, 없는 카테고리 ID는 저장하지 않고 `404 Not Found`를 반환합니다.

## Raw SQL 방식과 달라진 점

기존 Raw SQL 방식은 Repository에서 SQL 문자열을 작성하고 `JdbcTemplate`으로 결과를 `Map` 형태로 반환했습니다. ORM 방식에서는 `Book`과 `Category` 엔티티의 `@ManyToOne` 관계를 통해 외래 키를 표현하고, `JpaRepository`의 파생 쿼리로 도서를 최신순 조회합니다. 요청·응답 DTO를 사용해 검증과 응답 필드를 API 경계에서 관리하며, 등록은 카테고리 엔티티를 확인한 뒤 `Book`을 저장합니다. `RentalRepository`는 이 실습 범위 밖이므로 기존 JDBC 방식을 유지합니다.

## 요청 및 예상 결과

Postman collection의 `GET /books`, `POST /books`, 빈 제목, 없는 카테고리 요청으로 실행 결과를 확인할 수 있습니다. Collection 변수 `baseUrl`은 로컬 API 주소로, `categoryId`는 실제 존재하는 카테고리 ID로 설정해야 합니다.

성공 응답 예시:

```json
{
  "bookId": 12,
  "title": "Postman ORM 도서",
  "description": "Postman으로 등록한 도서",
  "categoryName": "소설",
  "isAvailable": true
}
```

등록 요청은 `201 Created`, 빈 제목 요청은 `400 Bad Request`, 존재하지 않는 카테고리 요청은 `404 Not Found`를 기대합니다. Postman collection은 이 상태 코드와 응답 필드를 검사합니다.

## 검증 결과

검증 문장: MockMvc 및 서비스 단위 테스트에서 GET 응답 필드, POST `201`, 빈 제목 `400`, 없는 카테고리 `404`와 저장 생략을 확인해 API 계약이 요구사항과 일치합니다.

DB에 연결하는 기존 `StudyApplicationTests.contextLoads()`는 이 환경에 DB 접속 정보가 없어 실패했으므로, 실제 MySQL/Postman 실행 결과는 DB 설정 후 collection을 실행해 캡처해야 합니다.
