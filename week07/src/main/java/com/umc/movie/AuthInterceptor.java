package com.umc.movie;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class AuthInterceptor implements HandlerInterceptor {
 static final String MEMBER_ID="authenticatedMemberId";
 private final JwtUtil jwt;
 public AuthInterceptor(JwtUtil jwt) { this.jwt=jwt; }
 @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
  if("OPTIONS".equals(req.getMethod())) return true;
  String header=req.getHeader("Authorization");
  if(header==null || header.isBlank()) throw new ApiException(401,"AUTH401_2","로그인이 필요한 요청입니다.");
  if(!header.regionMatches(true,0,"Bearer ",0,7) || header.substring(7).isBlank()) throw new ApiException(401,"AUTH401_3","유효하지 않은 토큰입니다.");
  req.setAttribute(MEMBER_ID,jwt.getMemberId(header.substring(7).trim()));
  return true;
 }
}
