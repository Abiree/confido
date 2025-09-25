package com.confido.api.auth.exceptions;

public class ForgotPasswordUserNotFoundException extends RuntimeException {
  public ForgotPasswordUserNotFoundException(String message) {
    super(message); // store the detailed message for logs
  }
}
