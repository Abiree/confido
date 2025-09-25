package com.confido.api.auth.services;

import com.confido.api.auth.dtos.*;

public interface IAuthService {
  UserDTO register(UserDTO user);

  LoginResponse login(LoginRequest loginUser);

  UserDTO getCurrentUser();

  String forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

  String resetPassword(ResetPasswordRequest resetPasswordRequest);
}
