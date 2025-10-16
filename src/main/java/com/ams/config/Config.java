package com.ams.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            } else {
                // fallback to example to avoid NPE in dev
                try (InputStream ex = Config.class.getClassLoader().getResourceAsStream("config.example.properties")) {
                    if (ex != null) PROPS.load(ex);
                }
            }
            loaded = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key, String def) {
        if (!loaded) load();
        return PROPS.getProperty(key, def);
    }
}
