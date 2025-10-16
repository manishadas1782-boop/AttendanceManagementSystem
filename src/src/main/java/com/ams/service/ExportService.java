package com.ams.service;

import com.ams.dao.AttendanceDao;
import com.ams.dao.UserDao;
import com.ams.dao.SubjectDao;
import com.ams.model.AttendanceRecord;
import com.ams.model.User;
import com.ams.model.Subject;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ExportService {
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final UserDao userDao = new UserDao();
    private final SubjectDao subjectDao = new SubjectDao();

    public boolean exportAttendanceToCSV(LocalDate date, String filePath) {
        try {
            List<AttendanceRecord> records = (date != null) ? 
                attendanceDao.listByDate(date) : 
                attendanceDao.listAll();
            
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No attendance records found for the selected date.");
                return false;
            }
            
            // Cache users and subjects for performance
            Map<Long, User> userCache = new HashMap<>();
            Map<Long, Subject> subjectCache = new HashMap<>();
            
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write CSV header
                writer.append("Registration Number,Name,Official Email,Subject,Status,Marked At,Source\n");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                for (AttendanceRecord record : records) {
                    // Get user info
                    User user = userCache.get(record.getUserId());
                    if (user == null) {
                        user = userDao.findById(record.getUserId());
                        if (user != null) userCache.put(record.getUserId(), user);
                    }
                    
                    // Get subject info
                    Subject subject = subjectCache.get(record.getSubjectId());
                    if (subject == null) {
                        subject = subjectDao.findById(record.getSubjectId());
                        if (subject != null) subjectCache.put(record.getSubjectId(), subject);
                    }
                    
                    String regNumber = user != null ? escapeCSV(user.getRegistrationNumber()) : "N/A";
                    String name = user != null ? escapeCSV(user.getUsername()) : "N/A";
                    String email = user != null ? escapeCSV(user.getOfficialEmail()) : "N/A";
                    String subjectName = subject != null ? escapeCSV(subject.getName()) : "N/A";
                    String markedAt = record.getMarkedAt() != null ? 
                        formatter.format(record.getMarkedAt().atZone(java.time.ZoneId.systemDefault())) : "";
                    
                    // Write CSV row
                    writer.append(regNumber).append(",")
                          .append(name).append(",")
                          .append(email).append(",")
                          .append(subjectName).append(",")
                          .append(record.getStatus()).append(",")
                          .append(markedAt).append(",")
                          .append(record.getSource()).append("\n");
                }
            }
            
            return true;
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to export attendance: " + e.getMessage());
            return false;
        }
    }
    
    public boolean exportAllAttendanceToCSV(String filePath) {
        return exportAttendanceToCSV(null, filePath);
    }
    
    public boolean exportTodaysAttendanceToCSV(String filePath) {
        return exportAttendanceToCSV(LocalDate.now(), filePath);
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // Escape CSV special characters
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
    
    // Method to create Google Sheets compatible format
    public String generateGoogleSheetsFormat(LocalDate date) {
        try {
            List<AttendanceRecord> records = (date != null) ? 
                attendanceDao.listByDate(date) : 
                attendanceDao.listAll();
            
            if (records.isEmpty()) {
                return "No attendance records found.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Attendance Report - ").append(date != null ? date.toString() : "All Records").append("\n\n");
            sb.append("Registration Number\tName\tOfficial Email\tSubject\tStatus\tMarked At\tSource\n");
            
            // Cache users and subjects for performance
            Map<Long, User> userCache = new HashMap<>();
            Map<Long, Subject> subjectCache = new HashMap<>();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (AttendanceRecord record : records) {
                // Get user info
                User user = userCache.get(record.getUserId());
                if (user == null) {
                    user = userDao.findById(record.getUserId());
                    if (user != null) userCache.put(record.getUserId(), user);
                }
                
                // Get subject info
                Subject subject = subjectCache.get(record.getSubjectId());
                if (subject == null) {
                    subject = subjectDao.findById(record.getSubjectId());
                    if (subject != null) subjectCache.put(record.getSubjectId(), subject);
                }
                
                String regNumber = user != null ? user.getRegistrationNumber() : "N/A";
                String name = user != null ? user.getUsername() : "N/A";
                String email = user != null ? user.getOfficialEmail() : "N/A";
                String subjectName = subject != null ? subject.getName() : "N/A";
                String markedAt = record.getMarkedAt() != null ? 
                    formatter.format(record.getMarkedAt().atZone(java.time.ZoneId.systemDefault())) : "";
                
                sb.append(regNumber).append("\t")
                  .append(name).append("\t")
                  .append(email).append("\t")
                  .append(subjectName).append("\t")
                  .append(record.getStatus()).append("\t")
                  .append(markedAt).append("\t")
                  .append(record.getSource()).append("\n");
            }
            
            return sb.toString();
        } catch (SQLException e) {
            return "Error generating report: " + e.getMessage();
        }
    }
}