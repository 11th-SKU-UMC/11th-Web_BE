package com.umc.movie;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.*;
@Component
public class LoginMemberResolver implements HandlerMethodArgumentResolver {
 public boolean supportsParameter(MethodParameter p) { return p.hasParameterAnnotation(LoginMember.class)&&p.getParameterType()==Long.class; }
 public Object resolveArgument(MethodParameter p,ModelAndViewContainer m,NativeWebRequest r,WebDataBinderFactory b) {
  Object id=r.getAttribute(AuthInterceptor.MEMBER_ID,RequestAttributes.SCOPE_REQUEST);
  if(id==null) throw new ApiException(401,"AUTH401_2","로그인이 필요한 요청입니다.");
  return id;
 }
}
