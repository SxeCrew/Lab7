package com.example.userservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void userRequestShouldWorkCorrectly() {
        UserRequest request = new UserRequest("John Doe", "john@example.com", 30);
        assertThat(request.getName()).isEqualTo("John Doe");
        assertThat(request.getEmail()).isEqualTo("john@example.com");
        assertThat(request.getAge()).isEqualTo(30);
    }

    @Test
    void userResponseShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse response = new UserResponse(1L, "John Doe", "john@example.com", 30, now);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAge()).isEqualTo(30);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void userRequestSettersWork() {
        UserRequest request = new UserRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setAge(20);

        assertThat(request.getName()).isEqualTo("Test");
        assertThat(request.getEmail()).isEqualTo("test@example.com");
        assertThat(request.getAge()).isEqualTo(20);
    }

    @Test
    void userResponseSettersWork() {
        UserResponse response = new UserResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setUserId(1L);
        response.setName("Test");
        response.setEmail("test@example.com");
        response.setAge(20);
        response.setCreatedAt(now);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAge()).isEqualTo(20);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }
}