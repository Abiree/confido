package com.confido.api.common.mail.services.Imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.confido.api.common.mail.services.IEmailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService implements IEmailSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender mailSender;

  @Override
  @Async
  public void send(String to, String emailContent, String subject) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
      helper.setText(emailContent, true); // true = enable HTML
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom("no-reply@confido.com"); // set your sender email
      mailSender.send(mimeMessage);
    } catch (MailException | MessagingException e) {
      LOGGER.error("Failed to send email", e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  @Override
  public String buildResetPasswordEmail(String name, String link) {
    return """
        <div style="font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;">
            <h2 style="color: #1D70B8; text-align: center;">Confido App - Reset Your Password</h2>
            <p>Hi %s,</p>
            <p>You recently requested to reset your password. Click the button below to reset it:</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" style="background-color: #1D70B8; color: #fff; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                    Change My Password
                </a>
            </div>
            <p style="font-size: 14px; color: #555;">
                This link will expire in <strong>15 minutes</strong>. If you didn’t request a password reset, you can safely ignore this email.
            </p>
            <p>Thank you,<br/>The Support Team</p>
        </div>
        """
        .formatted(name, link);
  }
}
