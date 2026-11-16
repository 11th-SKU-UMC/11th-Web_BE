package com.umc.movie.service;
import com.umc.movie.dto.*;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.regex.Pattern;
@Service
public class MemberService{
 private static final Pattern EMAIL=Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");
 private final MemberRepository repo;
 private final PasswordEncoder encoder;
 public MemberService(MemberRepository repo,PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
 @Transactional(readOnly=true) public CheckResponse nickname(String nickname){
  if(nickname==null||nickname.codePointCount(0,nickname.length())<2||nickname.codePointCount(0,nickname.length())>12)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"닉네임은 2~12자여야 합니다.");
  return new CheckResponse(!repo.existsByNickname(nickname));
 }
 @Transactional(readOnly=true) public CheckResponse email(String email){
  if(email==null||email.length()>254||!EMAIL.matcher(email).matches())
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"이메일 형식이 올바르지 않습니다.");
  return new CheckResponse(!repo.existsByEmail(email));
 }
 @Transactional public MemberResponse signUp(SignUpRequest request){
  if(repo.existsByEmail(request.email()))throw new com.umc.movie.exception.BusinessException(HttpStatus.CONFLICT,"MEMBER4091","이미 사용 중인 이메일입니다.");
  if(repo.existsByNickname(request.nickname()))throw new com.umc.movie.exception.BusinessException(HttpStatus.CONFLICT,"MEMBER4092","이미 사용 중인 닉네임입니다.");
  Member saved=repo.saveAndFlush(new Member(request.email(),encoder.encode(request.password()),request.nickname()));
  return new MemberResponse(saved.getMemberId(),saved.getEmail(),saved.getNickname());
 }
}
