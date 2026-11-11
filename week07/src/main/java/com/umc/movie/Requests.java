package com.umc.movie;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public final class Requests {
 private Requests() {}
 public record Signup(@NotBlank @Email String email,@NotBlank @Size(min=2,max=12) String nickname,
  @NotBlank @Size(min=8,max=72) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]{8,72}$",message="비밀번호는 영문 대소문자, 숫자, 특수문자를 포함한 8~72자여야 합니다.") String password) {}
 public record Login(@NotBlank @Email String email,@NotBlank @Size(max=72) String password) {}
 public record UpdateMember(@NotBlank @Size(min=2,max=12) String nickname) {}
 public record CreateRating(@NotNull @Positive Long movieId,@NotNull @DecimalMin("0.0") @DecimalMax("5.0") @Digits(integer=1,fraction=1) BigDecimal score,@Size(max=1000) String comment) {}
 public record UpdateRating(@NotNull @DecimalMin("0.0") @DecimalMax("5.0") @Digits(integer=1,fraction=1) BigDecimal score,@Size(max=1000) String comment) {}
}
