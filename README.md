<div align="center">

# Attendance Management System

Reliable, streamlined attendance tracking for classrooms and small teams.

[![Java](https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white)](https://adoptium.net/) [![Build](https://img.shields.io/badge/Build-Maven-FF69B4?logo=apache-maven&logoColor=white)](https://maven.apache.org/) ![Platform](https://img.shields.io/badge/Platform-Windows%20|%20Linux%20|%20macOS-lightgrey) [![PRs](https://img.shields.io/badge/PRs-welcome-success)](#contributing)

</div>

<p align="center">
  <a href="#overview">Overview</a> ·
  <a href="#features">Features</a> ·
  <a href="#requirements">Requirements</a> ·
  <a href="#installation">Installation</a> ·
  <a href="#run">Run</a> ·
  <a href="#configuration">Configuration</a> ·
  <a href="#project-structure">Project Structure</a> ·
  <a href="#troubleshooting">Troubleshooting</a> ·
  <a href="#roadmap">Roadmap</a> ·
  <a href="#contributing">Contributing</a>
</p>

---

## Overview
A desktop application built with Java Swing and packaged via Maven. It supports QR‑based check‑in, optional face detection, and exportable records.

## Features
- Fast attendance capture (QR, optional face detection)
- Student, subject, and user management
- Exportable attendance reports
- Offline‑friendly; MySQL optional (H2 fallback for development)
- Single‑JAR packaging with Maven Shade

## Requirements
- Java 17+
- Maven 3.8+
- MySQL 8+ (optional; used in production scenarios)

## Installation
```bash
mvn -q -DskipTests clean package
```

## Run
- Windows: run `run-app.bat` (builds and launches) or `run-standalone.bat` (launches existing JAR)
- Any OS:
```bash
java -jar target/attendance-app.jar
```

## Configuration
Copy the example configuration and adjust as needed:
- From: `src/main/resources/config.example.properties`
- To:   `src/main/resources/config.properties`

Key properties:
```properties
# Database
db.url=jdbc:mysql://localhost:3306/attendance
db.user=your_user
db.password=your_password

# Features
auth.enabled=true
feature.qr=true
feature.face=true
```

## Project Structure
```
attendance-management-system/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/                # Application source code
│  │  └─ resources/           # Config and assets
│  └─ db/
│     └─ schema.sql           # MySQL schema
├─ run-app.bat                # Build & run (Windows)
└─ run-standalone.bat         # Run built JAR (Windows)
```

## Troubleshooting
- Build: ensure `java -version` is 17+ and `mvn -version` is available
- Camera: close other camera apps; verify drivers
- Database: verify credentials and server availability

## Roadmap
- Advanced reporting and CSV/XLSX exports
- Analytics dashboards (daily/weekly)
- Theming (light/dark)
- Improved face detection workflow

## Contributing
Pull requests are welcome. Please open an issue to discuss significant changes.