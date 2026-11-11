package com.umc.movie;

import com.fasterxml.jackson.databind.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","jwt.secret=unit-test-secret-32-bytes-minimum-value","jwt.access-expiration=3600"})
@AutoConfigureMockMvc
class AuthIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired MemberRepository members;
 @Autowired JwtUtil jwt;
 ResultActions call(MockHttpServletRequestBuilder req,String body,String token) throws Exception {
  if(body!=null) req.contentType(MediaType.APPLICATION_JSON).content(body);
  if(token!=null) req.header("Authorization","Bearer "+token);
  return mvc.perform(req);
 }
 String login(String email,String password) throws Exception {
  String content=call(post("/auth/login"),"{\"email\":\""+email+"\",\"password\":\""+password+"\"}",null).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  return json.readTree(content).path("result").path("accessToken").asText();
 }
 @Test void workbookNineStepsAndSecurityEdges() throws Exception {
  // A/B 가입, 해시 저장 및 로그인
  call(post("/members"),"{\"email\":\"a@umc.com\",\"nickname\":\"회원에이\",\"password\":\"Password1!\"}",null).andExpect(status().isCreated());
  call(post("/members"),"{\"email\":\"b@umc.com\",\"nickname\":\"회원비\",\"password\":\"Password1!\"}",null).andExpect(status().isCreated());
  assertNotEquals("Password1!",members.findByEmail("a@umc.com").orElseThrow().passwordHash);
  String a=login("a@umc.com","Password1!"), b=login("b@umc.com","Password1!");
  String rating="{\"movieId\":100,\"score\":4.5,\"comment\":\"재미있어요\"}";
  String response=call(post("/ratings"),rating,a).andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201")).andReturn().getResponse().getContentAsString();
  long id=json.readTree(response).path("result").path("ratingId").asLong();
  call(post("/ratings"),rating,a).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RATING409_1"));
  call(patch("/ratings/"+id),"{\"score\":5,\"comment\":\"수정\"}",a).andExpect(status().isOk());
  call(patch("/ratings/"+id),"{\"score\":1}",b).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("RATING403_1"));
  call(delete("/ratings/"+id),null,b).andExpect(status().isForbidden());
  call(get("/members/me"),null,null).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
  call(post("/ratings"),rating,null).andExpect(status().isUnauthorized());
  call(delete("/ratings/99999"),null,a).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RATING404_1"));
  call(get("/members/me"),null,a).andExpect(status().isOk()).andExpect(jsonPath("$.result.email").value("a@umc.com"));
  call(patch("/members/me"),"{\"nickname\":\"변경회원\"}",a).andExpect(status().isOk()).andExpect(jsonPath("$.result.nickname").value("변경회원"));
  call(patch("/members/me"),"{\"nickname\":\"회원비\"}",a).andExpect(status().isConflict());
  call(get("/members/1/ratings"),null,null).andExpect(status().isOk());
  // 본문 memberId 위조는 알 수 없는 필드로 거절
  call(post("/ratings"),"{\"memberId\":2,\"movieId\":101,\"score\":4}",a).andExpect(status().isBadRequest());
  call(post("/ratings"),"{\"movieId\":101,\"score\":6}",a).andExpect(status().isBadRequest());
  call(post("/ratings"),"{broken",a).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON400"));
  // 로그인 실패는 이메일 존재 여부를 노출하지 않음
  String fail1=call(post("/auth/login"),"{\"email\":\"a@umc.com\",\"password\":\"Wrong123!\"}",null).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
  String fail2=call(post("/auth/login"),"{\"email\":\"nobody@umc.com\",\"password\":\"Wrong123!\"}",null).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
  assertEquals(fail1,fail2);
  String[] parts=a.split("\\.");
  parts[1]=(parts[1].startsWith("A")?"B":"A")+parts[1].substring(1);
  call(get("/members/me"),null,String.join(".",parts)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_3"));
  String expired=Jwts.builder().subject("1").expiration(Date.from(Instant.now().minusSeconds(5))).signWith(Keys.hmacShaKeyFor("unit-test-secret-32-bytes-minimum-value".getBytes(StandardCharsets.UTF_8))).compact();
  call(get("/members/me"),null,expired).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_4"));
  mvc.perform(options("/ratings").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,content-type")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
  call(delete("/ratings/"+id),null,a).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200")).andExpect(content().string(org.hamcrest.Matchers.containsString("\"result\":null")));
  call(delete("/ratings/"+id),null,a).andExpect(status().isNotFound());
 }
}
