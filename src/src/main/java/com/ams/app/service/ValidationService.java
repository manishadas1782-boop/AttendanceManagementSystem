package com.ams.app.service;

import java.util.regex.Pattern;

public class ValidationService {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern MOBILE = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern REG = Pattern.compile("^[A-Za-z0-9-_/]{3,}$");

    public boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s).matches();
    }

    public boolean isValidMobile(String s) {
        return s != null && MOBILE.matcher(s).matches();
    }

    public boolean isValidRegNumber(String s) {
        return s != null && REG.matcher(s).matches();
    }

    public boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
