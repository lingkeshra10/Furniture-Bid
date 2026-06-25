package com.furniturebid.mockapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public class AdminUserRowDto {

    private UUID id;
    private String displayName;
    private String email;
    private String role;
    private String status;
    private Instant registeredAt;

    public AdminUserRowDto() {
    }

    public AdminUserRowDto(UUID id, String displayName, String email,
                           String role, String status, Instant registeredAt) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }
}
