package com.ams.ui;

import com.ams.dao.SubjectDao;
import com.ams.model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class SubjectsPanel extends JPanel {
    private final SubjectDao subjectDao = new SubjectDao();

    private final JTextField nameField = new JTextField(20);
    private final JTextField codeField = new JTextField(10);
    private final JTextArea descriptionField = new JTextArea(3, 20);

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID", "Code", "Name", "Description"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public SubjectsPanel() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Subject Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Subject Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; 
        descriptionField.setWrapStyleWord(true);
        descriptionField.setLineWrap(true);
        form.add(new JScrollPane(descriptionField), gbc);

        JButton addBtn = new JButton("Add Subject");
        addBtn.addActionListener(this::onAddSubject);
        JButton updateBtn = new JButton("Update Selected");
        updateBtn.addActionListener(this::onUpdateSubject);
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(this::onDeleteSubject);
        JButton deleteAllBtn = new JButton("Delete All Subjects");
        deleteAllBtn.addActionListener(this::onDeleteAllSubjects);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSubjects());

        JPanel actions = new JPanel();
        actions.add(addBtn);
        actions.add(updateBtn);
        actions.add(deleteBtn);
        actions.add(deleteAllBtn);
        actions.add(refreshBtn);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        loadSubjects();
    }

    private void onAddSubject(ActionEvent e) {
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        
        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject name and code are required");
            return;
        }
        
        try {
            subjectDao.insert(name, code, description);
            clearFields();
            loadSubjects();
            JOptionPane.showMessageDialog(this, "Subject added successfully");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add subject: " + ex.getMessage());
        }
    }

    private void onUpdateSubject(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a subject in the table");
            return;
        }
        
        long id = (Long) tableModel.getValueAt(row, 0);
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        String description = descriptionField.getText().trim();
        
        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject name and code are required");
            return;
        }
        
        try {
            subjectDao.update(id, name, code, description);
            clearFields();
            loadSubjects();
            JOptionPane.showMessageDialog(this, "Subject updated successfully");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update subject: " + ex.getMessage());
        }
    }

    private void onDeleteSubject(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a subject in the table");
            return;
        }
        
        long id = (Long) tableModel.getValueAt(row, 0);
        String code = (String) tableModel.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Delete subject " + code + "?", 
            "Confirm", 
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            subjectDao.delete(id);
            loadSubjects();
            JOptionPane.showMessageDialog(this, "Subject deleted successfully");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete subject: " + ex.getMessage());
        }
    }

    private void onDeleteAllSubjects(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete ALL subjects?\nThis action cannot be undone!", 
            "Confirm Delete All", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            subjectDao.deleteAll();
            loadSubjects();
            JOptionPane.showMessageDialog(this, "All subjects have been deleted.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete subjects: " + ex.getMessage());
        }
    }

    private void loadSubjects() {
        tableModel.setRowCount(0);
        try {
            List<Subject> subjects = subjectDao.listAll();
            for (Subject s : subjects) {
                tableModel.addRow(new Object[]{s.getId(), s.getCode(), s.getName(), s.getDescription()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.setText("");
        codeField.setText("");
        descriptionField.setText("");
    }
}