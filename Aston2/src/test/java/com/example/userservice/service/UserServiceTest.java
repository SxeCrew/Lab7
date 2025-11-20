package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.EmailAlreadyExistsException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.createUser(userRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L); // ← ИЗМЕНИТЬ НА getUserId()
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: john@example.com"); // ← ПРОСТОЕ СООБЩЕНИЕ

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldGetUserByIdSuccessfully() {
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L); // ← ИЗМЕНИТЬ НА getUserId()
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        user1.setCreatedAt(LocalDateTime.now());

        User user2 = new User("Jane Doe", "jane@example.com", 25);
        user2.setId(2L);
        user2.setCreatedAt(LocalDateTime.now());

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAllOrderByCreatedAtDesc()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Doe");

        verify(userRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User existingUser = new User("John Old", "john.old@example.com", 30);
        existingUser.setId(1L);
        existingUser.setCreatedAt(LocalDateTime.now());

        UserRequest userRequest = new UserRequest("John Updated", "john.updated@example.com", 35);
        User updatedUser = new User("John Updated", "john.updated@example.com", 35);
        updatedUser.setId(1L);
        updatedUser.setCreatedAt(existingUser.getCreatedAt());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, userRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@example.com");
        assertThat(result.getAge()).isEqualTo(35);

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("john.updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserWithSameEmail() {
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UserRequest userRequest = new UserRequest("John Updated", "john@example.com", 35);
        User updatedUser = new User("John Updated", "john@example.com", 35);
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, userRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        UserRequest userRequest = new UserRequest("John Updated", "john.updated@example.com", 35);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UserRequest userRequest = new UserRequest("John Updated", "existing@example.com", 35);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: existing@example.com"); // ← ПРОСТОЕ СООБЩЕНИЕ

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldCheckIfEmailExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("john@example.com");

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void shouldCheckIfEmailDoesNotExist() {
        when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("unknown@example.com");

        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("unknown@example.com");
    }

    @Test
    void shouldReturnUserCount() {
        when(userRepository.count()).thenReturn(5L);

        long result = userService.getUserCount();

        assertThat(result).isEqualTo(5L);
        verify(userRepository).count();
    }

    @Test
    void shouldMapUserToResponseCorrectly() {
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(1L);

        assertThat(result.getUserId()).isEqualTo(1L); // ← ИЗМЕНИТЬ НА getUserId()
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldHandleEmptyUserList() {
        when(userRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void shouldHandlePartialUpdateWithNullFields() {
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UserRequest partialUpdate = new UserRequest();
        partialUpdate.setName("John Updated");
        // email и age остаются null

        User updatedUser = new User("John Updated", "john@example.com", 30);
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, partialUpdate);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@example.com"); // осталось прежним
        assertThat(result.getAge()).isEqualTo(30); // осталось прежним

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldHandleEmptyUserListInGetAllUsers() {
        when(userRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAllOrderByCreatedAtDesc();
    }

    @Test
    void shouldUpdateUserWithPartialData() {
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(1L);

        UserRequest userRequest = new UserRequest();
        userRequest.setName("John Updated");
        userRequest.setEmail(null);
        userRequest.setAge(null);

        User updatedUser = new User("John Updated", "john@example.com", 30);
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, userRequest);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}