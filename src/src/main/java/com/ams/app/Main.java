package com.ams.app;

import com.ams.app.model.Student;
import com.ams.app.service.AttendanceService;
import com.ams.app.service.AuthService;
import com.ams.app.service.ValidationService;
import com.ams.app.service.VerificationService;
import com.ams.app.storage.InMemoryStore;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Set;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InMemoryStore store = new InMemoryStore();
        ValidationService validator = new ValidationService();
        VerificationService verifier = new VerificationService();
        AuthService auth = new AuthService(validator, verifier, store);
        AttendanceService attendanceService = new AttendanceService(store);

        System.out.println("=== Attendance Management System (Java) ===");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Registration Number: ");
        String reg = scanner.nextLine().trim();

        System.out.print("Enter Official Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Mobile Number: ");
        String mobile = scanner.nextLine().trim();

        Student current;
        try {
            current = auth.loginOrRegister(name, reg, email, mobile, scanner);
        } catch (IllegalArgumentException ex) {
            System.out.println("Login failed: " + ex.getMessage());
            return;
        }

        System.out.println("\nWelcome, " + current.getName() + " (" + current.getRegNumber() + ")");

        boolean done = false;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!done) {
            System.out.println("\nMenu:");
            System.out.println("1) Mark today's attendance");
            System.out.println("2) View my attendance dates");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    boolean marked = attendanceService.markToday(current);
                    if (marked) {
                        System.out.println("Attendance marked for " + LocalDate.now().format(fmt));
                    } else {
                        System.out.println("Already marked for today.");
                    }
                    break;
                case "2":
                    Set<LocalDate> dates = attendanceService.getStudentAttendance(current);
                    if (dates.isEmpty()) {
                        System.out.println("No attendance records yet.");
                    } else {
                        System.out.println("Your attendance dates:");
                        dates.stream().sorted().forEach(d -> System.out.println("- " + d.format(fmt)));
                    }
                    break;
                case "0":
                    done = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        System.out.println("Goodbye!");
    }
}
