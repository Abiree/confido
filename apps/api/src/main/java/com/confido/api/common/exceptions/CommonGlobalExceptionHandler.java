package com.confido.api.common.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class CommonGlobalExceptionHandler {
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

  /** Handle user not found during authentication. Maps to HTTP 401 Unauthorized. */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ProblemDetail handleUserNotFound(
      UsernameNotFoundException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

    return enrich(problem, request, "The username or password is incorrect");
  }

  /** Handle access denied errors when user lacks permissions. Maps to HTTP 403 Forbidden. */
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Access Denied");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "You are not authorized to access this resource");
  }

  /** Handle validation errors (e.g., @Valid annotations). Maps to HTTP 400 Bad Request. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldError() != null
            ? ex.getBindingResult().getFieldError().getDefaultMessage()
            : "Validation error";

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Validation Error");
    problem.setDetail("One or more fields are invalid.");
    return enrich(problem, request, message);
  }

  /** Handle bad requests (illegal arguments). Maps to HTTP 400 Bad Request. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Bad Request");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "Bad request");
  }

  /** Catch-all handler for any unhandled exceptions. Maps to HTTP 500 Internal Server Error. */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Internal Server Error");
    problem.setDetail(ex.getMessage());
    return enrich(problem, request, "Unknown internal server error");
  }
}
