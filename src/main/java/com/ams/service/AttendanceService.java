package com.ams.service;

import com.ams.dao.AttendanceDao;

import java.sql.SQLException;

public class AttendanceService {
    private final AttendanceDao attendanceDao = new AttendanceDao();

    public void markPresent(long userId, long subjectId, String source) {
        markStatus(userId, subjectId, "PRESENT", source);
    }
    
    public void markPresent(long userId, long subjectId, String source, String checkInPhotoPath) {
        markStatus(userId, subjectId, "PRESENT", source, checkInPhotoPath);
    }

    public void markAbsent(long userId, long subjectId, String source) {
        markStatus(userId, subjectId, "ABSENT", source);
    }
    
    public void markAbsent(long userId, long subjectId, String source, String checkInPhotoPath) {
        markStatus(userId, subjectId, "ABSENT", source, checkInPhotoPath);
    }

    public void markStatus(long userId, long subjectId, String status, String source) {
        try {
            attendanceDao.insert(userId, subjectId, status, source);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark attendance", e);
        }
    }
    
    public void markStatus(long userId, long subjectId, String status, String source, String checkInPhotoPath) {
        try {
            attendanceDao.insert(userId, subjectId, status, source, checkInPhotoPath);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark attendance", e);
        }
    }
    
    public boolean hasAttendanceToday(long userId, long subjectId) {
        try {
            return attendanceDao.hasAttendanceForToday(userId, subjectId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check attendance", e);
        }
    }
    
    public void markPresentWithCheck(long userId, long subjectId, String source, String checkInPhotoPath) {
        if (hasAttendanceToday(userId, subjectId)) {
            throw new RuntimeException("Attendance already marked for this subject today. Only one attendance per subject per day is allowed.");
        }
        markPresent(userId, subjectId, source, checkInPhotoPath);
    }
    
    public void markAbsentWithCheck(long userId, long subjectId, String source, String checkInPhotoPath) {
        if (hasAttendanceToday(userId, subjectId)) {
            throw new RuntimeException("Attendance already marked for this subject today. Only one attendance per subject per day is allowed.");
        }
        markAbsent(userId, subjectId, source, checkInPhotoPath);
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
