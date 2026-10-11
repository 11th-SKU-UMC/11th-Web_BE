# Week 05 · Movie API

Spring Boot 3.5, Java 17, Spring Data JPA, MySQL로 회원 중복 확인과 회원별 평점 조회 API를 구현했다.

## 실행 준비

1. MySQL에서 [`schema.sql`](./schema.sql)을 실행하여 `umcine` 스키마와 `member`, `rating`, `favorite` 테이블을 만든다. `favorite`는 다음 주차 실습용으로 스키마만 준비했다.
2. IntelliJ의 `MovieApplication` 실행 설정에 `DB_PASSWORD` 환경변수를 등록한다. [`application.yaml`](./src/main/resources/application.yaml)은 `jdbc:mysql://127.0.0.1:3306/umcine`에 접속하며, JPA의 `ddl-auto: validate`로 테이블 구조를 확인한다.
3. `./gradlew bootRun` 또는 IntelliJ에서 `MovieApplication`을 실행한다.

실제 DB 비밀번호나 외부 API Key는 코드와 Git에 기록하지 않는다.

## 구현 내용

- `entity`: `BaseTimeEntity`가 생성·수정 시각을 관리하고, `Member`와 `Rating`이 테이블을 매핑한다. 평점은 회원과 영화의 조합에 UNIQUE 제약을 둔다.
- `repository`: 회원의 닉네임·이메일 중복 여부와 회원별 평점을 조회한다.
- `service`와 `controller`: 닉네임·이메일 사용 가능 여부, 회원별 평점 목록을 제공한다. 존재하지 않는 회원은 404를 반환한다.
- `dto`: 중복 확인 결과와 평점 응답, 페이지 정보를 API 응답 형식으로 전달한다.
- 입력 검증: 닉네임은 2~12자, 이메일은 이메일 형식이어야 하며 유효하지 않으면 400을 반환한다.
- 페이지네이션: `page`는 0부터 시작하고 `size`는 1~100으로 제한한다. 평점은 `ratingId` 내림차순으로 조회한다.

| 메서드 | 경로 | 기능 |
| --- | --- | --- |
| GET | `/members/nickname/{nickname}` | 닉네임 사용 가능 여부 `{ "available": true }` |
| GET | `/members/email/{email}` | 이메일 사용 가능 여부 `{ "available": true }` |
| GET | `/members/{memberId}/ratings?page=0&size=10` | 평점 목록과 `page`, `size`, `totalElements`, `totalPages` |

## 화면 연동과 CORS

[`demo/`](./demo/) 화면이 위 세 API를 실제로 호출한다. Spring Boot 실행 후 `movie/demo` 디렉터리에서 `python3 -m http.server 5173 --bind 127.0.0.1`을 실행하고 `http://localhost:5173`을 연다. 정상 응답, 페이지네이션, CORS 차단 화면은 [데모 설명](./demo/README.md)에 캡처해 두었다.

[`WebConfig`](./src/main/java/com/umc/movie/config/WebConfig.java)는 `/members/**`의 GET 요청에 대해 `http://localhost:5173`만 허용한다. 모든 출처(`*`)를 허용하면 어느 웹사이트의 브라우저 코드에서도 API 응답을 읽을 수 있지만, 특정 출처만 허용하면 허용된 화면에서만 응답을 읽을 수 있다. `localhost`와 `127.0.0.1`은 호스트가 달라 서로 다른 출처다. CORS는 브라우저의 응답 접근을 제어하며 사용자 인증을 대신하지 않는다.

## 학습 정리

- **SOP와 CORS**: SOP는 브라우저가 다른 출처의 응답을 임의로 읽지 못하게 한다. CORS는 서버가 허용한 출처의 브라우저 코드에 응답 접근을 허용한다.
- **Same-Origin과 Same-Site**: Same-Origin은 프로토콜·호스트·포트가 모두 같아야 한다. Same-Site는 프로토콜과 등록 가능 도메인을 기준으로 판단하므로 같은 사이트여도 출처는 다를 수 있다.
- **쿠키의 SameSite**: `Strict`는 동일 사이트 요청에만 쿠키를 보내고, `Lax`는 일부 최상위 페이지 이동도 허용한다. `None`은 다른 사이트 요청에도 쿠키를 보내며 `Secure`가 필요하다.
- **복합 UNIQUE**: `UNIQUE(member_id, movie_id)`는 회원과 영화의 조합 중복만 막는다. 한 회원이 여러 영화를 평가하거나 여러 회원이 같은 영화를 평가하는 것은 허용하면서 동일 회원의 동일 영화 중복 평점은 방지한다.

## 외부 API Key 관리

현재 프로젝트에는 외부 영화 Open API 연동을 구현하지 않았다. 이후 연동한다면 프론트엔드는 우리 백엔드 API만 호출하고, 백엔드 Service가 외부 API에 Key를 넣어 요청한 뒤 필요한 데이터만 반환한다. 브라우저에 전달되는 JavaScript나 `VITE_` 환경변수에 비밀 Key를 넣지 않는다.

로컬에서는 `DB_PASSWORD`처럼 `MOVIE_API_KEY`를 IntelliJ 환경변수로 등록하고, `application.yaml`에서 `${MOVIE_API_KEY}`를 참조한다. AWS에 배포할 때는 Secrets Manager에 비밀 값을 보관하고 서버 실행 역할에만 읽기 권한을 준다. Secrets Manager는 KMS를 사용해 저장된 값을 암호화한다. SSM Parameter Store의 `SecureString`도 대안이며, CloudHSM은 전용 하드웨어에서 암호화 키를 직접 관리해야 할 때 사용한다.

## 검증

`./gradlew test`로 닉네임·이메일 형식 검증과 평점 페이지 요청을 확인한다. [`demo/README.md`](./demo/README.md)에 세 API의 정상 응답 및 CORS 허용 범위 확인 결과를 기록했다.
