package com.confido.api.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

  @NotBlank(message = "Token is required")
  private String token;

  @NotBlank(message = "Password is required")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
      message =
          "Password must be at least 8 characters and include uppercase, lowercase, number, and special character")
  private String password;
}
