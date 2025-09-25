package com.confido.api.common.mail.services;

public interface IEmailSender {
  void send(String to, String emailContent, String subject);

  String buildResetPasswordEmail(String name, String link);
}
