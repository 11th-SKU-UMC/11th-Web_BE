# 9주차 필수 미션 — Spring Boot

기존 영화 평점 서비스에 적용하는 예시 패치입니다. 기존 7~8주차 소스가 제공되지 않아 Rating 관련 타입 및 메서드 이름은 가정했습니다. 실제 프로젝트에 맞게 수정하세요. 실행 및 캡처는 수행하지 않았습니다.

## 미션 기록
1. springdoc 의존성을 추가하고 SwaggerConfig에 bearerAuth를 등록한다.
2. 기존 WebConfig의 인터셉터 제외 목록에 `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`를 추가한다.
3. swagger-annotations.java.txt의 어노테이션을 기존 컨트롤러 메서드에 붙인다. 공개 유저별 평점 조회에는 SecurityRequirement를 붙이지 않는다. 실제 경로와 응답 코드는 기존 코드에서 확인한다.
4. RatingServiceTest.java의 두 단위 테스트를 추가한다. 중복 등록과 다른 회원의 평점 수정 거부를 확인한다.
5. RatingApiTest.java의 두 통합 테스트를 추가한다. H2 테스트 설정을 적용한다.
6. Windows에서는 `gradlew.bat test`, macOS/Linux에서는 `./gradlew test`를 실행한다.

## 제출용 캡처 (직접 실행 후 첨부)
- Swagger 전체 목록 및 평점 API 상세 화면
- 평점 단위/통합 테스트 코드
- 전체 테스트 성공 화면

## 기대 결과
| 테스트 | 기대 결과 |
|---|---|
| 중복 평점 등록 | RATING409_1 |
| 타인의 평점 수정 | RATING403_1 |
| 토큰 없이 POST /ratings | 401 / AUTH401_2 |
| 토큰 포함 POST /ratings | 201 / COMMON201 |

선택 미션은 포함하지 않았습니다. 테스트 코드의 예외 getter, DTO 생성자, Repository 메서드는 기존 프로젝트와 일치하도록 확인해야 합니다.
