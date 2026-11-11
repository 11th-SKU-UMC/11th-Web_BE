package com.umc.movie.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "MEMBER4091", "이미 사용 중인 이메일입니다."),
    MEMBER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "MEMBER4092", "이미 사용 중인 닉네임입니다."),
    RATING_DUPLICATE(HttpStatus.CONFLICT, "RATING4091", "이미 이 영화에 평점을 남기셨습니다."),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "COMMON4091", "이미 존재하는 데이터입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}