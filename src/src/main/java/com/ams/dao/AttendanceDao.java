package com.ams.dao;

import com.ams.db.Db;
import com.ams.model.AttendanceRecord;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {
    public AttendanceRecord insert(long userId, long subjectId, String status, String source) throws SQLException {
        String sql = "INSERT INTO attendance_records(user_id, subject_id, status, source) VALUES(?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, subjectId);
            ps.setString(3, status);
            ps.setString(4, source);
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
    
    // Legacy method for backward compatibility
    public AttendanceRecord insert(long userId, String status, String source) throws SQLException {
        return insert(userId, 1L, status, source); // Default subject ID
    }

    public AttendanceRecord findById(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source FROM attendance_records WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<AttendanceRecord> listAll() throws SQLException {
        List<AttendanceRecord> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source FROM attendance_records ORDER BY marked_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public List<AttendanceRecord> listByUser(long userId) throws SQLException {
        List<AttendanceRecord> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source FROM attendance_records WHERE user_id=? ORDER BY marked_at DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public void updateStatus(long id, String status) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE attendance_records SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
    
    public List<AttendanceRecord> listByDate(LocalDate date) throws SQLException {
        List<AttendanceRecord> out = new ArrayList<>();
        String sql = "SELECT id, user_id, subject_id, status, marked_at, source FROM attendance_records " +
                     "WHERE DATE(marked_at) = ? ORDER BY marked_at DESC";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }
    
    public void deleteByDate(LocalDate date) throws SQLException {
        String sql = "DELETE FROM attendance_records WHERE DATE(marked_at) = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM attendance_records")) {
            ps.executeUpdate();
        }
    }

    private AttendanceRecord map(ResultSet rs) throws SQLException {
        AttendanceRecord a = new AttendanceRecord();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setSubjectId(rs.getLong("subject_id"));
        a.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("marked_at");
        a.setMarkedAt(ts != null ? ts.toInstant() : Instant.now());
        a.setSource(rs.getString("source"));
        return a;
    }
}
