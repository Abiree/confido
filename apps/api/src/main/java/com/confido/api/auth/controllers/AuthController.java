package com.confido.api.auth.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.confido.api.auth.dtos.*;
import com.confido.api.auth.services.IAuthService;
import com.confido.api.common.dtos.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final IAuthService authService;

  AuthController(IAuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody UserDTO userDTO) {
    UserDTO registerUser = authService.register(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                HttpStatus.CREATED.value(), "User signed up successfully", registerUser));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest loginUserDTO) {
    LoginResponse loginResponse = authService.login(loginUserDTO);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(HttpStatus.OK.value(), "User signed in successfully", loginResponse));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
      @RequestBody Map<String, String> request) {
    LoginResponse loginResponse = authService.refreshLogin(request.get("refreshToken"));
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(
                HttpStatus.OK.value(), "Refresh Token generated successfully", loginResponse));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(HttpStatus.OK.value(), "Current-User", authService.getCurrentUser()));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<String>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(
                HttpStatus.OK.value(), authService.forgotPassword(forgotPasswordRequest), null));
  }

  @PutMapping("/reset-password")
  public ResponseEntity<ApiResponse<String>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(
                HttpStatus.OK.value(), authService.resetPassword(resetPasswordRequest), null));
  }
}
