package com.ams.ui;

import javax.swing.*;
import java.awt.*;

/**
 * QR features have been removed. This panel is intentionally minimal.
 */
public class QrPanel extends JPanel {
    public QrPanel() {
        setLayout(new BorderLayout());
        JLabel disabled = new JLabel("QR features have been removed", SwingConstants.CENTER);
        add(disabled, BorderLayout.CENTER);
    }
}
