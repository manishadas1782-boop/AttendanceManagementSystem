package com.ams.model;

import java.time.Instant;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String role;
    private String photoPath;
    private String officialEmail;
    private String registrationNumber;
    private Instant createdAt;

    public User() {}

    public User(long id, String username, String passwordHash, String role) {
        this(id, username, passwordHash, role, null, null, null, null);
    }

    public User(long id, String username, String passwordHash, String role, String photoPath, String officialEmail, String registrationNumber, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.photoPath = photoPath;
        this.officialEmail = officialEmail;
        this.registrationNumber = registrationNumber;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getOfficialEmail() { return officialEmail; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
