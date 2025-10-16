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
        
        statusLabel.setText("Starting camera...");
        statusLabel.setForeground(Color.BLUE);
        
        try {
            int idx = switch (cameraSelect.getSelectedIndex()) {
                case 1 -> 0; // Camera 0
                case 2 -> 1; // Camera 1
                case 3 -> 2; // Camera 2
                case 4 -> 3; // Camera 3
                default -> 0; // Auto -> 0
            };
            
            // For Windows, prefer webcam-capture library over OpenCV due to better camera compatibility
            String osName = System.getProperty("os.name").toLowerCase();
            boolean isWindows = osName.contains("windows");
            
            if (!isWindows) {
                // Try OpenCV VideoCapture first on non-Windows systems
                System.out.println("Attempting to open camera with OpenCV, index: " + idx);
                videoCapture = new VideoCapture(idx);
                
                // Set some common camera properties
                if (videoCapture.isOpened()) {
                    videoCapture.set(3, 640); // CAP_PROP_FRAME_WIDTH
                    videoCapture.set(4, 480); // CAP_PROP_FRAME_HEIGHT
                    videoCapture.set(5, 30);  // CAP_PROP_FPS
                    System.out.println("OpenCV camera opened successfully");
                } else {
                    System.out.println("OpenCV VideoCapture failed, trying webcam-capture library");
                    videoCapture.release();
                    videoCapture = null;
                }
            } else {
                System.out.println("Windows detected - using webcam-capture library for better compatibility");
                videoCapture = null;
            }
            
            if (videoCapture == null) {
                // Use webcam-capture library
                System.out.println("Initializing webcam-capture library...");
                java.util.List<Webcam> webcams = Webcam.getWebcams();
                
                System.out.println("Found " + webcams.size() + " webcam(s)");
                for (int i = 0; i < webcams.size(); i++) {
                    System.out.println("  Webcam " + i + ": " + webcams.get(i).getName());
                }
                
                if (webcams.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No webcam detected. Please check that your camera is connected and not being used by another application.");
                    return;
                }
                
                // Use the default webcam or the one at the selected index
                if (idx < webcams.size()) {
                    webcam = webcams.get(idx);
                } else {
                    webcam = Webcam.getDefault();
                }
                
                if (webcam == null) {
                    JOptionPane.showMessageDialog(this, "No webcam detected");
                    return;
                }
                
                // Set webcam resolution
                java.awt.Dimension[] sizes = webcam.getViewSizes();
                if (sizes.length > 0) {
                    // Find a good resolution (640x480 if available, otherwise use largest)
                    java.awt.Dimension targetSize = new java.awt.Dimension(640, 480);
                    java.awt.Dimension selectedSize = sizes[0];
                    
                    for (java.awt.Dimension size : sizes) {
                        if (size.equals(targetSize)) {
                            selectedSize = size;
                            break;
                        }
                        if (size.width <= 800 && size.width > selectedSize.width) {
                            selectedSize = size;
                        }
                    }
                    webcam.setViewSize(selectedSize);
                }
                
                System.out.println("Opening webcam: " + webcam.getName());
                webcam.open();
                
                if (!webcam.isOpen()) {
                    JOptionPane.showMessageDialog(this, "Failed to open camera. Please check that your camera is not being used by another application.");
                    return;
                }
                
                System.out.println("Webcam opened successfully");
            }
            
            running = true;
            startBtn.setEnabled(false);
            startBtn.setText("Camera Running");
            
            Thread t = new Thread(this::cameraLoop, "camera-capture-loop");
            t.setDaemon(true);
            t.start();
            
            statusLabel.setText("Camera started - Position your face for detection");
            statusLabel.setForeground(Color.BLUE);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to start camera: " + ex.getMessage() + "\n\nPlease ensure:\n1. Your camera is connected\n2. No other applications are using the camera\n3. Camera permissions are granted");
            statusLabel.setText("Camera failed to start");
            statusLabel.setForeground(Color.RED);
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
        
        try { 
            if (videoCapture != null) { 
                videoCapture.release(); 
                videoCapture = null;
                System.out.println("OpenCV VideoCapture released");
            } 
        } catch (Exception ignore) {}
        
        try { 
            if (webcam != null && webcam.isOpen()) { 
                webcam.close(); 
                webcam = null;
                System.out.println("Webcam closed");
            } 
        } catch (Exception ignore) {}
        
        SwingUtilities.invokeLater(() -> {
            startBtn.setEnabled(true);
            startBtn.setText("Start Camera");
            captureBtn.setEnabled(false);
            preview.setIcon(null);
            preview.setText("Camera not started");
            statusLabel.setText("Position your face properly for detection");
            statusLabel.setForeground(Color.BLUE);
        });
    }

    public boolean isSuccess() { return success; }
    public Path getCapturedFile() { return capturedFile; }
}
