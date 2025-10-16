package com.ams.ui;

import com.ams.dao.UserDao;
import com.ams.model.User;
import com.ams.service.AuthService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class RegistrationDialog extends JDialog {
    private final JTextField usernameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField regNumberField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);
    private final JLabel photoPreview = new JLabel();
    private File selectedPhotoFile = null;
    
    // Supported image formats
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final AuthService authService = new AuthService();
    private final UserDao userDao = new UserDao();

    private boolean success = false;
    private String createdUsername = null;

    public RegistrationDialog(Window owner) {
        super(owner, "Register", ModalityType.APPLICATION_MODAL);
        setSize(520, 360);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx=1; gbc.gridy=0; form.add(usernameField, gbc);

        gbc.gridx=0; gbc.gridy=1; form.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; gbc.gridy=1; form.add(passwordField, gbc);

        gbc.gridx=0; gbc.gridy=2; form.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx=1; gbc.gridy=2; form.add(confirmField, gbc);

        gbc.gridx=0; gbc.gridy=3; form.add(new JLabel("Official Email:"), gbc);
        gbc.gridx=1; gbc.gridy=3; form.add(emailField, gbc);

        gbc.gridx=0; gbc.gridy=4; form.add(new JLabel("Registration Number:"), gbc);
        regNumberField.setToolTipText("Format: RA followed by 13 digits (e.g., RA2412704010008)");
        gbc.gridx=1; gbc.gridy=4; form.add(regNumberField, gbc);

        JButton choosePhoto = new JButton("Choose Photo...");
        choosePhoto.addActionListener(this::onChoosePhoto);
        JButton capturePhoto = new JButton("Capture from Camera...");
        capturePhoto.addActionListener(this::onCaptureFromCamera);
        gbc.gridx=0; gbc.gridy=5; form.add(new JLabel("Photo (optional):"), gbc);
        gbc.gridx=1; gbc.gridy=5; form.add(choosePhoto, gbc);
        gbc.gridx=1; gbc.gridy=6; form.add(capturePhoto, gbc);

        photoPreview.setPreferredSize(new Dimension(120,120));
        photoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx=2; gbc.gridy=0; gbc.gridheight=7; form.add(photoPreview, gbc);
        gbc.gridheight=1;

        JPanel actions = new JPanel();
        JButton register = new JButton("Register");
        register.addActionListener(this::onRegister);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        actions.add(register);
        actions.add(cancel);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void onChoosePhoto(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        
        // Set up file filter for images only
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
            "Image files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)",
            "jpg", "jpeg", "png", "gif", "bmp"
        );
        fc.setFileFilter(imageFilter);
        fc.setAcceptAllFileFilterUsed(false); // Only show image files
        
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            
            // Validate the selected file
            if (validateImageFile(selectedFile)) {
                selectedPhotoFile = selectedFile;
                try {
                    Image img = ImageIO.read(selectedPhotoFile);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                        photoPreview.setIcon(new ImageIcon(scaled));
                    } else {
                        JOptionPane.showMessageDialog(this, "Unable to read the image file. Please select a valid image.");
                        selectedPhotoFile = null;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
                    selectedPhotoFile = null;
                }
            }
        }
    }

    private void onCaptureFromCamera(ActionEvent e) {
        CameraCaptureDialog dialog = new CameraCaptureDialog(this);
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

    private void onRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String role = "USER";

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required");
            return;
        }
        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        try {
            String email = emailField.getText().trim();
            String regNumber = regNumberField.getText().trim();
            if (email.isEmpty()) { JOptionPane.showMessageDialog(this, "Official Email is required"); return; }
            if (regNumber.isEmpty()) { JOptionPane.showMessageDialog(this, "Registration Number is required"); return; }
            
            // Validate registration number format: RA followed by 13 digits
            if (!regNumber.matches("^RA\\d{13}$")) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid Registration Number format.\n" +
                    "Required format: RA followed by 13 digits\n" +
                    "Example: RA2412704010008");
                return;
            }
            User created = authService.registerUser(username, email, regNumber, pass, role);
            if (created == null) {
                JOptionPane.showMessageDialog(this, "Registration failed");
                return;
            }
            // Optional photo handling
            if (selectedPhotoFile != null) {
                Path dir = Path.of("uploads", "users");
                Files.createDirectories(dir);
                Path dest = dir.resolve(created.getId() + getFileExtension(selectedPhotoFile.getName()));
                Files.copy(selectedPhotoFile.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                userDao.update(created.getId(), created.getUsername(), created.getRole(), dest.toString(), email, regNumber);
            }
            this.createdUsername = created.getUsername();
            this.success = true;
            JOptionPane.showMessageDialog(this, "Registration successful. You can now log in.");
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save photo/user: " + ex.getMessage());
        }
    }

    /**
     * Validates if the selected file is a valid image file
     */
    private boolean validateImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            JOptionPane.showMessageDialog(this, "Please select a valid file.");
            return false;
        }
        
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            JOptionPane.showMessageDialog(this, 
                "File size too large. Please select an image smaller than 10MB.");
            return false;
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex + 1);
        }
        
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            JOptionPane.showMessageDialog(this, 
                "Unsupported file format. Please select a valid image file (JPG, PNG, GIF, BMP).");
            return false;
        }
        
        // Try to read the image to verify it's a valid image file
        try {
            Image img = ImageIO.read(file);
            if (img == null) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid image file. Please select a valid image.");
                return false;
            }
            
            // Check minimum image dimensions
            if (img.getWidth(null) < 50 || img.getHeight(null) < 50) {
                JOptionPane.showMessageDialog(this, 
                    "Image too small. Please select an image at least 50x50 pixels.");
                return false;
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Cannot read image file. Please select a valid image.");
            return false;
        }
        
        return true;
    }
    
    private static String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".jpg";
    }

    public boolean isSuccess() { return success; }
    public String getCreatedUsername() { return createdUsername; }
}