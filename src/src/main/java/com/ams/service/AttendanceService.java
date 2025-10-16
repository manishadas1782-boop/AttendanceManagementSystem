package com.ams.service;

import com.ams.dao.AttendanceDao;

import java.sql.SQLException;

public class AttendanceService {
    private final AttendanceDao attendanceDao = new AttendanceDao();

    public void markPresent(long userId, long subjectId, String source) {
        markStatus(userId, subjectId, "PRESENT", source);
    }

    public void markAbsent(long userId, long subjectId, String source) {
        markStatus(userId, subjectId, "ABSENT", source);
    }

    public void markStatus(long userId, long subjectId, String status, String source) {
        try {
            attendanceDao.insert(userId, subjectId, status, source);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark attendance", e);
        }
    }
    
    // Legacy methods for backward compatibility
    public void markPresent(long userId, String source) {
        // Use default subject ID of 1 if available, or handle differently
        markStatus(userId, 1L, "PRESENT", source);
    }

    public void markAbsent(long userId, String source) {
        markStatus(userId, 1L, "ABSENT", source);
    }
}
