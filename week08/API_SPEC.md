# Week 07 API 명세 (Spring Boot)

쓰기/회원 API는 공통 `{ isSuccess, code, message, result }` 구조를 사용합니다. 보호 API에는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

## 로그인 — `POST /auth/login` (200)
```json
{"email":"a@umc.com","password":"Umc!1234"}
```
성공 result: `{ "nickname": "영화광", "accessToken": "..." }`. 존재하지 않는 이메일과 잘못된 비밀번호는 동일하게 `401 AUTH401_1`로 응답합니다.

## 내 정보 — `GET /members/me` (200)
인증 헤더 필요. 결과는 `email`, `nickname`, `createdAt`이며 비밀번호는 노출하지 않습니다. 헤더 누락 시 `401 AUTH401_2`.

## 내 닉네임 수정 — `PATCH /members/me` (200)
```json
{"nickname":"newname"}
```
인증 헤더 필요. 중복 닉네임은 `409 MEMBER409_1`.

## 평점 쓰기 API (인증 헤더 필요)
- `POST /ratings` — 201, body는 6주차 `CreateRatingRequest`, memberId는 넣지 않습니다. result는 `RatingResponse`입니다.
- `PATCH /ratings/{ratingId}` — 200, body는 6주차 `UpdateRatingRequest`, 소유자가 아니면 `403 RATING403_1`, 없으면 `404 RATING404_1`.
- `DELETE /ratings/{ratingId}` — 200, result null.
- 같은 회원·영화 평점 중복은 `409 RATING409_1`.

공개 조회 `GET /members/{memberId}/ratings`와 회원가입·중복 확인은 인증이 필요 없습니다.

## 공통 인증 실패 코드
- 로그인 정보 오류 `AUTH401_1`
- Authorization 헤더 누락/형식 오류 `AUTH401_2`
- 위조되거나 유효하지 않은 토큰 `AUTH401_3`
- 만료 토큰 `AUTH401_4`

JWT 설정은 환경 변수 `JWT_SECRET`(32바이트 이상)과 `JWT_ACCESS_EXPIRATION`(초, 기본값 3600)을 사용합니다. 실행 환경에 설정하고 비밀키를 저장소에 커밋하지 마세요.
