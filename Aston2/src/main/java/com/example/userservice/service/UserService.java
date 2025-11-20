package com.example.userservice.service;

import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.EmailAlreadyExistsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequest.getEmail());
        }

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
            return mapToUserResponse(user);
        }, throwable -> {
            // Fallback метод при ошибке
            return createFallbackUserResponse(id);
        });
    }

    public List<UserResponse> getAllUsers() {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(() -> {
            return userRepository.findAllOrderByCreatedAtDesc()
                    .stream()
                    .map(this::mapToUserResponse)
                    .collect(Collectors.toList());
        }, throwable -> {
            // Fallback при ошибке - возвращаем пустой список
            return Collections.emptyList();
        });
    }

    // Fallback метод
    private UserResponse createFallbackUserResponse(Long id) {
        UserResponse fallback = new UserResponse();
        fallback.setUserId(id);
        fallback.setName("Service Temporarily Unavailable");
        fallback.setEmail("fallback@example.com");
        fallback.setAge(0);
        fallback.setCreatedAt(LocalDateTime.now());
        return fallback;
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userRequest.getEmail() != null &&
                !existingUser.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequest.getEmail());
        }

        if (userRequest.getName() != null) {
            existingUser.setName(userRequest.getName());
        }
        if (userRequest.getEmail() != null) {
            existingUser.setEmail(userRequest.getEmail());
        }
        if (userRequest.getAge() != null) {
            existingUser.setAge(userRequest.getAge());
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public long getUserCount() {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(() -> {
            return userRepository.count();
        }, throwable -> {
            // Fallback при ошибке
            return 0L;
        });
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}