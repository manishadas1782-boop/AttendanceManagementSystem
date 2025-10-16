package com.ams.db;

import com.ams.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    static {
        Config.load();
        // Load H2 database driver
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found: " + e.getMessage());
        }
        // Load MySQL driver if available
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // MySQL driver not available, will use H2 fallback
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = Config.get("db.url", "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        String user = Config.get("db.user", "attendance_user");
        String pass = Config.get("db.password", "change_me");
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            // Fallback to embedded H2 for local dev so the app can run without MySQL credentials
            String h2Url = "jdbc:h2:file:./devdb;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_UPPER=false";
            try {
                return DriverManager.getConnection(h2Url, "sa", "");
            } catch (SQLException h2e) {
                // If fallback also fails, bubble the original MySQL exception for clarity
                throw e;
            }
        }
    }
}
