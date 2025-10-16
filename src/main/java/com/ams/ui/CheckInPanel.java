package com.ams.ui;

import com.ams.dao.UserDao;
import com.ams.dao.SubjectDao;
import com.ams.model.User;
import com.ams.model.Subject;
import com.ams.service.AttendanceService;
import com.ams.service.FaceService;
import com.ams.util.CameraUtil;
import com.github.sarxos.webcam.Webcam;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CheckInPanel extends JPanel {
    private final User currentUser;
    private final JLabel usernameLabel = new JLabel();
    private final JComboBox<Subject> subjectSelect = new JComboBox<>();
    private final JComboBox<String> cameraSelect = new JComboBox<>(new String[]{"Auto (0)", "Camera 0", "Camera 1", "Camera 2", "Camera 3"});
    private final JLabel cameraLabel = new JLabel("Camera not started", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton startBtn = new JButton("Start Camera");
    private final JButton stopBtn = new JButton("Stop Camera");
    private final JButton captureBtn = new JButton("Capture & Verify");

    private final UserDao userDao = new UserDao();
    private final SubjectDao subjectDao = new SubjectDao();
    private final AttendanceService attendanceService = new AttendanceService();
    private final FaceService faceService = new FaceService();

    // Preferred backend: OpenCV VideoCapture; fallback to webcam-capture
    private VideoCapture videoCapture;
    private Webcam webcam;
    private volatile boolean running = false;
    private volatile boolean faceDetected = false;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;

    public CheckInPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        usernameLabel.setText("Full Name: " + (currentUser != null ? currentUser.getUsername() : "Unknown"));
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(Font.BOLD));
        top.add(usernameLabel);
        
        top.add(new JLabel("Subject:"));
        loadSubjects();
        top.add(subjectSelect);
        
        top.add(new JLabel("Camera:"));
        top.add(cameraSelect);
        startBtn.addActionListener(e -> startCamera());
        top.add(startBtn);
        stopBtn.addActionListener(e -> shutdownCamera());
        stopBtn.setEnabled(false);
        top.add(stopBtn);
        captureBtn.addActionListener(e -> onCapture());
        captureBtn.setEnabled(false);
        top.add(captureBtn);
        add(top, BorderLayout.NORTH);

        cameraLabel.setPreferredSize(new Dimension(640, 480));
        cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(cameraLabel, BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(statusLabel, BorderLayout.SOUTH);
    }


    private void startCamera() {
        if (running) return;
        try {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            cameraLabel.setText(null);
            int idx = switch (cameraSelect.getSelectedIndex()) {
                case 1 -> 0; // Camera 0
                case 2 -> 1; // Camera 1
                case 3 -> 2; // Camera 2
                case 4 -> 3; // Camera 3
                default -> 0; // Auto -> 0
            };
            videoCapture = new VideoCapture(idx);
            if (!videoCapture.isOpened()) {
                // fallback to default webcam-capture
                videoCapture = null;
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    JOptionPane.showMessageDialog(this, "No webcam detected");
                    return;
                }
                webcam.open();
            }
            running = true;
            // Allow user to attempt capture in face-only mode
            captureBtn.setEnabled(true);
            attemptCount = 0; // Reset attempts when starting camera
            Thread t = new Thread(this::cameraLoop, "camera-loop");
            t.setDaemon(true);
            t.start();
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Position your face for detection (Attempts: 0/" + MAX_ATTEMPTS + ")");
                statusLabel.setForeground(Color.BLUE);
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to start camera: " + ex.getMessage());
        }
    }

    private void cameraLoop() {
        while (running) {
            try {
                BufferedImage img = null;
                if (videoCapture != null) {
                    Mat frame = new Mat();
                    if (videoCapture.read(frame) && !frame.empty()) {
                        img = CameraUtil.matToBufferedImage(frame);
                    }
                } else if (webcam != null && webcam.isOpen()) {
                    img = webcam.getImage();
                }

                if (img != null) {
                    Image scaled = img.getScaledInstance(640, 480, Image.SCALE_SMOOTH);
                    BufferedImage finalImg = img;
                    
                    SwingUtilities.invokeLater(() -> cameraLabel.setIcon(new ImageIcon(scaled)));

                    // Real-time face detection
                    new Thread(() -> {
                        try {
                            // Save temporary image for face detection
                            Path tempDir = Path.of("captured");
                            Files.createDirectories(tempDir);
                            Path tempFile = Files.createTempFile(tempDir, "temp-", ".jpg");
                            ImageIO.write(finalImg, "jpg", tempFile.toFile());
                            
                            boolean detected = faceService.detectFace(tempFile);
                            faceDetected = detected;
                            
                            SwingUtilities.invokeLater(() -> {
                                if (detected) {
                                    statusLabel.setText("✓ Face detected - Ready to capture! (Attempts: " + attemptCount + "/" + MAX_ATTEMPTS + ")");
                                    statusLabel.setForeground(Color.GREEN);
                                    captureBtn.setEnabled(true);
                                } else {
                                    statusLabel.setText("⚠ No face detected - Position yourself properly (Attempts: " + attemptCount + "/" + MAX_ATTEMPTS + ")");
                                    statusLabel.setForeground(Color.RED);
                                    captureBtn.setEnabled(false);
                                }
                            });
                            
                            // Clean up temp file
                            try { Files.deleteIfExists(tempFile); } catch (Exception ignore) {}
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Error in face detection");
                                statusLabel.setForeground(Color.RED);
                            });
                        }
                    }).start();
                }
                Thread.sleep(300); // Slower to reduce CPU usage
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void onCapture() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "No user logged in");
            return;
        }
        
        Subject selectedSubject = (Subject) subjectSelect.getSelectedItem();
        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject");
            return;
        }
        
        // Check if attendance already marked for today
        if (attendanceService.hasAttendanceToday(currentUser.getId(), selectedSubject.getId())) {
            JOptionPane.showMessageDialog(this, 
                "Attendance already marked for " + selectedSubject.getName() + " today.\n" +
                "Only one attendance per subject per day is allowed.",
                "Already Marked", JOptionPane.WARNING_MESSAGE);
            shutdownCamera();
            return;
        }
        
        if (!faceDetected) {
            JOptionPane.showMessageDialog(this, "No face detected! Please position your face properly before capturing.");
            return;
        }
        
        attemptCount++;
        captureBtn.setEnabled(false);
        
        try {
            BufferedImage img = null;
            if (videoCapture != null) {
                Mat frame = new Mat();
                if (videoCapture.read(frame) && !frame.empty()) {
                    img = CameraUtil.matToBufferedImage(frame);
                }
            } else if (webcam != null && webcam.isOpen()) {
                img = webcam.getImage();
            }
            if (img == null) {
                JOptionPane.showMessageDialog(this, "Failed to capture image");
                attemptCount--; // Don't count failed captures
                return;
            }
            
            Path capDir = Path.of("captured");
            Files.createDirectories(capDir);
            Path capFile = capDir.resolve("capture-" + System.currentTimeMillis() + ".jpg");
            ImageIO.write(img, "jpg", capFile.toFile());

            if (currentUser.getPhotoPath() == null || currentUser.getPhotoPath().isBlank()) {
                JOptionPane.showMessageDialog(this, "User has no registered photo. Please add one in Users tab.");
                attemptCount--; // Don't count this as an attempt
                return;
            }

            // Double-check face detection on captured image
            boolean faceDetectedInCapture = faceService.detectFace(capFile);
            if (!faceDetectedInCapture) {
                statusLabel.setText("No face detected in captured image (Attempt " + attemptCount + "/" + MAX_ATTEMPTS + ")");
                statusLabel.setForeground(Color.RED);
                
                if (attemptCount >= MAX_ATTEMPTS) {
                    // Mark as absent after 3 failed attempts
                    try {
                        attendanceService.markAbsentWithCheck(currentUser.getId(), selectedSubject.getId(), "CAMERA", capFile.toString());
                        statusLabel.setText("Maximum attempts reached. Marked ABSENT for " + currentUser.getUsername());
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(this, "No face detected after " + MAX_ATTEMPTS + " attempts. Marked ABSENT for " + currentUser.getUsername());
                        shutdownCamera();
                        return;
                    } catch (RuntimeException ex) {
                        statusLabel.setText("Already marked for today");
                        statusLabel.setForeground(Color.ORANGE);
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                        shutdownCamera();
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No face detected in captured image! Attempt " + attemptCount + " of " + MAX_ATTEMPTS + ". Try again.");
                    captureBtn.setEnabled(true);
                    return;
                }
            }
            
            // Verify face against registered photo
            boolean match = faceService.verifyFace(Path.of(currentUser.getPhotoPath()), capFile);
            if (match) {
                // SUCCESS - Mark present and stop camera
                try {
                    attendanceService.markPresentWithCheck(currentUser.getId(), selectedSubject.getId(), "CAMERA", capFile.toString());
                    statusLabel.setText("✓ Face verified! Marked PRESENT for " + currentUser.getUsername() + " in " + selectedSubject.getName());
                    statusLabel.setForeground(Color.GREEN);
                    JOptionPane.showMessageDialog(this, "Face verified successfully! Attendance marked PRESENT for " + currentUser.getUsername() + " in " + selectedSubject.getName());
                    shutdownCamera();
                    return;
                } catch (RuntimeException ex) {
                    statusLabel.setText("Already marked for today");
                    statusLabel.setForeground(Color.ORANGE);
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                    shutdownCamera();
                    return;
                }
            } else {
                // Face doesn't match
                statusLabel.setText("⚠ Face verification failed (Attempt " + attemptCount + "/" + MAX_ATTEMPTS + ")");
                statusLabel.setForeground(Color.ORANGE);
                
                if (attemptCount >= MAX_ATTEMPTS) {
                    // Mark as absent after 3 failed verification attempts
                    try {
                        attendanceService.markAbsentWithCheck(currentUser.getId(), selectedSubject.getId(), "CAMERA", capFile.toString());
                        statusLabel.setText("Face verification failed " + MAX_ATTEMPTS + " times. Marked ABSENT for " + currentUser.getUsername());
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(this, "Face verification failed after " + MAX_ATTEMPTS + " attempts. Marked ABSENT for " + currentUser.getUsername());
                        shutdownCamera();
                        return;
                    } catch (RuntimeException ex) {
                        statusLabel.setText("Already marked for today");
                        statusLabel.setForeground(Color.ORANGE);
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                        shutdownCamera();
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Face verification failed! This is attempt " + attemptCount + " of " + MAX_ATTEMPTS + ". Please try again.");
                    captureBtn.setEnabled(true);
                    return;
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error capturing image: " + ex.getMessage());
            attemptCount--; // Don't count technical failures as attempts
        } finally {
            // Re-enable capture button if camera is still running and we haven't exceeded attempts
            if (running && attemptCount < MAX_ATTEMPTS) {
                SwingUtilities.invokeLater(() -> {
                    if (faceDetected) {
                        captureBtn.setEnabled(true);
                    }
                });
            }
        }
    }

    private void shutdownCamera() {
        running = false;
        faceDetected = false;
        try { if (videoCapture != null) { videoCapture.release(); videoCapture = null; } } catch (Exception ignore) {}
        try { if (webcam != null && webcam.isOpen()) { webcam.close(); webcam = null; } } catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> {
            cameraLabel.setIcon(null);
            cameraLabel.setText("Camera not started");
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            captureBtn.setEnabled(false);
            if (attemptCount == 0) {
                statusLabel.setText("Camera stopped");
                statusLabel.setForeground(Color.BLACK);
            }
        });
    }

    
    private void loadSubjects() {
        try {
            subjectSelect.removeAllItems();
            List<Subject> subjects = subjectDao.listAll();
            for (Subject subject : subjects) {
                subjectSelect.addItem(subject);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + e.getMessage());
        }
    }
}
