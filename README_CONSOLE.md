# Attendance Management System (Java, Console)

Console-based Java application to manage student login and attendance with basic verification.

Features
- Login with Name, Registration Number, Official Email, and Mobile Number
- Input validation (email format, mobile length, reg number format)
- OTP-based verification (simulated send to email/SMS, 5-minute expiry)
- Mark today's attendance and view recorded attendance dates

Getting Started
1) Requirements
   - Java JDK 11+ in PATH (javac and java)
   - Windows PowerShell to run helper scripts

2) Build
   - Run: scripts\build.ps1

3) Run
   - Run: scripts\run.ps1

Project Structure
- src/main/java/com/ams/app/Main.java (entrypoint)
- src/main/java/com/ams/app/model/Student.java
- src/main/java/com/ams/app/service/* (Validation, Verification, Auth, Attendance)
- src/main/java/com/ams/app/storage/InMemoryStore.java
- scripts/build.ps1, scripts/run.ps1

Notes
- OTP delivery is simulated via console logs for demo purposes. Integrate with real email/SMS providers for production.
- Storage is in-memory; swap with a database or file persistence as needed.
