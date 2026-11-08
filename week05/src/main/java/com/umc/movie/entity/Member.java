package com.umc.movie.entity;
import jakarta.persistence.*;
@Entity @Table(name="member")
public class Member extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="member_id") private Long memberId;
 @Column(nullable=false,unique=true,length=254) private String email;
 @Column(nullable=false,length=60) private String password;
 @Column(nullable=false,unique=true,length=50) private String nickname;
 protected Member(){}
 public Member(String email,String password,String nickname){this.email=email;this.password=password;this.nickname=nickname;}
 public Long getMemberId(){return memberId;}
 public String getEmail(){return email;}
 public String getNickname(){return nickname;}
}
