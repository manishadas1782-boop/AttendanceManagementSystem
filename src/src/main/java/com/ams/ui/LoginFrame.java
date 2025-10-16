package com.ams.ui;

import com.ams.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("Attendance System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 260);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::onLogin);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(this::onRegister);

        JPanel actions = new JPanel();
        actions.add(loginBtn);
        actions.add(registerBtn);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        var user = authService.authenticate(username, password);
        if (user != null) {
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister(ActionEvent e) {
        RegistrationDialog dialog = new RegistrationDialog(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            usernameField.setText(dialog.getCreatedUsername());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
}
