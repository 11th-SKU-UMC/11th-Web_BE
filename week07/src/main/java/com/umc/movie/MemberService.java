package com.umc.movie;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Service @Transactional
public class MemberService {
 private final MemberRepository members; private final BCryptPasswordEncoder bcrypt; private final JwtUtil jwt;
 private final String dummyHash;
 public MemberService(MemberRepository members,BCryptPasswordEncoder bcrypt,JwtUtil jwt) { this.members=members; this.bcrypt=bcrypt; this.jwt=jwt; dummyHash=bcrypt.encode("dummy-password-not-a-user"); }
 public Views.Signup signup(Requests.Signup d) {
  String email=d.email().trim(); String nickname=normalizeNickname(d.nickname());
  if(members.existsByEmail(email)) throw new ApiException(409,"MEMBER409_2","이미 사용 중인 이메일입니다.");
  if(members.existsByNickname(nickname)) throw new ApiException(409,"MEMBER409_1","이미 사용 중인 닉네임입니다.");
  try { return Views.Signup.from(members.saveAndFlush(new Member(email,nickname,bcrypt.encode(d.password())))); }
  catch(DataIntegrityViolationException e) { throw new ApiException(409,"MEMBER409_3","이메일 또는 닉네임이 이미 사용 중입니다."); }
 }
 @Transactional(readOnly=true) public Views.Login login(Requests.Login d) {
  Member member=members.findByEmail(d.email().trim()).orElse(null);
  boolean matches=bcrypt.matches(d.password(),member==null?dummyHash:member.passwordHash);
  if(member==null || !matches) throw new ApiException(401,"AUTH401_1","이메일 또는 비밀번호가 올바르지 않습니다.");
  return new Views.Login(member.nickname,jwt.createAccessToken(member.memberId,member.email));
 }
 @Transactional(readOnly=true) public Views.Me me(Long id) { return Views.Me.from(find(id)); }
 public Views.Me update(Long id,Requests.UpdateMember d) {
  Member member=find(id); String nickname=normalizeNickname(d.nickname());
  if(members.existsByNicknameAndMemberIdNot(nickname,id)) throw new ApiException(409,"MEMBER409_1","이미 사용 중인 닉네임입니다.");
  member.updateNickname(nickname);
  try { members.flush(); } catch(DataIntegrityViolationException e) { throw new ApiException(409,"MEMBER409_1","이미 사용 중인 닉네임입니다."); }
  return Views.Me.from(member);
 }
 private String normalizeNickname(String value) { String n=value.trim(); if(n.length()<2||n.length()>12) throw new ApiException(400,"COMMON400","닉네임은 2~12자여야 합니다."); return n; }
 Member find(Long id) { return members.findById(id).orElseThrow(()->new ApiException(404,"MEMBER404_1","해당 회원을 찾을 수 없습니다.")); }
}
