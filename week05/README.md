# UMC 5주차 영화 평점 백엔드 (Spring Boot)

워크북의 필수 API 3종 + 선택 미션 3종(유효성 검사, 페이지네이션, CORS 범위 설명)을 포함한 실습 프로젝트입니다.

## 실행 준비
- Java 21, MySQL 8, Gradle 필요. **Gradle Wrapper는 포함되지 않았습니다.** IntelliJ의 Gradle 기능 또는 설치된 `gradle` 사용.
- MySQL에서 `sql/schema-and-seed.sql` 실행. 이미 생성된 테이블이 있다면 구조를 확인하고 실행하세요.
- `DB_PASSWORD` 환경 변수를 설정하고 `gradle bootRun` 실행. 포트 8080.
- `frontend` 폴더에서 `npx serve -l 5173` 실행. http://localhost:5173 접속.
- SQL의 비밀번호 해시는 실습용 샘플 값이며 실제 로그인에 사용하지 마세요.

## API
| API | 요청 | 정상 응답 |
|---|---|---|
| 닉네임 중복 | GET /members/nickname/수현 | {"available":false} |
| 이메일 중복 | GET /members/email/noco@umc.com | {"available":false} |
| 평점 목록 | GET /members/1/ratings | RatingResponse[] |
| 평점 페이지 | GET /members/1/ratings?page=0&size=10 | content, page, size, totalElements, totalPages, last |

- 닉네임은 2~12자, 이메일 형식이 잘못되면 400; 없는 회원의 평점 조회는 404.
- 클라이언트 포트는 정확히 `http://localhost:5173` 이어야 CORS 허용.

## 제출 인증
아래 캡처는 실제 본인 환경에서 실행 후 촬영해야 합니다(파일에는 포함되지 않음).
1. `entity`, `dto`, `controller`, `service` 핵심 코드
2. Postman에서 위 3개 API 요청 URL과 JSON 응답
3. 프론트에서 3개 API 모두 호출하여 정상 응답 받는 화면
4. 선택 미션: 잘못된 닉네임/이메일의 400, 페이지네이션, CORS 허용/차단 실험

## CORS 비교 실습
1. `WebConfig`에서 `allowedOrigins("http://localhost:5173")` 유지하고 5173은 정상 작동하는지 확인.
2. `allowedOrigins("*")`로 변경 후 서버 재시작: 다른 포트의 브라우저 Origin도 허용될 수 있음(자격 증명 없는 요청 기준).
3. 다시 특정 origin으로 복구하여 제출. **개발 외 환경에서 무조건 `*`를 사용하지 않기.**

## 주의
실제로 이 환경에서 MySQL/Postman/브라우저를 실행하고 캡처한 결과물은 아니므로 직접 작동 검증이 필요합니다.

## Postman 자동 가져오기
Postman > Import > `postman/UMC-week5.postman_collection.json` 선택. `baseUrl=http://localhost:8080`로 구성되어 있습니다.
요청을 Send한 뒤 URL과 응답 Body(JSON), HTTP 상태 코드가 보이게 캡처하세요.

## 브라우저 인증 화면
프론트 파일은 `frontend/index.html` 하나이며 프레임워크나 별도 빌드 없이 동작합니다.
닉네임/이메일/평점 조회 버튼을 각각 눌렀을 때 **요청 URL, HTTP 상태, 응답 JSON**이 화면에 표시됩니다.
세 API가 모두 연동되었다는 증빙을 위해 3번 촬영하거나 화면 녹화를 제출할 수 있습니다(제출 규칙 확인).

## 실행 전 점검
- 반드시 5173 포트에서 프론트를 서빙하세요. HTML을 `file://`로 바로 열면 Origin이 달라 CORS 오류가 날 수 있습니다.
- 서버에서 데이터가 없으면 회원/평점이 기대한 값으로 나오지 않습니다. 먼저 SQL을 실행하세요.
- **Postman 성공만으로는 CORS 검증이 되지 않습니다.** CORS는 실제 브라우저 fetch를 통해 확인해야 합니다.
- `gradle`이 없으면 IntelliJ의 Gradle 패널에서 `bootRun`을 실행하거나 공식 Gradle 설치를 이용하세요.
