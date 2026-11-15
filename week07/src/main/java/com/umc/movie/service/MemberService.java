package com.umc.movie.service;
import com.umc.movie.dto.*;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class MemberService {
 private final MemberRepository memberRepository; private final PasswordEncoder passwordEncoder;
 @Transactional(readOnly=true) public NicknameCheckResponse checkNickname(String nickname) { return new NicknameCheckResponse(!memberRepository.existsByNickname(nickname)); }
 @Transactional(readOnly=true) public EmailCheckResponse checkEmail(String email) { return new EmailCheckResponse(!memberRepository.existsByEmail(email)); }
 @Transactional public MemberResponse signUp(SignUpRequest request) {
  if(memberRepository.existsByEmail(request.email())) throw new ApiException(HttpStatus.CONFLICT,"MEMBER4091","이미 사용 중인 이메일입니다.");
  if(memberRepository.existsByNickname(request.nickname())) throw new ApiException(HttpStatus.CONFLICT,"MEMBER4092","이미 사용 중인 닉네임입니다.");
  Member saved=memberRepository.save(new Member(request.email(),passwordEncoder.encode(request.password()),request.nickname()));
  return new MemberResponse(saved.getMemberId(),saved.getEmail(),saved.getNickname());
 }
 @Transactional(readOnly=true)
 public MemberInfoResponse getMyInfo(Long memberId) {
  Member member=memberRepository.findById(memberId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"MEMBER404_1","해당 회원을 찾을 수 없습니다."));
  return MemberInfoResponse.from(member);
 }
 @Transactional
 public MemberInfoResponse updateMyInfo(Long memberId, MemberUpdateRequest request) {
  Member member=memberRepository.findById(memberId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"MEMBER404_1","해당 회원을 찾을 수 없습니다."));
  if(memberRepository.existsByNicknameAndMemberIdNot(request.nickname(),memberId)) throw new ApiException(HttpStatus.CONFLICT,"MEMBER409_1","이미 사용 중인 닉네임입니다.");
  member.updateNickname(request.nickname());
  return MemberInfoResponse.from(memberRepository.save(member));
 }
}
