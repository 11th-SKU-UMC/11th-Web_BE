# UMC 7주차 Spring Boot — JWT 인증·인가

Java 17 이상 / Spring Boot 3.5.7 / Maven Wrapper / JPA / bcrypt / JJWT 0.12.6.
기존 6주차 소스 없이 실행할 수 있도록 회원가입과 공개 평점 조회도 포함했습니다.
기본 DB는 파일 H2이며 서버 재시작 후에도 회원과 평점이 유지됩니다.

## 실행 (Windows PowerShell)

프로젝트 폴더에서 실행합니다. JDK 17 이상을 설치하고 JAVA_HOME을 설정하세요.

```powershell
$env:JWT_SECRET = "여기에_매번_새로_생성한_충분히_긴_랜덤키를_입력하세요"
$env:JWT_ACCESS_EXPIRATION = "3600"
.\mvnw.cmd spring-boot:run
```

JWT_SECRET은 UTF-8 기준 32바이트 이상의 랜덤 키를 사용하세요. 위 문장을 그대로 쓰지 마세요.
PowerShell에서 랜덤 키를 생성하고 바로 환경변수에 넣을 수 있습니다.

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
.\mvnw.cmd spring-boot:run
```

## 실행 (macOS / Linux)

```bash
chmod +x mvnw
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_ACCESS_EXPIRATION=3600
./mvnw spring-boot:run
```

첫 실행에는 Maven과 의존성 다운로드를 위한 인터넷 연결이 필요합니다.
.env 파일은 자동 로드하지 않습니다. .env.example은 설정 항목을 알려주는 참고 파일입니다.
IntelliJ에서는 pom.xml을 프로젝트로 열고 실행 설정의 환경변수에 JWT_SECRET을 넣어도 됩니다.

## 테스트와 JAR 생성

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/movie-week7-1.0.0.jar
```

테스트는 별도 메모리 H2와 테스트 전용 키를 사용합니다. JWT_SECRET 없이도 테스트 가능합니다.
테스트에 있는 키는 실제 서버용으로 사용하지 마세요.

## API

| Method | 경로 | 인증 | 성공 |
|---|---|---|---|
| POST | /members | 불필요 | 201 |
| POST | /auth/login | 불필요 | 200 |
| GET | /members/me | Bearer 토큰 | 200 |
| PATCH | /members/me | Bearer 토큰 | 200 |
| GET | /members/{memberId}/ratings | 불필요 | 200 |
| POST | /ratings | Bearer 토큰 | 201 |
| PATCH | /ratings/{ratingId} | Bearer 토큰 + 소유권 | 200 |
| DELETE | /ratings/{ratingId} | Bearer 토큰 + 소유권 | 200, result:null |
| GET | /members/email/{email} | 불필요 | 200, available |
| GET | /members/nickname/{nickname} | 불필요 | 200, available |

회원가입: `{"email":"a@umc.com","nickname":"회원에이","password":"Password1!"}`
로그인: `{"email":"a@umc.com","password":"Password1!"}`
평점 등록: `{"movieId":100,"score":4.5,"comment":"재미있어요"}`
평점 수정: `{"score":5,"comment":"수정"}`
내 정보 수정: `{"nickname":"새닉네임"}`

평점 점수는 0~5, 소수점 최대 한 자리로 정했습니다. 이전 프로젝트의 별도 정책이 있다면 조정하세요.
평점 요청에 memberId를 넣으면 400으로 거절합니다. 요청자는 검증한 토큰의 subject에서만 얻습니다.
이메일·닉네임 중복 확인 응답은 프론트 워크북 호환을 위해 `{ "available": true }` 형태입니다.
나머지 API는 isSuccess/code/message/result 공통 응답을 사용합니다.

## Postman

1. 서버 실행 후 postman/UMC-Week7.postman_collection.json을 Import합니다.
2. Collection Runner에서 위에서 아래로 실행합니다.
3. runId가 자동 생성되고 회원 A·B의 토큰과 ratingId가 자동 저장됩니다.
4. 로그인 200, 무토큰 401, 타인 수정 403, 등록 201의 요청과 응답을 캡처합니다.

반복 실행 시 새 이메일과 닉네임을 사용하도록 구성했습니다.
실제 발급한 토큰은 공개 저장소에 올리지 마세요. 제공된 응답 예시의 토큰은 로컬 검증용 임시 키로 발급했습니다.

## CORS 및 브라우저 확인

`http://localhost:5173`을 허용하며 Authorization/Content-Type 헤더와 OPTIONS 사전 요청을 지원합니다.
CORS_ORIGINS 환경변수로 쉼표로 구분한 origin 목록을 바꿀 수 있습니다.
서버의 http://localhost:8080 에서 실습 화면을 사용할 수 있습니다.
브라우저 화면은 실제 API를 호출하며 Postman 앱 화면은 아닙니다.

## 선택 미션

@LoginMember와 HandlerMethodArgumentResolver 적용을 포함합니다.
만료 테스트는 JWT_ACCESS_EXPIRATION=60으로 재시작하고 새로 로그인한 뒤 60초 이후 같은 토큰으로 호출하세요.
워크북 예시의 clockSkewSeconds(60)는 의도적으로 생략해 60초 만료가 추가로 지연되지 않도록 했습니다.

## MySQL 사용 (선택)

DB를 먼저 생성하고 아래 환경변수로 실행하세요.

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://localhost:3306/umc_movie?serverTimezone=Asia/Seoul"
$env:DB_USERNAME = "본인계정"
$env:DB_PASSWORD = "본인비밀번호"
.\mvnw.cmd spring-boot:run
```

기본 H2만으로도 모든 필수 미션을 실행할 수 있습니다.
