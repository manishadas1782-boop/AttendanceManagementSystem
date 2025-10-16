package com.ams.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void init() {
        try (Connection c = Db.getConnection(); Statement st = c.createStatement()) {
            // Users
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(100) NOT NULL UNIQUE," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "role VARCHAR(50) NOT NULL DEFAULT 'USER'," +
                    "photo_path VARCHAR(255)," +
                    "official_email VARCHAR(255)," +
                    "registration_number VARCHAR(50)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL) ");
            try { st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS official_email VARCHAR(255)"); } catch (SQLException ignore) {}
            try { st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS registration_number VARCHAR(50)"); } catch (SQLException ignore) {}

            // Subjects
            st.executeUpdate("CREATE TABLE IF NOT EXISTS subjects (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "code VARCHAR(50) NOT NULL UNIQUE," +
                    "description TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");

            // QR codes
            st.executeUpdate("CREATE TABLE IF NOT EXISTS qr_codes (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id BIGINT NOT NULL," +
                    "qr_data VARCHAR(255) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");

            // Attendance
            st.executeUpdate("CREATE TABLE IF NOT EXISTS attendance_records (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id BIGINT NOT NULL," +
                    "subject_id BIGINT," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'PRESENT'," +
                    "marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "source VARCHAR(20) NOT NULL DEFAULT 'QR')");
            try { st.executeUpdate("ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS subject_id BIGINT"); } catch (SQLException ignore) {}
            try { st.executeUpdate("ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS check_in_photo_path TEXT"); } catch (SQLException ignore) {}
        } catch (SQLException e) {
            // Let callers handle UI errors; for now, print stack trace for dev visibility
            e.printStackTrace();
        }
    }
}