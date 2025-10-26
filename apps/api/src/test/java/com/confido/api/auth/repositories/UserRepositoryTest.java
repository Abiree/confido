package com.confido.api.auth.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.confido.api.auth.dao.IUserRepository;
import com.confido.api.auth.models.User;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTest {

  @Autowired private IUserRepository userRepository;

  @Test
  public void userRepository_saveAll_ReturnsSavedUsers() {
    User user = User.builder().email("user@gmail.com").password("gygfdiueg11@@@").build();
    userRepository.save(user);
    Assertions.assertNotNull(userRepository.findAll());
    Assertions.assertEquals(1, userRepository.findAll().size());
  }

  @Test
  public void userRepository_GetAll_ReturnsTwoUsers() {
    User user = User.builder().email("user1@gmail.com").password("test123").build();
    User user2 = User.builder().email("user2@gmail.com").password("kit123").build();
    userRepository.save(user);
    userRepository.save(user2);
    Assertions.assertEquals(2, userRepository.findAll().size());
  }

  @Test
  public void userRepository_GetUserByEmail_ReturnsUserNotNull() {
    User user = User.builder().email("user@gmail.com").password("test123").build();
    userRepository.save(user);
    Optional<User> user2 = userRepository.findByEmail(user.getEmail());
    Assertions.assertTrue(user2.isPresent());
  }
}
