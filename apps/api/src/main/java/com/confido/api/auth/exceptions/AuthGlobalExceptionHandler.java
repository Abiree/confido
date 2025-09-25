package com.confido.api.auth.exceptions;

import java.security.SignatureException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class AuthGlobalExceptionHandler {
  /**
   * Utility method to enrich all ProblemDetail responses with common properties. - timestamp: when
   * the error happened - path: which endpoint was called - message: user-friendly message
   */
  private ProblemDetail enrich(
      ProblemDetail problemDetail, HttpServletRequest request, String message) {
    problemDetail.setProperty("timestamp", LocalDateTime.now().toString());
    problemDetail.setProperty("message", message);
    return problemDetail;
  }

  /** Handle bad credentials (wrong username/password). Maps to HTTP 401 Unauthorized. */
  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Authentication Failed");
    return enrich(problem, request, "The username or password is incorrect");
  }

  /** Handle expired JWT tokens. Maps to HTTP 401 Unauthorized. */
  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("JWT Expired");
    return enrich(problem, request, "The JWT token has expired");
  }

  /** Handle invalid JWT signatures (tampered tokens). Maps to HTTP 401 Unauthorized. */
  @ExceptionHandler(SignatureException.class)
  public ProblemDetail handleInvalidJwtSignature(
      SignatureException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Invalid JWT Signature");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "The JWT signature is invalid");
  }

  /** Handle user not found during authentication. Maps to HTTP 401 Unauthorized. */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ProblemDetail handleUserNotFound(
      UsernameNotFoundException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

    return enrich(problem, request, "The username or password is incorrect");
  }

  /**
   * Handle cases where the user is not found during the forgot-password flow. Maps to HTTP 401
   * Unauthorized.
   */
  @ExceptionHandler(ForgotPasswordUserNotFoundException.class)
  public ProblemDetail handleForgotPasswordUserNotFound(
      ForgotPasswordUserNotFoundException ex, HttpServletRequest request) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.OK);
    // Only expose a generic message to the client
    return enrich(
        problem, request, "If an account exists for this email, you will receive a reset link.");
  }

  /**
   * Handle account-related errors (e.g., disabled, locked accounts). Maps to HTTP 403 Forbidden.
   */
  @ExceptionHandler(AccountStatusException.class)
  public ProblemDetail handleAccountStatus(AccountStatusException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Account Locked");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "The account is locked");
  }

  /** Handle access denied errors when user lacks permissions. Maps to HTTP 403 Forbidden. */
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Access Denied");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "You are not authorized to access this resource");
  }

  /** Handle cases where the password reset token is invalid. Maps to HTTP 400 Bad Request. */
  @ExceptionHandler(InvalidResetTokenException.class)
  public ProblemDetail handleInvalidResetToken(
      InvalidResetTokenException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    return enrich(problem, request, "Invalid reset token");
  }

  /** Handle cases where the password reset token has expired. Maps to HTTP 400 Bad Request. */
  @ExceptionHandler(ExpiredResetTokenException.class)
  public ProblemDetail handleExpiredResetToken(
      ExpiredResetTokenException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    return enrich(problem, request, "Reset token has expired");
  }
}
