package com.confido.api.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  private String email;

  private String password;
}
