package com.ams.ui;

import com.ams.util.CameraUtil;
import com.ams.service.FaceService;
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

public class CameraCaptureDialog extends JDialog {
    private final JLabel preview = new JLabel("Camera not started", SwingConstants.CENTER);
    private final JButton startBtn = new JButton("Start Camera");
    private final JButton captureBtn = new JButton("Capture");
    private final JComboBox<String> cameraSelect = new JComboBox<>(new String[]{"Auto (0)", "Camera 0", "Camera 1", "Camera 2", "Camera 3"});
    private final JLabel statusLabel = new JLabel("Position your face properly for detection", SwingConstants.CENTER);
    private final FaceService faceService = new FaceService();

    private volatile boolean running = false;
    private VideoCapture videoCapture;
    private Webcam webcam;
    private volatile boolean faceDetected = false;

    private boolean success = false;
    private Path capturedFile;

    public CameraCaptureDialog(Window owner) {
        super(owner, "Capture Photo", ModalityType.APPLICATION_MODAL);
        setSize(720, 620);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Camera:"));
        top.add(cameraSelect);
        startBtn.addActionListener(e -> startCamera());
        top.add(startBtn);
        captureBtn.setEnabled(false);
        captureBtn.addActionListener(e -> onCapture());
        top.add(captureBtn);
        add(top, BorderLayout.NORTH);

        preview.setPreferredSize(new Dimension(640, 480));
        preview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(preview, BorderLayout.CENTER);
        
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));
        statusLabel.setForeground(Color.BLUE);
        add(statusLabel, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { shutdownCamera(); }
        });
    }

    private void startCamera() {
        if (running) return;
        try {
            int idx = switch (cameraSelect.getSelectedIndex()) {
                case 1 -> 0; // Camera 0
                case 2 -> 1; // Camera 1
                case 3 -> 2; // Camera 2
                case 4 -> 3; // Camera 3
                default -> 0; // Auto -> 0
            };
            videoCapture = new VideoCapture(idx);
            if (!videoCapture.isOpened()) {
                // fallback to webcam-capture
                videoCapture = null;
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    JOptionPane.showMessageDialog(this, "No webcam detected");
                    return;
                }
                webcam.open();
            }
            running = true;
            captureBtn.setEnabled(true);
            Thread t = new Thread(this::cameraLoop, "camera-capture-loop");
            t.setDaemon(true);
            t.start();
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
                    
                    // Check for face detection in real-time
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
                                    statusLabel.setText("✓ Face detected - Ready to capture!");
                                    statusLabel.setForeground(Color.GREEN);
                                    captureBtn.setEnabled(true);
                                } else {
                                    statusLabel.setText("⚠ No face detected - Position yourself properly");
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
                    
                    SwingUtilities.invokeLater(() -> preview.setIcon(new ImageIcon(scaled)));
                }
                Thread.sleep(300); // Slightly slower to reduce CPU usage
            } catch (InterruptedException ie) {
                break;
            }
        }
    }

    private void onCapture() {
        if (!faceDetected) {
            JOptionPane.showMessageDialog(this, "No face detected! Please position your face properly before capturing.");
            return;
        }
        
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
                return;
            }
            
            Path dir = Path.of("captured");
            Files.createDirectories(dir);
            Path out = Files.createTempFile(dir, "capture-", ".jpg");
            ImageIO.write(img, "jpg", out.toFile());
            
            // Double-check face detection on captured image
            boolean finalFaceCheck = faceService.detectFace(out);
            if (!finalFaceCheck) {
                Files.deleteIfExists(out);
                JOptionPane.showMessageDialog(this, "No face detected in captured image! Please try again.");
                return;
            }
            
            this.capturedFile = out;
            this.success = true;
            shutdownCamera();
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error capturing image: " + ex.getMessage());
        }
    }

    private void shutdownCamera() {
        running = false;
        try { if (videoCapture != null) { videoCapture.release(); videoCapture = null; } } catch (Exception ignore) {}
        try { if (webcam != null && webcam.isOpen()) { webcam.close(); webcam = null; } } catch (Exception ignore) {}
    }

    public boolean isSuccess() { return success; }
    public Path getCapturedFile() { return capturedFile; }
}
