# UMC 백엔드 6주차 핵심 키워드 · 미션 기록

## 핵심 키워드

### 1. Bean Validation과 @Valid
Bean Validation은 DTO 필드에 `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max` 같은 제약을 선언해 요청 데이터를 검사하는 표준이다. Spring MVC 컨트롤러의 `@Valid @RequestBody`가 이를 실행하며, 실패하면 `MethodArgumentNotValidException`이 발생한다. 검증 실패는 전역 예외 처리로 HTTP 400과 공통 JSON을 반환한다. DTO 형식 검증은 DB 중복 방지나 소유자 검증을 대체하지 않는다.

### 2. class-validator와 ValidationPipe
이는 **NestJS 스택**에서 사용하는 대안이다. `class-validator`의 `@IsEmail()`, `@Min()` 등으로 DTO를 선언하고 전역 `ValidationPipe`로 검사한다. `whitelist`는 선언되지 않은 속성을 제거하고 `forbidNonWhitelisted`는 이를 오류로 돌릴 수 있다. 이번 제출 구현은 Spring Boot이므로 해당 라이브러리를 코드에 혼용하지 않았다.

### 3. bcrypt와 salt round
bcrypt는 비밀번호를 복호화할 수 없는 해시로 변환하며, 같은 비밀번호라도 각기 다른 랜덤 salt에 의해 다른 해시가 만들어진다. `BCryptPasswordEncoder(10)`에서 10은 비용 계수(cost factor)로, 주요 계산량이 대략 `2^10` 단위로 증가한다. 값이 높을수록 추측 공격 비용이 증가하지만 로그인·가입 처리 비용도 증가한다. DB에는 해시만 저장하고 응답에는 비밀번호/해시를 포함하지 않는다.

### 4. HTTP 201 / 204 / 409
`201 Created`는 새 리소스 생성이 성공했음을 뜻하며 회원가입과 평점 등록에 사용한다. `204 No Content`는 성공했으나 응답 본문이 없으므로 평점 삭제에 적합하다. `409 Conflict`는 현재 리소스 상태와 충돌해 요청을 처리할 수 없는 경우로, 중복 회원·중복 평점에 사용한다. 204에는 공통 JSON을 억지로 넣지 않는다.

### 5. UNIQUE 제약 조건과 동시성
서비스에서 `existsByEmail`로 중복을 확인해도 두 요청이 동시에 검사하면 둘 다 '중복 없음'을 볼 수 있다. 이 경우 DB UNIQUE가 최종적으로 하나의 INSERT만 허용한다. `(member_id,movie_id)` 복합 UNIQUE는 같은 회원의 동일 영화 평점 등록을 막는다. DB 예외는 `DataIntegrityViolationException` 등을 통해 잡아 409로 변환한다. 원자적 유일성 보장이 핵심이며 사전 검사만으로 해결할 수 없다.

### 6. @RestControllerAdvice / Exception Filter
`@RestControllerAdvice`와 `@ExceptionHandler`는 Spring에서 컨트롤러 전반의 예외를 한곳에 모아 HTTP 상태·코드·메시지를 일관되게 반환한다. NestJS는 `ExceptionFilter`로 같은 역할을 구현한다. 이번 프로젝트는 Spring의 전역 예외 처리로 검증 오류(400), 소유권 오류(403), 미존재(404), 중복(409)을 다룬다. 예상치 못한 내부 오류의 상세 정보는 클라이언트에 노출하지 않는다.

## 필수 미션 기록
- `SignUpRequest`: 이메일 형식, 비밀번호 복잡도, 닉네임 2~12자 검증
- `MemberService.signUp`: 중복 검사→bcrypt 해시→`saveAndFlush`→비밀번호 제외 응답 DTO
- `RatingService`: 존재 여부 및 작성자 소유권 확인, 등록·수정·삭제
- `ApiResponse`: 성공·실패 동일 필드 `isSuccess`, `code`, `message`, `result`
- `GlobalExceptionHandler`: 400/403/404/409 JSON 및 DB UNIQUE 오류 매핑
- 제출할 실제 요청·응답 캡처: 회원가입 201, 검증 오류 400, 평점 201/200/204, 중복 409, 타인 수정 403, 없는 평점 404

## 선택 미션 기록
- 사전 검사 이메일 중복 → `MEMBER4091` / '이미 사용 중인 이메일입니다.'
- 사전 검사 닉네임 중복 → `MEMBER4092` / '이미 사용 중인 닉네임입니다.'
- 동시 저장 중 DB UNIQUE 실패도 전역 처리에서 키 이름을 파악해 동일 코드와 메시지를 반환한다.
- 중복 검사를 통과해도 다른 트랜잭션이 먼저 INSERT하면 UNIQUE 위반이 발생할 수 있으므로 DB 제약은 필수다.

## 트러블슈팅 기록 예시 (실제 발생 여부 확인 후 수정)
- 이슈: 같은 이메일로 동시에 회원가입할 때 사전 중복 조회로만 처리하면 중복 저장 위험이 있다.
- 원인: 두 트랜잭션이 서로의 INSERT 전 상태를 읽을 수 있다.
- 해결: 이메일/닉네임 컬럼에 UNIQUE를 설정하고 `DataIntegrityViolationException`을 409 공통 응답으로 변환했다.
- 배운 점: 사전 조회는 사용자 친화적 메시지에 도움이 되고, 최종 무결성은 DB가 보장한다.
- 주의: 위 항목은 **구현 설계 설명**이며 직접 겪은 실행 오류로 주장하지 말고, 테스트 시 실제 관찰 내용으로 수정한다.

## 학습 후기 초안
이번 6주차에는 조회 중심의 API에 회원가입과 평점 CRUD 중 쓰기 기능을 추가하면서 요청 검증부터 데이터베이스 제약 조건까지 저장 API의 전체 흐름을 학습했다. 특히 비밀번호를 bcrypt 해시로 저장하고 응답에서 제외해야 하는 이유를 이해했다. 또한 DTO 검증과 서비스 중복 검사가 있어도 동시 요청에는 DB UNIQUE 제약이 필요하다는 점을 배웠고, 전역 예외 처리로 프론트엔드가 오류를 일관되게 해석할 수 있도록 구성했다.
