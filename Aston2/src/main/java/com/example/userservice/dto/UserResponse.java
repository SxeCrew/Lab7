package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

public class UserResponse extends RepresentationModel<UserResponse> {

    @JsonProperty("id")
    private Long userId;

    private String name;
    private String email;
    private Integer age;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long userId, String name, String email, Integer age, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}