package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // ← ДОБАВЬТЕ ЭТОТ ИМПОРТ

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("John Doe", "john@example.com", 30);
        User savedUser = userRepository.save(user);

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("John Doe");
        assertThat(foundUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User("John Doe", "john@example.com", 30);
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = new User("John Doe", "john@example.com", 30);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnUsersOrderedByCreationDate() {
        User user1 = new User("First User", "first@example.com", 20);
        User user2 = new User("Second User", "second@example.com", 25);

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAllOrderByCreatedAtDesc();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getEmail()).isEqualTo("second@example.com");
        assertThat(users.get(1).getEmail()).isEqualTo("first@example.com");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("unknown@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("unknown@example.com");

        assertThat(exists).isFalse();
    }
}