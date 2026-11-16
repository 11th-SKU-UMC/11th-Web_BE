# UMC 백엔드 6주차 — Spring Boot

5주차 조회 API를 유지하고 회원가입 및 평점 등록·수정·삭제 API를 추가한 **독립 실행 프로젝트**입니다.

## 준비
- JDK 21, MySQL 8, Gradle 8.x (로컬 설치 또는 IntelliJ Gradle 지원)
- `sql/schema-and-seed.sql`을 MySQL에서 실행 (기존 데이터가 있다면 백업 후 실행)
- 환경 변수 `DB_PASSWORD` 지정. 필요하면 `src/main/resources/application.yml` DB 설정 변경
- `gradle bootRun` 또는 IntelliJ의 `MovieApplication` 실행
- `postman/UMC-backend-week6.postman_collection.json`을 Postman에 Import 후 **변수 `memberId`, `ratingId`를 실제 응답값으로 설정**

## API 명세
| Method | URL | 요청 | 정상 응답 |
|---|---|---|---|
| POST | /members | {email,password,nickname} | 201 + ApiResponse<MemberResponse> |
| POST | /members/{memberId}/ratings | {movieId,score,comment?} | 201 + ApiResponse<ratingId> |
| PATCH | /members/{memberId}/ratings/{ratingId} | {score,comment?} | 200 + ApiResponse<null> |
| DELETE | /members/{memberId}/ratings/{ratingId} | 없음 | 204, 본문 없음 |
| GET | /members/nickname/{nickname} | 없음 | 200 + {available} |
| GET | /members/email/{email} | 없음 | 200 + {available} |
| GET | /members/{memberId}/ratings | 선택: page,size | 200 + 리스트 또는 페이지 |

## 오류 코드
- COMMON4001: Bean Validation 실패 (400)
- MEMBER4091 / MEMBER4092: 이메일 / 닉네임 중복 (409)
- RATING4091: 동일 회원·영화 평점 중복 (409)
- RATING403: 다른 사람의 평점 수정·삭제 (403)
- RATING404 / MEMBER404: 없는 평점 / 회원 (404)
- DB UNIQUE가 사전 검사 이후 실패할 때도 GlobalExceptionHandler가 409 JSON 형태로 변환하도록 구현.
- 로그인 전 실습이므로 memberId는 클라이언트가 임의로 보낼 수 있습니다. **운영용 인증/인가가 아니며 7주차에는 JWT로 사용자 식별을 변경해야 합니다.**

## 제출/검증
1. Postman으로 회원가입 201 및 응답에 password가 없는지 캡처
2. 잘못된 비밀번호/이메일/별점으로 400 JSON 캡처
3. 중복 이메일·닉네임·평점 각각 409 JSON 확인
4. 평점 등록 201, 수정 200, 삭제 204 확인
5. 다른 memberId에서 수정 403, 존재하지 않는 ratingId 수정 404 확인
6. 선택 미션: 반복 또는 동시 가입 요청 중 UNIQUE 오류를 재현할 때 서버 응답 409 확인 (사전 검사 충돌을 매번 강제로 재현할 수 있는 것은 아님)

**실제 캡처/운영 DB 연동은 이 프로젝트에 포함되지 않으며 직접 실행 결과를 기록해야 합니다.** `KEYWORDS_AND_MISSION.md`에 공부 내용과 제출 초안을 수록했습니다.
