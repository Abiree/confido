package com.confido.api.auth.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private LocalDateTime accessTokenExpiresIn;
  private LocalDateTime refreshTokenExpiresIn;
}
