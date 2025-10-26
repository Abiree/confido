package com.confido.api.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordRequest {
  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  private String email;
}
