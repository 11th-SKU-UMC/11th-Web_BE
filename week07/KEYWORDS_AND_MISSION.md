# 7주차 핵심 키워드

## 1. 인증과 인가
인증은 요청자가 누구인지 확인하는 과정이다. 로그인할 때 이메일과 비밀번호를 확인하고, 이후 요청에서도 JWT를 검증해 요청자를 확인한다.
인가는 확인된 사용자가 해당 행동을 할 권한이 있는지 판단하는 과정이다. 이번 프로젝트에서는 평점의 작성자와 토큰의 회원 ID를 비교해 수정·삭제 권한을 검사한다.
로그인한 회원 B의 정상 토큰으로 회원 A의 평점을 수정하면 인증은 통과하지만 인가에서 403으로 거절된다.

## 2. 해시와 salt, bcrypt
해시는 입력을 일방향으로 변환하는 방법이다. 비밀번호는 복호화해 원문을 확인하는 대신 입력값과 저장된 해시를 비교한다.
salt는 해시에 포함하는 무작위 값으로, 같은 비밀번호도 서로 다른 해시가 나오도록 한다.
bcrypt는 salt와 작업 비용(cost)을 반영하는 비밀번호 해시 알고리즘이다. 이 프로젝트는 BCryptPasswordEncoder(10)을 사용한다. 비용 값이 커질수록 검증 시간도 증가한다.
로그인 시 encode한 문자열을 단순 비교하면 salt 때문에 일치하지 않을 수 있다. matches(입력 비밀번호, 저장된 해시)를 사용한다. 가입 비밀번호는 bcrypt의 입력 길이를 고려해 ASCII 문자 8~72자로 제한했다.

## 3. JWT와 Claim
일반적인 서명된 JWT는 Header.Payload.Signature로 구성된다.
Header는 알고리즘 등의 정보를, Payload는 사용자의 식별자와 만료 시간 등의 클레임을 담는다. Signature는 토큰 내용의 무결성을 검증한다.
본 프로젝트는 sub에 회원 ID, email에 이메일, iat에 발급 시간, exp에 만료 시간을 담는다.
디코딩은 내용을 읽는 작업일 뿐 서명 검증이 아니다. 서버는 키로 서명을 검증하고 만료 시간과 subject를 확인한 뒤 회원 ID를 사용한다.
Payload는 일반적으로 암호화되지 않으므로 비밀번호를 넣지 않는다. 서명이 있어도 토큰 자체를 탈취하면 사용될 수 있으므로 관리가 필요하다.

## 4. Bearer 인증과 Authorization 헤더
클라이언트는 `Authorization: Bearer {accessToken}`으로 토큰을 보낸다.
Bearer는 토큰을 보유한 쪽이 해당 토큰을 사용할 수 있다는 의미의 인증 방식이다.
이번 구현은 JWT를 쿠키나 세션이 아니라 요청 헤더에서 읽는다. 헤더가 없으면 AUTH401_2, 형식이나 토큰이 올바르지 않으면 AUTH401_3을 반환한다.

## 5. HandlerInterceptor / Guard
Spring MVC의 HandlerInterceptor는 컨트롤러 실행 전후의 공통 처리를 담당한다. NestJS의 Guard는 요청을 진행할지 판단하는 구성 요소다.
이번 Spring Boot 구현은 워크북에 맞춰 preHandle에서 JWT를 검증하고 요청 속성에 회원 ID를 저장한다. 컨트롤러에서는 선택 미션의 @LoginMember와 ArgumentResolver로 ID를 받는다.
평점 소유권 검사는 DB 조회가 필요한 업무 규칙이므로 RatingService에서 수행한다.
실서비스에서는 Spring Security의 필터 체인 적용을 검토해야 한다. 이 프로젝트는 인터셉터 학습용으로 지정된 API 경로를 보호한다.

## 6. 401과 403
| 상태 | 의미 | 본 프로젝트 예시 |
|---|---|---|
| 401 Unauthorized | 인증이 없거나 유효하지 않음 | 무토큰, 위조 토큰, 만료 토큰, 잘못된 로그인 |
| 403 Forbidden | 인증된 사용자의 권한이 부족함 | 회원 B가 회원 A의 평점 수정·삭제 |
| 404 Not Found | 대상 자원이 없음 | 존재하지 않는 ratingId |

# 필수 미션 구현 기록

## 1. 기존 CRUD에 요청자 검증 추가
평점 작성자의 ID를 URL이나 본문에서 받지 않고 검증된 토큰의 subject에서 가져오도록 구성했다.
POST /ratings 요청 DTO에는 memberId가 없으며, 알 수 없는 JSON 필드를 거절하도록 설정했다.

## 2. 로그인
가입 시 bcrypt 해시를 저장한다. 로그인은 이메일 조회 후 matches로 검증한다.
이메일이 없거나 비밀번호가 틀린 경우 동일한 AUTH401_1 응답을 보낸다. 존재하지 않는 이메일도 더미 해시와 비교하도록 작성했다.

## 3. 공통 인증
AuthInterceptor → JwtUtil 서명·만료 검증 → 요청 속성에 회원 ID 저장 → LoginMemberResolver → 컨트롤러 순서로 요청자를 전달한다.

## 4. 내 정보 조회·수정
/members/me는 토큰에서 얻은 회원 ID로 조회한다. 닉네임 수정 시 자기 자신을 제외한 중복 여부를 검사한다. DB에도 UNIQUE 제약을 설정했다.

## 5. 평점 인가
RatingService.owned에서 대상 평점을 먼저 조회한다. 없으면 RATING404_1, 작성자가 다르면 RATING403_1을 반환한다. 수정과 삭제가 같은 소유권 검사 메서드를 사용한다.

```java
private Rating owned(Long id, Long ratingId) {
    Rating r = ratings.findById(ratingId)
        .orElseThrow(() -> new ApiException(404, "RATING404_1", "해당 평점을 찾을 수 없습니다."));
    if (!r.member.getMemberId().equals(id)) {
        throw new ApiException(403, "RATING403_1", "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.");
    }
    return r;
}
```

## 6. 중복 등록과 공통 응답
(member_id, movie_id)에 복합 UNIQUE 제약을 두고 서비스에서 먼저 중복을 확인한다. 동시에 중복 요청이 들어와도 saveAndFlush에서 발생한 DB 제약 위반을 409로 변환한다.
삭제 성공은 워크북 규칙에 맞춰 200과 result:null을 보낸다.

## 7. 브라우저 연동
localhost:5173의 Origin을 허용하고 Authorization 및 Content-Type 헤더를 허용했다. OPTIONS 사전 요청은 인증으로 차단하지 않는다.

## 8. 검증 자료
- postman/UMC-Week7.postman_collection.json: 워크북 9단계 요청, 응답 코드 검사, 토큰/평점 ID 자동 저장
- src/test/.../AuthIntegrationTest.java: 인증·인가 및 입력값·CORS 통합 검증
- evidence/: 실제 실행 후 생성한 응답 JSON과 HTML 보고서 (화면 캡처 아님)
- VERIFICATION.md: 검증 결과와 확인 범위

# 선택 미션: @LoginMember

적용 전:
```java
public ApiResponse<?> me(@RequestAttribute("authenticatedMemberId") Long memberId)
```
적용 후:
```java
public ApiResponse<?> me(@LoginMember Long id)
```

LoginMemberResolver가 어노테이션을 확인하고 요청 속성을 해석한다. 컨트롤러에서 문자열 키를 반복하지 않아도 된다.
만료 응답은 AUTH401_4로 구분한다. 자동 테스트에서는 이미 만료된 토큰을 생성해 검사하며, 실제 60초 대기 실습과는 구분한다.

# 트러블슈팅

## 1. 로그인과 JWT 검증의 실패 코드 구분
- 이슈: 인증 실패를 모두 같은 응답으로 처리하면 로그인 정보 오류, 무토큰, 위조, 만료를 구분하기 어렵다.
- 문제: 인증이 실패한 단계와 이유가 다른데 동일한 분기로 처리하기 때문이다.
- 해결: 로그인 AUTH401_1, 무토큰 AUTH401_2, 위조·형식 오류 AUTH401_3, 만료 AUTH401_4로 분리했다. ExpiredJwtException을 먼저 처리한다.
- 배운 점: 같은 HTTP 상태라도 클라이언트가 상황에 맞게 안내할 수 있도록 도메인 코드를 구분한다.

## 2. 만료 시간이 설정값보다 늦게 적용되는 문제 예방
- 이슈: 워크북의 clockSkewSeconds(60)를 그대로 적용하면 60초 토큰의 만료 응답이 추가로 지연될 수 있다.
- 문제: 시간 오차 허용 범위가 만료 판정에 적용되기 때문이다.
- 해결: 이번 프로젝트에서는 시간 오차 허용 설정을 생략하고 exp 기준으로 검사한다.
- 배운 점: 토큰 유효시간과 시간 오차 허용 범위는 서로 다른 설정이다.

## 3. 중복 조회만으로 동시 가입·평점 등록을 막을 수 없는 문제 예방
- 이슈: 두 요청이 동시에 사전 중복 검사를 통과할 수 있다.
- 문제: exists 조회와 저장 사이에 다른 트랜잭션이 저장할 수 있기 때문이다.
- 해결: DB UNIQUE 제약을 함께 사용하고 saveAndFlush로 제약 위반을 서비스 내에서 확인해 409로 변환한다.
- 배운 점: 사전 조회는 친절한 안내에 유용하지만 최종 무결성은 DB 제약도 필요하다.

위 세 항목은 구현 과정의 설계·예방 기록이며, 발생하지 않은 오류 메시지를 실제 오류처럼 작성하지 않았다.

# 학습 후기 초안

이번 주차에는 로그인 기능을 만드는 것에서 더 나아가 요청을 보낸 사용자가 실제로 누구인지 검증하고, 그 사용자가 데이터를 변경할 권한이 있는지 확인하는 흐름을 학습했다. 특히 정상 토큰을 가지고 있어도 다른 사람의 평점을 수정하면 403이 반환된다는 점을 통해 인증과 인가의 차이를 이해할 수 있었다.
비밀번호는 다시 해싱해 문자열을 비교하는 것이 아니라 bcrypt의 matches로 검증해야 한다는 점과 JWT의 디코딩만으로 토큰을 신뢰하면 안 된다는 점이 기억에 남았다. 공통 인증 처리는 인터셉터로 모으고 평점 소유권은 서비스에서 확인하면서 각 계층의 책임도 구분할 수 있었다.
앞으로는 Spring Security를 적용했을 때 인증 흐름이 어떻게 바뀌는지와 Access Token 만료 후 인증을 이어가는 방법을 더 학습하고 싶다.

# 공식 참고 자료
- Spring MVC Interceptors: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/interceptors.html
- Spring Security Password Storage: https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
- JJWT: https://github.com/jwtk/jjwt
- JWT 표준 RFC 7519: https://www.rfc-editor.org/rfc/rfc7519
- Bearer 인증 RFC 6750: https://www.rfc-editor.org/rfc/rfc6750
