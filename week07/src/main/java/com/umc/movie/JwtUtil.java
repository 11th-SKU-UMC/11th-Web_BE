package com.umc.movie;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class JwtUtil {
 private final SecretKey key; private final long expiration;
 public JwtUtil(@Value("${jwt.secret}") String secret,@Value("${jwt.access-expiration}") long expiration) {
  key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  if(expiration<=0) throw new IllegalArgumentException("JWT expiration must be positive");
  this.expiration=expiration;
 }
 public String createAccessToken(Long memberId,String email) {
  Instant now=Instant.now();
  return Jwts.builder().subject(memberId.toString()).claim("email",email).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expiration))).signWith(key,Jwts.SIG.HS256).compact();
 }
 public Long getMemberId(String token) {
  try {
   Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
   Long id=Long.valueOf(claims.getSubject());
   if(id<=0 || claims.getExpiration()==null) throw new IllegalArgumentException();
   return id;
  } catch(ExpiredJwtException e) { throw new ApiException(401,"AUTH401_4","토큰이 만료되었습니다. 다시 로그인해주세요."); }
  catch(JwtException|IllegalArgumentException e) { throw new ApiException(401,"AUTH401_3","유효하지 않은 토큰입니다."); }
 }
}
