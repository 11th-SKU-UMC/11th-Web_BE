# 4주차 ORM 실습

3주차 `study` 프로젝트를 복사해 `book`·`category`를 JPA 엔티티로 연결한 독립 작업본입니다. 원본의 `JdbcTemplate` 코드는 원본 프로젝트에 그대로 남아 있습니다.

## 실행

1. 이 폴더를 IntelliJ에서 Gradle 프로젝트로 엽니다.
2. `StudyApplication` 실행 구성에 `DB_URL`, `DB_USER`, `DB_PW`가 설정되어 있는지 확인합니다. 복사된 IntelliJ 설정에는 기존 값이 들어 있습니다.
3. 로컬 MySQL을 실행하고 `StudyApplication`을 실행합니다. `ddl-auto: validate`가 기존 `book`·`category` 테이블을 검사하며 테이블을 새로 만들지 않습니다.

## API

- `GET /books`: 도서 목록을 JSON으로 반환합니다.
- `POST /books`: `{ "categoryId": 1, "title": "새 책", "description": "설명" }`을 보내면 도서를 등록하고 201과 등록된 도서를 반환합니다.

Repository는 `JpaRepository`를 사용하며 SQL을 직접 작성하지 않습니다. HTTP 요청과 응답은 DTO로 다뤄 엔티티와 DB 컬럼명을 API에 직접 노출하지 않습니다.
