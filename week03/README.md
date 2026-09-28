# Week 03 Spring API

`JdbcTemplate`을 사용해 카테고리별 도서 조회와 신규 대여 생성을 구현한 Spring Boot 프로젝트입니다.

## 실행 준비

1. MySQL에서 `week02/datainit.sql`을 실행해 `umc2` 데이터베이스와 테이블을 만듭니다.
2. IntelliJ에서 이 `week03` 폴더(또는 `build.gradle`)를 엽니다.
3. Gradle 의존성 로딩이 끝나면 `LibraryApplication`을 실행합니다.

기본 연결 정보는 `localhost:3306/umc2`, 사용자 `root`, 빈 비밀번호입니다. 다른 환경에서는 다음 환경변수를 지정합니다.

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

MySQL의 `root` 계정에 비밀번호가 설정되어 있다면 IntelliJ 실행 구성의 환경 변수에
`DB_PASSWORD=본인의_비밀번호`를 추가해야 합니다. 또는 `application.properties`의
`spring.datasource.password` 기본값을 로컬 비밀번호로 변경할 수 있습니다.

`.env` 파일을 쓰려면 `.env.example`을 복사해 `week03/.env`로 만들고 실제 비밀번호를
입력합니다. IntelliJ 실행 구성의 Working directory는 `week03` 폴더여야 합니다.

터미널에서는 포함된 Gradle Wrapper로 실행할 수 있습니다.

```powershell
.\gradlew.bat bootRun
```

## API 확인

카테고리 1의 도서 조회:

```http
GET http://localhost:8080/books/category/1
```

대여 기록 생성:

```http
POST http://localhost:8080/rentals
Content-Type: application/json

{
  "userId": 1,
  "bookId": 1
}
```

성공 시 HTTP 201과 `도서 대여가 완료되었습니다.`를 반환합니다.
