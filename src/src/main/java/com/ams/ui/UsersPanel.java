package com.ams.ui;

import com.ams.dao.UserDao;
import com.ams.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class UsersPanel extends JPanel {
    private final UserDao userDao = new UserDao();

    private final JTextField usernameField = new JTextField(16);
    private final JComboBox<String> roleField = new JComboBox<>(new String[]{"USER","ADMIN"});
    private final JTextField emailField = new JTextField(18);
    private final JTextField regNumberField = new JTextField(16);
    private final JLabel photoPreview = new JLabel();
    private File selectedPhotoFile = null;

    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"ID","Username","Role","Email","Registration Number","Photo"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public UsersPanel() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx=1; gbc.gridy=0; form.add(usernameField, gbc);

        gbc.gridx=0; gbc.gridy=1; form.add(new JLabel("Role:"), gbc);
        gbc.gridx=1; gbc.gridy=1; form.add(roleField, gbc);

        gbc.gridx=0; gbc.gridy=2; form.add(new JLabel("Official Email:"), gbc);
        gbc.gridx=1; gbc.gridy=2; form.add(emailField, gbc);

        gbc.gridx=0; gbc.gridy=3; form.add(new JLabel("Registration Number:"), gbc);
        gbc.gridx=1; gbc.gridy=3; form.add(regNumberField, gbc);

        JButton choosePhoto = new JButton("Choose Photo...");
        choosePhoto.addActionListener(this::onChoosePhoto);
        JButton capturePhoto = new JButton("Capture from Camera...");
        capturePhoto.addActionListener(this::onCaptureFromCamera);
        gbc.gridx=0; gbc.gridy=4; form.add(new JLabel("Photo:"), gbc);
        gbc.gridx=1; gbc.gridy=4; form.add(choosePhoto, gbc);
        gbc.gridx=1; gbc.gridy=5; form.add(capturePhoto, gbc);

        photoPreview.setPreferredSize(new Dimension(120,120));
        photoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx=2; gbc.gridy=0; gbc.gridheight=6; form.add(photoPreview, gbc);
        gbc.gridheight=1;

        JButton addBtn = new JButton("Register User");
        addBtn.addActionListener(this::onAddUser);
        JButton updateBtn = new JButton("Update Selected");
        updateBtn.addActionListener(this::onUpdateUser);
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(this::onDeleteUser);
        JButton viewPhotoBtn = new JButton("View Photo");
        viewPhotoBtn.addActionListener(this::onViewPhoto);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers());
        JButton deleteAllBtn = new JButton("Delete All Users");
        deleteAllBtn.addActionListener(this::onDeleteAllUsers);

        JPanel actions = new JPanel();
        actions.add(addBtn);
        actions.add(updateBtn);
        actions.add(deleteBtn);
        actions.add(deleteAllBtn);
        actions.add(viewPhotoBtn);
        actions.add(refreshBtn);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        loadUsers();
    }

    private void onChoosePhoto(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = fc.getSelectedFile();
            try {
                Image img = ImageIO.read(selectedPhotoFile);
                if (img != null) {
                    Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    photoPreview.setIcon(new ImageIcon(scaled));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
            }
        }
    }

    private void onCaptureFromCamera(ActionEvent e) {
        CameraCaptureDialog dialog = new CameraCaptureDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            try {
                selectedPhotoFile = dialog.getCapturedFile().toFile();
                Image img = ImageIO.read(selectedPhotoFile);
                if (img != null) {
                    Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    photoPreview.setIcon(new ImageIcon(scaled));
                }
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(this, "Failed to load captured image: " + ioException.getMessage());
            }
        }
    }

    private void onAddUser(ActionEvent e) {
        String username = usernameField.getText().trim();
        String role = (String) roleField.getSelectedItem();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username required");
            return;
        }
        try {
            String email = emailField.getText().trim();
            String regNumber = regNumberField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Official Email required"); return; }
            if (regNumber.isEmpty()) { JOptionPane.showMessageDialog(this, "Registration Number required"); return; }
            // For demo, store a dummy password hash until admin sets/reset password.
            User created = userDao.insert(username, "dummy_hash", role, null, email, regNumber);
            if (created != null && selectedPhotoFile != null) {
                Path dir = Path.of("uploads", "users");
                Files.createDirectories(dir);
                Path dest = dir.resolve(created.getId() + getFileExtension(selectedPhotoFile.getName()));
                Files.copy(selectedPhotoFile.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                userDao.update(created.getId(), created.getUsername(), created.getRole(), dest.toString(), email, regNumber);
            }
            selectedPhotoFile = null;
            photoPreview.setIcon(null);
            usernameField.setText("");
            emailField.setText("");
            regNumberField.setText("");
            loadUsers();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to register user: " + ex.getMessage());
        }
    }

    private void onUpdateUser(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user in the table");
            return;
        }
        long id = (Long) tableModel.getValueAt(row, 0);
        String username = usernameField.getText().trim();
        String role = (String) roleField.getSelectedItem();
        String photoPath = (String) tableModel.getValueAt(row, 5);
        try {
            if (selectedPhotoFile != null) {
                Path dir = Path.of("uploads", "users");
                Files.createDirectories(dir);
                Path dest = dir.resolve(id + getFileExtension(selectedPhotoFile.getName()));
                Files.copy(selectedPhotoFile.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                photoPath = dest.toString();
            }
            String email = emailField.getText().trim();
            String regNumber = regNumberField.getText().trim();
            userDao.update(id, username, role, photoPath, email, regNumber);
            selectedPhotoFile = null;
            photoPreview.setIcon(null);
            loadUsers();
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update user: " + ex.getMessage());
        }
    }

    private void onViewPhoto(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user in the table"); return; }
        String path = (String) tableModel.getValueAt(row, 5);
        if (path == null || path.isBlank()) { JOptionPane.showMessageDialog(this, "No photo uploaded for this user"); return; }
        try {
            var img = javax.imageio.ImageIO.read(new java.io.File(path));
            if (img == null) { JOptionPane.showMessageDialog(this, "Cannot read photo file"); return; }
            Image scaled = img.getScaledInstance(360, 360, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            JOptionPane.showMessageDialog(this, lbl, "Photo Preview", JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load photo: " + ex.getMessage());
        }
    }

    private void onDeleteUser(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user in the table");
            return;
        }
        long id = (Long) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            userDao.delete(id);
            loadUsers();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete user: " + ex.getMessage());
        }
    }

    private void onDeleteAllUsers(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete ALL users (except admins)?\nThis action cannot be undone!", 
            "Confirm Delete All", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            userDao.deleteAll();
            loadUsers();
            JOptionPane.showMessageDialog(this, "All users (except admins) have been deleted.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete users: " + ex.getMessage());
        }
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDao.listAll();
            for (User u : users) {
                tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole(), u.getOfficialEmail(), u.getRegistrationNumber(), u.getPhotoPath()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
    }

    private static String getFileExtension(String name) {
        int dot = name.lastIndexOf('.')
;        return dot >= 0 ? name.substring(dot) : ".jpg";
    }
}
