package com.ams.service;

import com.ams.dao.UserDao;
import com.ams.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User authenticate(String username, String rawPassword) {
        try {
            User u = userDao.findByUsername(username);
            if (u == null) return null;
            String hash = u.getPasswordHash();
            if (hash == null || hash.isBlank()) return null;
            return BCrypt.checkpw(rawPassword, hash) ? u : null;
        } catch (SQLException e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }

    public User registerUser(String username, String officialEmail, String registrationNumber, String rawPassword, String role) {
        // Validate SRM email domain
        if (!isValidSrmEmail(officialEmail)) {
            throw new RuntimeException("Only SRM official emails (@srmist.edu.in) are allowed");
        }
        
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new RuntimeException("Registration number is required");
        }
        
        try {
            String salt = BCrypt.gensalt(12);
            String hash = BCrypt.hashpw(rawPassword, salt);
            return userDao.insert(username, hash, role, null, officialEmail, registrationNumber.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }
    
    public void changeUserPassword(long userId, String newPassword) {
        try {
            String salt = BCrypt.gensalt(12);
            String hash = BCrypt.hashpw(newPassword, salt);
            userDao.updatePassword(userId, hash);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to change password: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidSrmEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.toLowerCase().endsWith("@srmist.edu.in");
    }
}
