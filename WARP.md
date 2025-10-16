# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Project quick facts
- Language/Tooling: Java 17, Maven, JUnit 5
- UI: Swing/JFrame
- DB: MySQL (JDBC), with H2 fallback for local dev
- Vision/Camera: OpenCV (bytedeco) and webcam-capture
- Entry points: com.ams.App (Swing UI), com.ams.app.Main (CLI demo)

Setup for local development
1) Configuration
- Copy src/main/resources/config.example.properties to src/main/resources/config.properties and set DB credentials.

2) Initialize database schema (MySQL)
- Apply db/schema.sql to your MySQL instance. Use env vars for credentials.
- PowerShell example:
  - Set environment variables (do not echo secrets):
    $env:MYSQL_HOST="localhost"; $env:MYSQL_PORT="3306"; $env:MYSQL_USER="{{MYSQL_USER}}"; $env:MYSQL_PWD="{{MYSQL_PASSWORD}}"
  - Apply schema:
    mysql -u $env:MYSQL_USER -h $env:MYSQL_HOST -P $env:MYSQL_PORT < db/schema.sql

Build, run, and test (Maven)
- Clean build/package (quiet):
  mvn -q clean package

- Run the Swing UI app (exec plugin configured):
  mvn -q exec:java

- Run the CLI demo (override main class):
  mvn -q exec:java -Dexec.mainClass=com.ams.app.Main

- Run all tests:
  mvn -q test

- Run a single test class:
  mvn -q -Dtest=com.ams.AppTest test

- Run a single test method:
  mvn -q -Dtest=com.ams.AppTest#sanity test

Notes:
- QR features have been removed; the system uses face-only verification during Check-in.
- A shaded/fat JAR is not configured. Use exec:java to run, or add maven-shade-plugin if needed.

High-level architecture
- App bootstrap (com.ams.App)
  - Ensures DB schema via DatabaseInitializer, then starts the Swing UI (LoginFrame -> DashboardFrame).

- Configuration (com.ams.config.Config)
  - Loads config.properties (or falls back to config.example.properties). Used by Db for connection settings.

- Database access (com.ams.db)
  - Db: connection factory. Attempts MySQL using Config; if it fails, falls back to embedded H2 for local development.
  - DatabaseInitializer: creates tables (users, qr_codes, attendance_records) if missing at startup.
  - Database: legacy/simple connection helper that directly loads properties; some older code paths may reference it, but new data access goes through Db.

- Data access objects (com.ams.dao)
  - UserDao, QrDao, AttendanceDao: thin JDBC DAOs that map rows to com.ams.model.* types and encapsulate CRUD/queries.

- Services (com.ams.service)
  - AuthService: registers users with BCrypt-hashed passwords and authenticates credentials against DB.
  - AttendanceService: records attendance status via AttendanceDao.
  - FaceService: ORB-based image matching with OpenCV (via bytedeco) for simple face verification.

- UI layer (com.ams.ui)
  - LoginFrame: DB-backed login via AuthService; includes a Register button.
- RegistrationDialog: collects username/password/role (+ optional photo), supports taking a photo via camera, persists via AuthService and updates photo_path.
- DashboardFrame: main window with tabs:
  - UsersPanel: create/update/delete users; photo can be chosen from disk or captured instantly via camera; persists via UserDao. Note: legacy add flow uses a dummy password hash.
  - CheckInPanel: camera preview via OpenCV or webcam-capture; captures snapshot; performs face-only verification; marks attendance.
  - AttendancePanel: table view of recent attendance records.

- CLI demo (com.ams.app.*)
  - In-memory demo of login/register and attendance marking for a single student. Useful for quick console tests (run main: com.ams.app.Main).

Testing
- JUnit 5 via maven-surefire-plugin (useModulePath=false). Tests live under src/test/java (e.g., com.ams.AppTest#sanity).

What’s not configured (by design)
- Lint/format plugins (e.g., Checkstyle/SpotBugs/Formatter) are not present.
- Shaded/fat JAR packaging is not present.
