package com.ams.ui;

import com.ams.dao.AttendanceDao;
import com.ams.dao.UserDao;
import com.ams.dao.SubjectDao;
import com.ams.model.AttendanceRecord;
import com.ams.model.User;
import com.ams.model.Subject;
import com.ams.service.ExportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;

public class AttendancePanel extends JPanel {
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final UserDao userDao = new UserDao();
    private final SubjectDao subjectDao = new SubjectDao();
    private final ExportService exportService = new ExportService();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Registration Number", "Name", "Email", "Subject", "Status", "Marked At", "Source"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final List<Long> recordIds = new ArrayList<>();

    private final boolean isAdmin;
    private final User currentUser;

    public AttendancePanel(User currentUser, boolean isAdmin) {
        this.currentUser = currentUser;
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());
        JTable table = new JTable(model);
        // Hide the ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        bottom.add(refresh);
        
        if (isAdmin) {
            JButton markPresent = new JButton("Mark PRESENT");
            JButton markAbsent = new JButton("Mark ABSENT");
            markPresent.addActionListener(e -> updateSelected(table, "PRESENT"));
            markAbsent.addActionListener(e -> updateSelected(table, "ABSENT"));
            bottom.add(markPresent);
            bottom.add(markAbsent);
            
            JButton clearToday = new JButton("Clear Today's Records");
            clearToday.addActionListener(e -> clearTodaysRecords());
            bottom.add(clearToday);
            
            JButton clearAll = new JButton("Clear All Records");
            clearAll.addActionListener(e -> clearAllRecords());
            bottom.add(clearAll);
            
            JButton exportTodayCSV = new JButton("Export Today (CSV)");
            exportTodayCSV.addActionListener(e -> exportTodayToCSV());
            bottom.add(exportTodayCSV);
            
            JButton exportAllCSV = new JButton("Export All (CSV)");
            exportAllCSV.addActionListener(e -> exportAllToCSV());
            bottom.add(exportAllCSV);
            
            JButton copyGoogleSheets = new JButton("Copy for Google Sheets");
            copyGoogleSheets.addActionListener(e -> copyForGoogleSheets());
            bottom.add(copyGoogleSheets);
        }
        add(bottom, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        model.setRowCount(0);
        recordIds.clear();
        try {
            List<AttendanceRecord> list = isAdmin ? attendanceDao.listAll() : attendanceDao.listByUser(currentUser.getId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // Cache users and subjects for performance
            Map<Long, User> userCache = new HashMap<>();
            Map<Long, Subject> subjectCache = new HashMap<>();
            
            for (AttendanceRecord a : list) {
                // Get user info
                User user = userCache.get(a.getUserId());
                if (user == null) {
                    user = userDao.findById(a.getUserId());
                    if (user != null) userCache.put(a.getUserId(), user);
                }
                
                // Get subject info
                Subject subject = subjectCache.get(a.getSubjectId());
                if (subject == null) {
                    subject = subjectDao.findById(a.getSubjectId());
                    if (subject != null) subjectCache.put(a.getSubjectId(), subject);
                }
                
                String regNumber = user != null ? user.getRegistrationNumber() : "N/A";
                String name = user != null ? user.getUsername() : "N/A";
                String email = user != null ? user.getOfficialEmail() : "N/A";
                String subjectName = subject != null ? subject.getName() : "N/A";
                String markedAt = a.getMarkedAt() != null ? fmt.format(a.getMarkedAt().atZone(java.time.ZoneId.systemDefault())) : "";
                
                recordIds.add(a.getId());
                model.addRow(new Object[]{a.getId(), regNumber, name, email, subjectName, a.getStatus(), markedAt, a.getSource()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load attendance: " + e.getMessage());
        }
    }

    private void updateSelected(JTable table, String status) {
        int row = table.getSelectedRow();
        if (row < 0) { 
            JOptionPane.showMessageDialog(this, "Select a record"); 
            return; 
        }
        if (row >= recordIds.size()) {
            JOptionPane.showMessageDialog(this, "Invalid record selected");
            return;
        }
        
        try {
            long recordId = recordIds.get(row);
            attendanceDao.updateStatus(recordId, status);
            JOptionPane.showMessageDialog(this, "Status updated to " + status);
            load();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update status: " + e.getMessage());
        }
    }
    
    private void clearTodaysRecords() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear today's attendance records?\nThis action cannot be undone!", 
            "Confirm Clear Today", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            attendanceDao.deleteByDate(LocalDate.now());
            load();
            JOptionPane.showMessageDialog(this, "Today's attendance records have been cleared.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to clear records: " + e.getMessage());
        }
    }
    
    private void clearAllRecords() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear ALL attendance records?\nThis action cannot be undone!", 
            "Confirm Clear All", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            attendanceDao.deleteAll();
            load();
            JOptionPane.showMessageDialog(this, "All attendance records have been cleared.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to clear records: " + e.getMessage());
        }
    }
    
    private void exportTodayToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Today's Attendance as CSV");
        fileChooser.setSelectedFile(new File("attendance-" + LocalDate.now().toString() + ".csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (exportService.exportTodaysAttendanceToCSV(fileToSave.getAbsolutePath())) {
                JOptionPane.showMessageDialog(this, "Today's attendance exported successfully to: " + fileToSave.getAbsolutePath());
            }
        }
    }
    
    private void exportAllToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save All Attendance Records as CSV");
        fileChooser.setSelectedFile(new File("attendance-all-" + LocalDate.now().toString() + ".csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (exportService.exportAllAttendanceToCSV(fileToSave.getAbsolutePath())) {
                JOptionPane.showMessageDialog(this, "All attendance records exported successfully to: " + fileToSave.getAbsolutePath());
            }
        }
    }
    
    private void copyForGoogleSheets() {
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Select data to copy:",
            "Google Sheets Export",
            JOptionPane.PLAIN_MESSAGE,
            null,
            new String[]{"Today's Records", "All Records"},
            "Today's Records"
        );
        
        if (choice != null) {
            LocalDate date = choice.equals("Today's Records") ? LocalDate.now() : null;
            String data = exportService.generateGoogleSheetsFormat(date);
            
            // Copy to clipboard
            StringSelection stringSelection = new StringSelection(data);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            JOptionPane.showMessageDialog(this, 
                "Data copied to clipboard!\n\nYou can now:\n" +
                "1. Open Google Sheets\n" +
                "2. Select a cell\n" +
                "3. Press Ctrl+V to paste\n" +
                "4. The data will be formatted automatically");
        }
    }
}
