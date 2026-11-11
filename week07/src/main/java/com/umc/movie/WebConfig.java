package com.umc.movie;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
@Configuration
public class WebConfig implements WebMvcConfigurer {
 private final AuthInterceptor auth; private final LoginMemberResolver resolver; private final String[] origins;
 public WebConfig(AuthInterceptor auth,LoginMemberResolver resolver,@Value("${app.cors-origins}") String[] origins) { this.auth=auth; this.resolver=resolver; this.origins=origins; }
 @Bean BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(10); }
 @Override public void addInterceptors(InterceptorRegistry r) { r.addInterceptor(auth).addPathPatterns("/members/me","/members/me/**","/ratings","/ratings/**"); }
 @Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) { list.add(resolver); }
 @Override public void addCorsMappings(CorsRegistry r) { r.addMapping("/**").allowedOrigins(origins).allowedMethods("GET","POST","PATCH","DELETE","OPTIONS").allowedHeaders("Authorization","Content-Type"); }
}
