package com.confido.api.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.confido.api.auth.dao.IProfileRepository;
import com.confido.api.auth.dao.IUserRepository;
import com.confido.api.auth.dtos.*;
import com.confido.api.auth.exceptions.AccountStatusException;
import com.confido.api.auth.exceptions.ExpiredResetTokenException;
import com.confido.api.auth.exceptions.ForgotPasswordUserNotFoundException;
import com.confido.api.auth.exceptions.InvalidResetTokenException;
import com.confido.api.auth.mapper.UserMapper;
import com.confido.api.auth.models.Profile;
import com.confido.api.auth.models.User;
import com.confido.api.auth.services.impl.AuthService;
import com.confido.api.auth.services.impl.JwtService;
import com.confido.api.common.mail.services.IEmailSender;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock private IUserRepository userRepository;

  @Mock private IProfileRepository profileRepository;
  @Mock private IEmailSender emailSender;

  @Mock private UserMapper userMapper;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtService jwtService;

  @Mock private Authentication authentication;

  @Mock private SecurityContext securityContext;

  @Spy @InjectMocks private AuthService authService;

  private User user;
  private User userDisabled;
  private Profile profile;
  private UserDTO registerUserDTO;
  private LoginRequest loginRequest;
  private LoginResponse loginResponse;
  private ForgotPasswordRequest forgotPasswordRequestForUser;
  private ForgotPasswordRequest forgotPasswordRequestForInvalidEmail;
  private ForgotPasswordRequest forgotPasswordRequestForDisabledUser;
  private ResetPasswordRequest resetPasswordRequestForInvalidResetToken;
  private ResetPasswordRequest resetPasswordRequest;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .email("user@gmail.com")
            .password("encodedPass")
            .refreshToken("validRefreshToken")
            .refreshTokenExpiry(LocalDateTime.now())
            .enabled(true)
            .resetPasswordToken("validRefreshToken")
            .build();
    profile = Profile.builder().firstName("user").lastName("profile").build();
    userDisabled =
        User.builder()
            .email("userDisabled@gmail.com")
            .password("encodedPass")
            .refreshToken("validRefreshToken")
            .refreshTokenExpiry(LocalDateTime.now())
            .enabled(false)
            .resetPasswordToken("validRefreshToken")
            .build();
    registerUserDTO = UserDTO.builder().email("user@gmail.com").password("password123@").build();
    loginResponse =
        LoginResponse.builder()
            .accessToken("accessToken")
            .refreshToken("validRefreshToken")
            .build();
    loginRequest = LoginRequest.builder().email("user@gmail.com").password("password123@").build();
    forgotPasswordRequestForInvalidEmail =
        ForgotPasswordRequest.builder().email("invalidEmail@gmail.com").build();
    forgotPasswordRequestForDisabledUser =
        ForgotPasswordRequest.builder().email("userDisabled@gmail.com").build();
    forgotPasswordRequestForUser = ForgotPasswordRequest.builder().email("user@gmail.com").build();
    resetPasswordRequestForInvalidResetToken =
        ResetPasswordRequest.builder().token("invalidResetToken").build();
    resetPasswordRequest =
        ResetPasswordRequest.builder().token("validRefreshToken").password("encodedPass").build();
  }

  @Test
  void authService_register_ShouldThrowException_WhenEmailAlreadyExists() {
    when(userRepository.existsByEmail(registerUserDTO.getEmail())).thenReturn(true);
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> authService.register(this.registerUserDTO));
    Assertions.assertEquals(exception.getMessage(), "Email already in use");
  }

  @Test
  void authService_register_ShouldSaveUserAndReturnDTO_WhenEmailDoesNotExist() {
    when(userRepository.existsByEmail(registerUserDTO.getEmail())).thenReturn(false);
    when(userMapper.toEntity(registerUserDTO)).thenReturn(user);
    when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn(user.getPassword());
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDTO(user)).thenReturn(registerUserDTO);

    UserDTO result = authService.register(registerUserDTO);
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
    verify(userRepository).existsByEmail(user.getEmail());
    verify(userMapper).toEntity(registerUserDTO);
    verify(passwordEncoder).encode(registerUserDTO.getPassword());
    verify(userRepository).save(user);
    verify(userMapper).toDTO(user);
  }

  @Test
  void authService_login_ShouldAuthenticateAndReturnLoginResponse() {
    when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

    doReturn(this.loginResponse)
        .when(authService)
        .updateUserRefreshTokenAndbuildLoginResponse(user);

    LoginResponse response = authService.login(loginRequest);

    assertNotNull(response);
    assertEquals("accessToken", response.getAccessToken());
    assertEquals("validRefreshToken", response.getRefreshToken());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByEmail(user.getEmail());
    verify(authService).updateUserRefreshTokenAndbuildLoginResponse(user);
  }

  @Test
  void authService_login_ShouldThrow_WhenUserNotFound() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
    UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));
    assertEquals("User not found with email: " + user.getEmail(), ex.getMessage());
  }

  @Test
  void authService_refreshToken_ShouldReturnLoginResponse_WhenTokenIsValid() {
    String refreshToken = "validRefreshToken";
    when(jwtService.extractEmail(refreshToken)).thenReturn(user.getEmail());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(jwtService.isRefreshTokenValid(
            user.getRefreshToken(), refreshToken, user.getRefreshTokenExpiry()))
        .thenReturn(true);

    doReturn(this.loginResponse)
        .when(authService)
        .updateUserRefreshTokenAndbuildLoginResponse(user);

    LoginResponse response = authService.refreshLogin(refreshToken);

    assertNotNull(response);
    assertEquals("accessToken", response.getAccessToken());
    assertEquals(refreshToken, response.getRefreshToken());

    verify(jwtService).extractEmail(refreshToken);
    verify(userRepository).findByEmail(user.getEmail());
    verify(jwtService)
        .isRefreshTokenValid(user.getRefreshToken(), refreshToken, user.getRefreshTokenExpiry());
    verify(authService).updateUserRefreshTokenAndbuildLoginResponse(user);
  }

  @Test
  void authService_refreshLogin_ShouldThrow_WhenUserNotFound() {
    String refreshToken = "token";

    when(jwtService.extractEmail(refreshToken)).thenReturn("notfound@gmail.com");
    when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

    InvalidRefreshTokenException ex =
        assertThrows(
            InvalidRefreshTokenException.class, () -> authService.refreshLogin(refreshToken));
    assertEquals("User not found", ex.getMessage());
  }

  @Test
  void authService_refreshLogin_ShouldThrow_WhenTokenInvalid() {
    String refreshToken = "invalidToken";

    when(jwtService.extractEmail(refreshToken)).thenReturn(user.getEmail());
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(jwtService.isRefreshTokenValid(
            user.getRefreshToken(), refreshToken, user.getRefreshTokenExpiry()))
        .thenReturn(false);

    InvalidRefreshTokenException ex =
        assertThrows(
            InvalidRefreshTokenException.class, () -> authService.refreshLogin(refreshToken));
    assertEquals("Invalid refresh token", ex.getMessage());
  }

  @Test
  void authService_getCurrentUser_ShouldReturnCurrentUser() {
    // Mock Authentication
    when(authentication.getPrincipal()).thenReturn(user);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    // Set mocked context
    SecurityContextHolder.setContext(securityContext);
    // Mock mapper
    when(userMapper.toDTO(user)).thenReturn(registerUserDTO);
    // Act
    UserDTO result = authService.getCurrentUser();
    // Assert
    assertEquals("user@gmail.com", result.getEmail());
    // Clean up
    SecurityContextHolder.clearContext();
  }

  @Test
  void authService_forgotPassword_ShouldThrow_WhenEmailInvalid() {
    ForgotPasswordUserNotFoundException ex =
        assertThrows(
            ForgotPasswordUserNotFoundException.class,
            () -> authService.forgotPassword(forgotPasswordRequestForInvalidEmail));
    assertEquals(
        "No user found with email: " + forgotPasswordRequestForInvalidEmail.getEmail(),
        ex.getMessage());
  }

  @Test
  void authService_forgotPassword_ShouldThrow_WhenUserIsDisable() {
    when(userRepository.findByEmail(userDisabled.getEmail())).thenReturn(Optional.of(userDisabled));
    AccountStatusException ex =
        assertThrows(
            AccountStatusException.class,
            () -> authService.forgotPassword(forgotPasswordRequestForDisabledUser));
    assertEquals(
        "User is disabled: " + forgotPasswordRequestForDisabledUser.getEmail(), ex.getMessage());
  }

  @Test
  void authService_forgotPassword_ShouldThrow_WhenProfileNotFound() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(profileRepository.findById(user.getId())).thenReturn(Optional.empty());
    ForgotPasswordUserNotFoundException ex =
        assertThrows(
            ForgotPasswordUserNotFoundException.class,
            () -> authService.forgotPassword(forgotPasswordRequestForUser));
    assertEquals("No profile found", ex.getMessage());
  }

  @Test
  void authService_forgetPassword_ShouldRetrunMessage() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(profileRepository.findById(user.getId())).thenReturn(Optional.of(profile));
    assertEquals(
        authService.forgotPassword(forgotPasswordRequestForUser),
        "If an account exists for this email, you will receive a reset link");
  }

  @Test
  void authService_resetPassword_ShouldThrow_WhenInvalidResetToken() {
    InvalidResetTokenException ex =
        assertThrows(
            InvalidResetTokenException.class,
            () -> authService.resetPassword(resetPasswordRequestForInvalidResetToken));
    assertEquals("Invalid reset token", ex.getMessage());
  }

  @Test
  void authService_resetPassword_ShouldThrow_WhenTokenExpired() {
    when(userRepository.findByResetPasswordToken(user.getResetPasswordToken()))
        .thenReturn(Optional.of(user));
    when(jwtService.isTokenExpired(user.getResetPasswordTokenExpiry())).thenReturn(true);
    ExpiredResetTokenException ex =
        assertThrows(
            ExpiredResetTokenException.class,
            () -> authService.resetPassword(resetPasswordRequest));
    assertEquals("Reset token has expired", ex.getMessage());
  }

  @Test
  void authService_resetPassword_ShouldThrow_WhenUserIsDisable() {
    when(userRepository.findByResetPasswordToken(userDisabled.getResetPasswordToken()))
        .thenReturn(Optional.of(userDisabled));
    when(jwtService.isTokenExpired(userDisabled.getResetPasswordTokenExpiry())).thenReturn(false);
    AccountStatusException ex =
        assertThrows(
            AccountStatusException.class, () -> authService.resetPassword(resetPasswordRequest));
    assertEquals("The account is disabled", ex.getMessage());
  }

  @Test
  void authService_resetPassword_ShouldRetrunMessage() {
    when(userRepository.findByResetPasswordToken(user.getResetPasswordToken()))
        .thenReturn(Optional.of(user));
    when(jwtService.isTokenExpired(user.getResetPasswordTokenExpiry())).thenReturn(false);
    assertEquals(
        authService.resetPassword(resetPasswordRequest), "Password has been reset successfully");
  }
}
