package com.ams.tools;

import com.ams.dao.UserDao;
import com.ams.db.DatabaseInitializer;
import com.ams.db.Db;
import com.ams.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;

public class AdminMaintenance {
    public static void main(String[] args) {
        DatabaseInitializer.init();
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            long adminId = ensureAdmin(c, "Husnu_007", "MSD007", "husnaienhusnu@gmail.com");

            try (Statement st = c.createStatement()) {
                st.executeUpdate("DELETE FROM attendance_records");
                st.executeUpdate("DELETE FROM qr_codes");
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id<>?")) {
                    ps.setLong(1, adminId);
                    ps.executeUpdate();
                }
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Maintenance failed", e);
        }

        // Clean filesystem artifacts
        deleteRecursively(Path.of("uploads"));
        deleteRecursively(Path.of("captured"));
        deleteRecursively(Path.of("qrs"));

        System.out.println("Admin updated and all data cleared (except admin).");
    }

    private static long ensureAdmin(Connection c, String username, String rawPassword, String email) throws SQLException {
        UserDao dao = new UserDao();
        User u;
        try {
            u = dao.findByUsername(username);
        } catch (SQLException e) {
            throw e;
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        if (u == null) {
            User created = dao.insert(username, hash, "ADMIN", null, email, "ADMIN001");
            return created != null ? created.getId() : findAdminIdFallback(dao, username);
        } else {
            dao.update(u.getId(), username, "ADMIN", u.getPhotoPath(), email, u.getRegistrationNumber());
            try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
                ps.setString(1, hash);
                ps.setLong(2, u.getId());
                ps.executeUpdate();
            }
            return u.getId();
        }
    }

    private static long findAdminIdFallback(UserDao dao, String username) throws SQLException {
        User u = dao.findByUsername(username);
        if (u == null) throw new SQLException("Failed to create admin user");
        return u.getId();
    }

    private static void deleteRecursively(Path path) {
        try {
            if (!Files.exists(path)) return;
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}
