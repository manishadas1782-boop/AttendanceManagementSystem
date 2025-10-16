package com.ams.ui;

import com.ams.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ProfilePanel extends JPanel {
    private final User user;
    private final JLabel usernameLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel roleLabel = new JLabel();
    private final JLabel regNumberLabel = new JLabel();
    private final JLabel createdAtLabel = new JLabel();
    private final JLabel photoPreview = new JLabel("No photo", SwingConstants.CENTER);
    
    // Colors for the theme
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color SECONDARY_COLOR = new Color(92, 107, 192);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);

    public ProfilePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // Create main content panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Header with gradient background
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center content panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Photo panel with better styling
        JPanel photoPanel = createPhotoPanel();
        centerPanel.add(photoPanel, BorderLayout.WEST);

        // Info panel with card-like design
        JPanel infoPanel = createInfoPanel();
        centerPanel.add(infoPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer with action buttons
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        load();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createPhotoPanel() {
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(BACKGROUND_COLOR);
        photoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        
        // Photo container with shadow effect
        JPanel photoContainer = new JPanel(new BorderLayout());
        photoContainer.setBackground(Color.WHITE);
        photoContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        photoPreview.setPreferredSize(new Dimension(200, 200));
        photoPreview.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 2));
        photoPreview.setBackground(Color.WHITE);
        photoPreview.setOpaque(true);
        
        photoContainer.add(photoPreview, BorderLayout.CENTER);
        photoPanel.add(photoContainer, BorderLayout.NORTH);
        
        return photoPanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Title
        JLabel infoTitle = new JLabel("Personal Information");
        infoTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        infoTitle.setForeground(PRIMARY_COLOR);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createVerticalStrut(20));
        
        // Info fields
        addInfoField(infoPanel, "Username:", usernameLabel);
        addInfoField(infoPanel, "Registration Number:", regNumberLabel);
        addInfoField(infoPanel, "Official Email:", emailLabel);
        addInfoField(infoPanel, "Role:", roleLabel);
        addInfoField(infoPanel, "Member Since:", createdAtLabel);
        
        return infoPanel;
    }
    
    private void addInfoField(JPanel parent, String label, JLabel valueLabel) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        labelComp.setForeground(TEXT_COLOR);
        labelComp.setPreferredSize(new Dimension(150, 20));
        
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        valueLabel.setForeground(new Color(100, 100, 100));
        
        fieldPanel.add(labelComp, BorderLayout.WEST);
        fieldPanel.add(valueLabel, BorderLayout.CENTER);
        
        parent.add(fieldPanel);
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton refreshBtn = new JButton("Refresh Profile");
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> load());
        
        // Hover effect
        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshBtn.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshBtn.setBackground(PRIMARY_COLOR);
            }
        });
        
        footerPanel.add(refreshBtn);
        return footerPanel;
    }

    private void load() {
        usernameLabel.setText(user != null ? user.getUsername() : "N/A");
        regNumberLabel.setText(user != null ? (user.getRegistrationNumber() != null ? user.getRegistrationNumber() : "N/A") : "N/A");
        emailLabel.setText(user != null ? (user.getOfficialEmail() != null ? user.getOfficialEmail() : "N/A") : "N/A");
        roleLabel.setText(user != null ? user.getRole() : "N/A");
        
        // Format creation date
        if (user != null && user.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            createdAtLabel.setText(formatter.format(user.getCreatedAt().atZone(java.time.ZoneId.systemDefault())));
        } else {
            createdAtLabel.setText("N/A");
        }
        
        // Load photo with improved styling
        photoPreview.setIcon(null);
        photoPreview.setText("<html><center><div style='color: #888;'>No photo<br>available</div></center></html>");
        
        if (user != null && user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            try {
                File f = new File(user.getPhotoPath());
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                        photoPreview.setIcon(new ImageIcon(scaled));
                        photoPreview.setText(null);
                    }
                }
            } catch (IOException ignored) {}
        }
    }
}