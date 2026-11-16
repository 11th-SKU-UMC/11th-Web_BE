# Week 06 API 명세 (Spring Boot)

기준 주소 예시: `http://localhost:8080`

쓰기 API 성공/실패는 `{ isSuccess, code, message, result }` 공통 구조를 사용합니다. DELETE 성공은 워크북 요구사항대로 `204 No Content`이며 응답 본문이 없습니다.

## 회원가입

`POST /members` — `201 Created`

```json
{
  "email": "umc@example.com",
  "password": "Umc!1234",
  "nickname": "moviefan"
}
```

성공 결과에는 `memberId`, `email`, `nickname`만 포함됩니다. 비밀번호는 bcrypt로 해시해 저장하고 응답에는 포함하지 않습니다.

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "회원가입에 성공했습니다.",
  "result": { "memberId": 1, "email": "umc@example.com", "nickname": "moviefan" }
}
```

검증 규칙: 이메일 형식, 비밀번호 대/소문자·숫자·특수문자 포함 및 8자 이상, 닉네임 2~12자. 중복 이메일/닉네임은 각각 409 응답이며 DB UNIQUE 제약 위반도 공통 오류 형식의 409로 변환합니다.

## 평점 등록

`POST /members/{memberId}/ratings` — `201 Created`

```json
{ "movieId": 550, "score": 5, "comment": "좋았어요." }
```

`movieId`는 양수, `score`는 필수 1~5, `comment`는 선택 500자 이하입니다. 회원이 없으면 404, 해당 회원이 같은 영화에 이미 평점을 남겼으면 409입니다. DB에도 `(member_id, movie_id)` 복합 UNIQUE 제약이 있습니다.

## 평점 수정

`PATCH /members/{memberId}/ratings/{ratingId}` — `200 OK`

```json
{ "score": 4, "comment": "다시 보니 더 좋네요." }
```

성공 응답은 공통 구조입니다. 별점은 필수이며 1~5, 후기는 선택 500자 이하입니다. 평점이 없으면 404, 요청 회원이 소유자가 아니면 403입니다.

## 평점 삭제

`DELETE /members/{memberId}/ratings/{ratingId}` — `204 No Content`

평점이 없으면 404, 요청 회원이 소유자가 아니면 403입니다.

## 오류 응답 예시

잘못된 입력은 `400 Bad Request`입니다.

```json
{ "isSuccess": false, "code": "COMMON400", "message": "별점은 5 이하여야 합니다.", "result": null }
```

업무 오류 예시: 이메일 중복 `MEMBER4091`, 닉네임 중복 `MEMBER4092`, 평점 중복 `RATING409`, 소유자 불일치 `RATING403`, 평점 없음 `RATING404`. DB 제약 충돌은 `COMMON409` 형식으로 응답합니다.

## 확인용 요청

```bash
curl -i -X POST http://localhost:8080/members \
  -H 'Content-Type: application/json' \
  -d '{"email":"umc@example.com","password":"Umc!1234","nickname":"moviefan"}'

curl -i -X POST http://localhost:8080/members/1/ratings \
  -H 'Content-Type: application/json' \
  -d '{"movieId":550,"score":5,"comment":"좋았어요."}'

curl -i -X PATCH http://localhost:8080/members/1/ratings/1 \
  -H 'Content-Type: application/json' \
  -d '{"score":4,"comment":"다시 보니 더 좋네요."}'

curl -i -X DELETE http://localhost:8080/members/1/ratings/1
```

직접 실행한 요청/응답 캡처와 본인 환경에서 겪은 트러블슈팅 기록은 미션 제출 자료로 추가하세요.
