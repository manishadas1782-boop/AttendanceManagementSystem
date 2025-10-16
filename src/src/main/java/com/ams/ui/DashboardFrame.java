package com.ams.ui;

import javax.swing.*;
import java.awt.*;

import com.ams.model.User;

public class DashboardFrame extends JFrame {
    public DashboardFrame(User user) {
        super("Attendance System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        JLabel welcome = new JLabel("Welcome, " + (user == null || user.getUsername() == null || user.getUsername().isBlank() ? "User" : user.getUsername()) + "!", SwingConstants.CENTER);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 18f));
        add(welcome, BorderLayout.NORTH);

        boolean isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", new ProfilePanel(user));
        if (isAdmin) {
            tabs.addTab("Users", new UsersPanel());
            tabs.addTab("Subjects", new SubjectsPanel());
        }
        tabs.addTab("Check-in", new CheckInPanel(user));
        tabs.addTab("Attendance", new AttendancePanel(user, isAdmin));
        add(tabs, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setLocationRelativeTo(null);
                login.setVisible(true);
            });
            dispose();
        });
        actions.add(logout);
        add(actions, BorderLayout.SOUTH);
    }
}
