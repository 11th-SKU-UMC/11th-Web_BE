package com.umc.movie;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="members",uniqueConstraints={@UniqueConstraint(name="uk_member_email",columnNames="email"),@UniqueConstraint(name="uk_member_nickname",columnNames="nickname")})
public class Member {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long memberId;
 @Column(nullable=false) String email;
 @Column(nullable=false,length=12) String nickname;
 @Column(nullable=false) String passwordHash;
 @Column(nullable=false,updatable=false) LocalDateTime createdAt;
 protected Member() {}
 Member(String email,String nickname,String hash) { this.email=email; this.nickname=nickname; this.passwordHash=hash; this.createdAt=LocalDateTime.now(); }
 public Long getMemberId() { return memberId; }
 void updateNickname(String nickname) { this.nickname=nickname; }
}
