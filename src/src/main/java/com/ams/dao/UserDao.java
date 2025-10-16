package com.ams.dao;

import com.ams.db.Db;
import com.ams.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    public User insert(String username, String passwordHash, String role, String photoPath, String officialEmail, String registrationNumber) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role, photo_path, official_email, registration_number) VALUES(?,?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            ps.setString(4, photoPath);
            ps.setString(5, officialEmail);
            ps.setString(6, registrationNumber);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return findById(id);
                }
            }
        }
        return null;
    }

    public User update(long id, String username, String role, String photoPath, String officialEmail, String registrationNumber) throws SQLException {
        String sql = "UPDATE users SET username=?, role=?, photo_path=?, official_email=?, registration_number=? WHERE id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, photoPath);
            ps.setString(4, officialEmail);
            ps.setString(5, registrationNumber);
            ps.setLong(6, id);
            ps.executeUpdate();
        }
        return findById(id);
    }

    public void delete(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE role != 'ADMIN'")) {
            ps.executeUpdate();
        }
    }

    public User findById(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, password_hash, role, photo_path, official_email, registration_number, created_at FROM users WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, password_hash, role, photo_path, official_email, registration_number, created_at FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<User> listAll() throws SQLException {
        List<User> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, password_hash, role, photo_path, official_email, registration_number, created_at FROM users ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setPhotoPath(rs.getString("photo_path"));
        u.setOfficialEmail(rs.getString("official_email"));
        u.setRegistrationNumber(rs.getString("registration_number"));
        Timestamp ts = rs.getTimestamp("created_at");
        u.setCreatedAt(ts != null ? ts.toInstant() : Instant.now());
        return u;
    }
}
