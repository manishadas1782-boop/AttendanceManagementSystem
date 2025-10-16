package com.ams.dao;

import com.ams.db.Db;
import com.ams.model.Subject;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SubjectDao {

    public Subject insert(String name, String code, String description) throws SQLException {
        String sql = "INSERT INTO subjects(name, code, description) VALUES(?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, code);
            ps.setString(3, description);
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

    public Subject update(long id, String name, String code, String description) throws SQLException {
        String sql = "UPDATE subjects SET name=?, code=?, description=? WHERE id=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, code);
            ps.setString(3, description);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
        return findById(id);
    }

    public void delete(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subjects WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Subject findById(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, code, description, created_at FROM subjects WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public Subject findByCode(String code) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, code, description, created_at FROM subjects WHERE code=?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Subject> listAll() throws SQLException {
        List<Subject> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, code, description, created_at FROM subjects ORDER BY code ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public void deleteAll() throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subjects")) {
            ps.executeUpdate();
        }
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setCode(rs.getString("code"));
        s.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        s.setCreatedAt(ts != null ? ts.toInstant() : Instant.now());
        return s;
    }
}