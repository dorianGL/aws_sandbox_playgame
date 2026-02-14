package com.aws.dodo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.Objects;
import java.util.UUID;

@DynamoDbBean
public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private Long createdAt;
    private Long updatedAt;

    public User() {
    }

    public User(String userId, String name, String email, String phone, Long createdAt, Long updatedAt) {
        if (userId == null || userId.isEmpty()) {
            this.userId = UUID.randomUUID().toString();
        } else {
            this.userId = userId;
        }
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = Objects.requireNonNullElseGet(createdAt, System::currentTimeMillis);
        this.updatedAt = Objects.requireNonNullElseGet(updatedAt, System::currentTimeMillis);
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}