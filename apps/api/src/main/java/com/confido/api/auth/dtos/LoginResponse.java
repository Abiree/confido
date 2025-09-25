package com.confido.api.auth.dtos;

import lombok.Data;

@Data
public class LoginResponse {
  private String token;
  private Long ExpiresIn;
}
