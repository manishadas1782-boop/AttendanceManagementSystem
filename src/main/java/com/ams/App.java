package com.ams;

import javax.swing.SwingUtilities;
import com.ams.ui.LoginFrame;
import com.ams.db.DatabaseInitializer;
import com.ams.db.Db;
import com.ams.dao.UserDao;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        // Ensure schema exists (works for MySQL and H2 fallback)
        DatabaseInitializer.init();
        // Seed permanent admin if absent
        seedAdmin();
        // Seed default subjects
        seedDefaultSubjects();
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void seedAdmin() {
        try {
            UserDao dao = new UserDao();
            var existing = dao.findByUsername("Husnu_007");
            if (existing == null) {
                String hash = BCrypt.hashpw("MSD007", BCrypt.gensalt(12));
                dao.insert("Husnu_007", hash, "ADMIN", null, "husnaienhusnu@gmail.com", "ADMIN001");
            }
        } catch (SQLException e) {
            // Log and continue; app can still run
            e.printStackTrace();
        }
    }

    private static void seedDefaultSubjects() {
        try {
            com.ams.dao.SubjectDao dao = new com.ams.dao.SubjectDao();
            if (dao.listAll().isEmpty()) {
                dao.insert("Computer Science", "CS101", "Introduction to Computer Science");
                dao.insert("Mathematics", "MATH101", "Calculus I");
                dao.insert("Physics", "PHY101", "General Physics");
                dao.insert("English", "ENG101", "English Composition");
            }
        } catch (SQLException e) {
            // Log and continue; app can still run
            e.printStackTrace();
        }
    }
}
