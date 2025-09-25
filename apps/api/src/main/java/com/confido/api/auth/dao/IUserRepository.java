package com.confido.api.auth.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.confido.api.auth.models.User;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByResetPasswordToken(String resetPasswordToken);

  boolean existsByEmail(String email);
}
