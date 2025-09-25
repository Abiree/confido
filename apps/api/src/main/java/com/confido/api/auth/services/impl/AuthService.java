package com.confido.api.auth.services.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.confido.api.auth.services.IAuthService;
import com.confido.api.common.mail.services.IEmailSender;

import jakarta.transaction.Transactional;

@Service
public class AuthService implements IAuthService {
  private final IUserRepository userRepository;
  private final IProfileRepository profileRepository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final IEmailSender emailSender;

  @Value("${security.jwt.reset.expiration-time}")
  long TokenRestExpirationTime;

  @Value("${link-to-reset-password}")
  String linkToResetPass;

  AuthService(
      IUserRepository userRepository,
      IProfileRepository profileRepository,
      AuthenticationManager authenticationManager,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      UserMapper userMapper,
      IEmailSender emailSender) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.authenticationManager = authenticationManager;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.userMapper = userMapper;
    this.emailSender = emailSender;
  }

  @Override
  @Transactional
  public UserDTO register(UserDTO registerUserDTO) {

    if (userRepository.existsByEmail(registerUserDTO.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }

    User user = userMapper.toEntity(registerUserDTO);
    user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));

    User userSaved = userRepository.save(user);

    return userMapper.toDTO(userSaved);
  }

  @Override
  public LoginResponse login(LoginRequest loginUserDTO) {
    // verify the credentials with Spring Security
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginUserDTO.getEmail(), loginUserDTO.getPassword()));
    User authenticatedUser =
        userRepository
            .findByEmail(loginUserDTO.getEmail())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "User not found with email: " + loginUserDTO.getEmail()));
    String token = jwtService.generateToken(authenticatedUser);
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setToken(token);
    loginResponse.setExpiresIn(jwtService.getExpirationTime());
    return loginResponse;
  }

  @Override
  public UserDTO getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userMapper.toDTO((User) auth.getPrincipal());
  }

  @Override
  public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    String email = forgotPasswordRequest.getEmail();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ForgotPasswordUserNotFoundException("No user found with email: " + email));

    if (!user.isEnabled()) {
      throw new AccountStatusException("User is disabled: " + email);
    }

    Profile profile =
        profileRepository
            .findById(user.getId())
            .orElseThrow(() -> new ForgotPasswordUserNotFoundException("No profile found"));

    String token = UUID.randomUUID().toString();
    user.setResetPasswordToken(token);
    user.setResetPasswordTokenExpiry(
        LocalDateTime.now().plusMinutes(TokenRestExpirationTime)); // expires in 15 min
    userRepository.save(user);
    // send token via email
    String link = linkToResetPass + user.getResetPasswordToken();
    emailSender.send(
        user.getEmail(),
        emailSender.buildResetPasswordEmail(profile.getLastName(), link),
        "Here is the link to reset your password.");
    return "If an account exists for this email, you will receive a reset link";
  }

  @Override
  public String resetPassword(ResetPasswordRequest resetPasswordRequest) {
    String token = resetPasswordRequest.getToken();
    String password = resetPasswordRequest.getPassword();
    User user =
        userRepository
            .findByResetPasswordToken(token)
            .orElseThrow(() -> new InvalidResetTokenException("Invalid reset token"));

    if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new ExpiredResetTokenException("Reset token has expired");
    }
    // ✅ Check if the account is disabled
    if (!user.isEnabled()) {
      throw new AccountStatusException("The account is disabled");
    }

    // Encode password
    user.setPassword(passwordEncoder.encode(password));
    user.setResetPasswordToken(null);
    user.setResetPasswordTokenExpiry(null);
    userRepository.save(user);

    return "Password has been reset successfully";
  }
}
