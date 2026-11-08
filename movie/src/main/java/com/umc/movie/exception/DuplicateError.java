package com.umc.movie.exception;

public enum DuplicateError {
    EMAIL("MEMBER4091", "이미 사용 중인 이메일입니다."),
    NICKNAME("MEMBER4092", "이미 사용 중인 닉네임입니다."),
    RATING("RATING4091", "이미 이 영화에 평점을 남기셨습니다.");

    private final String code;
    private final String message;

    DuplicateError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
