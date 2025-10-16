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
        return insert(userId, subjectId, status, source, null);
    }
    
    public AttendanceRecord insert(long userId, long subjectId, String status, String source, String checkInPhotoPath) throws SQLException {
        // First, try to add the column if it doesn't exist
        tryAddCheckInPhotoColumn();
        
        String sql = "INSERT INTO attendance_records(user_id, subject_id, status, source, check_in_photo_path) VALUES(?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, subjectId);
            ps.setString(3, status);
            ps.setString(4, source);
            ps.setString(5, checkInPhotoPath);
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
        tryAddCheckInPhotoColumn();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source, check_in_photo_path FROM attendance_records WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<AttendanceRecord> listAll() throws SQLException {
        tryAddCheckInPhotoColumn();
        List<AttendanceRecord> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source, check_in_photo_path FROM attendance_records ORDER BY marked_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    public List<AttendanceRecord> listByUser(long userId) throws SQLException {
        tryAddCheckInPhotoColumn();
        List<AttendanceRecord> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, subject_id, status, marked_at, source, check_in_photo_path FROM attendance_records WHERE user_id=? ORDER BY marked_at DESC")) {
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
        tryAddCheckInPhotoColumn();
        List<AttendanceRecord> out = new ArrayList<>();
        String sql = "SELECT id, user_id, subject_id, status, marked_at, source, check_in_photo_path FROM attendance_records " +
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
    
    public AttendanceRecord findByUserSubjectDate(long userId, long subjectId, LocalDate date) throws SQLException {
        tryAddCheckInPhotoColumn();
        String sql = "SELECT id, user_id, subject_id, status, marked_at, source, check_in_photo_path FROM attendance_records " +
                     "WHERE user_id = ? AND subject_id = ? AND DATE(marked_at) = ? ORDER BY marked_at DESC LIMIT 1";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, subjectId);
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }
    
    public boolean hasAttendanceForToday(long userId, long subjectId) throws SQLException {
        return findByUserSubjectDate(userId, subjectId, LocalDate.now()) != null;
    }
    
    public void deleteByDate(LocalDate date) throws SQLException {
        String sql = "DELETE FROM attendance_records WHERE DATE(marked_at) = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.executeUpdate();
        }
    }
    
    public void deleteById(long id) throws SQLException {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM attendance_records WHERE id=?")) {
            ps.setLong(1, id);
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
        a.setCheckInPhotoPath(rs.getString("check_in_photo_path"));
        return a;
    }

    private void tryAddCheckInPhotoColumn() {
        try (Connection c = Db.getConnection()) {
            // Check if column exists and add it if it doesn't
            String checkColumnSql = "PRAGMA table_info(attendance_records)";
            boolean columnExists = false;
            try (PreparedStatement ps = c.prepareStatement(checkColumnSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("check_in_photo_path".equals(rs.getString("name"))) {
                        columnExists = true;
                        break;
                    }
                }
            }
            
            if (!columnExists) {
                String addColumnSql = "ALTER TABLE attendance_records ADD COLUMN check_in_photo_path TEXT";
                try (PreparedStatement ps = c.prepareStatement(addColumnSql)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // Column likely already exists, ignore
        }
    }
}
