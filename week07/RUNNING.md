# Week 07 실행 준비 (Spring Boot)

IntelliJ에서 `week07`을 별도 Gradle 프로젝트로 열고 `MovieApplication`을 실행합니다.

실행 구성의 Environment variables에 로컬 설정을 넣어주세요.

- `DB_PASSWORD`: 로컬 MySQL `root` 비밀번호
- `JWT_SECRET`: HMAC 서명용 비밀 문자열 (최소 32바이트; 저장소에 커밋 금지)
- `JWT_ACCESS_EXPIRATION`: Access Token 유효 시간(초), 예: `3600`

`application.yaml`은 MySQL의 `movie` 데이터베이스를 사용합니다. DB와 계정이 준비되어 있어야 합니다. `ddl-auto: validate`이므로 기존 테이블 스키마가 엔티티와 일치해야 합니다.

Postman에서 로그인은 `POST /auth/login`으로 호출하고, 응답의 `result.accessToken`을 복사합니다. 보호 경로 요청마다 Headers에 `Authorization: Bearer {토큰}`을 설정합니다. 평점 쓰기 요청의 URL은 `/ratings` 또는 `/ratings/{ratingId}`이며 요청 바디에 `memberId`를 넣지 않습니다.
