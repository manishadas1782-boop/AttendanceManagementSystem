package com.ams.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static final String CONFIG_FILE = "/config.properties";
    private static final String CONFIG_EXAMPLE_FILE = "/config.example.properties";

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = Database.class.getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (IOException ignored) {}
        try (InputStream in = Database.class.getResourceAsStream(CONFIG_EXAMPLE_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {}
        return props;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = loadProperties();
        String url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC");
        String user = p.getProperty("db.user", "root");
        String pass = p.getProperty("db.password", "");
        // Optional: modern MySQL drivers auto-register
        // try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(url, user, pass);
    }
}
