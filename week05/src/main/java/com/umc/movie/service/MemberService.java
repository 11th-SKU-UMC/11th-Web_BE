package com.umc.movie.service;
import com.umc.movie.dto.CheckResponse;
import com.umc.movie.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.regex.Pattern;
@Service
public class MemberService {
 private static final Pattern EMAIL=Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");
 private final MemberRepository repo;
 public MemberService(MemberRepository repo){this.repo=repo;}
 @Transactional(readOnly=true)
 public CheckResponse nickname(String nickname){
  if(nickname==null||nickname.codePointCount(0,nickname.length())<2||nickname.codePointCount(0,nickname.length())>12)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"닉네임은 2~12자여야 합니다.");
  return new CheckResponse(!repo.existsByNickname(nickname));
 }
 @Transactional(readOnly=true)
 public CheckResponse email(String email){
  if(email==null||email.length()>254||!EMAIL.matcher(email).matches())
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"이메일 형식이 올바르지 않습니다.");
  return new CheckResponse(!repo.existsByEmail(email));
 }
}
