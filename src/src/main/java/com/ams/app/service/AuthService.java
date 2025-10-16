package com.ams.app.service;

import com.ams.app.model.Student;
import com.ams.app.storage.InMemoryStore;

import java.util.Optional;
import java.util.Scanner;

public class AuthService {
    private final ValidationService validator;
    private final VerificationService verifier;
    private final InMemoryStore store;

    public AuthService(ValidationService validator, VerificationService verifier, InMemoryStore store) {
        this.validator = validator;
        this.verifier = verifier;
        this.store = store;
    }

    public Student loginOrRegister(String name, String reg, String email, String mobile, Scanner scanner) {
        if (!validator.isNonEmpty(name)) throw new IllegalArgumentException("Name is required");
        if (!validator.isValidRegNumber(reg)) throw new IllegalArgumentException("Invalid registration number");
        if (!validator.isValidEmail(email)) throw new IllegalArgumentException("Invalid official email");
        if (!validator.isValidMobile(mobile)) throw new IllegalArgumentException("Invalid mobile number (10-15 digits)");

        Optional<Student> existing = store.findStudent(reg);
        Student student = existing.orElseGet(() -> store.addStudent(new Student(name, reg, email, mobile)));

        // OTP Verification
        System.out.println("\nWe need to verify your identity with a one-time code.");
        verifier.sendOtp(email, mobile);
        System.out.print("Enter the 6-digit code sent to your email/SMS: ");
        String code = scanner.nextLine();
        if (!verifier.verify(code)) {
            throw new IllegalArgumentException("Verification failed. Invalid or expired code.");
        }

        return student;
    }
}
