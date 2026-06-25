package com.furniturebid.mockapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private UUID id;
    private String email;
    private String password;
    private String displayName;
    private String role;
    private String avatarUrl;
    private String status;
    private Instant createdAt;
}
