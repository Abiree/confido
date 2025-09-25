package com.confido.api.auth.exceptions;

public class ExpiredResetTokenException extends RuntimeException {
  public ExpiredResetTokenException(String message) {
    super(message);
  }
}
