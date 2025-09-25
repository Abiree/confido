package com.confido.api.auth.exceptions;

public class AccountStatusException extends RuntimeException {
  public AccountStatusException(String message) {
    super(message);
  }
}
