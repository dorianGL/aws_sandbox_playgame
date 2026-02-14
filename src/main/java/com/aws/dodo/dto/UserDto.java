package com.aws.dodo.dto;

import com.aws.dodo.User;

public record UserDto(
        String userId,
        String name,
        String email,
        String phone,
        Long createdAt,
        Long updatedAt) {

    public User toEntity() {
        return new User(userId, name, email, phone, createdAt, updatedAt);
    }
}
