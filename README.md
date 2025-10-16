# Attendance Management System

[![Java](https://img.shields.io/badge/Java-17%2B-007396)](https://adoptium.net/) [![Build](https://img.shields.io/badge/Build-Maven-ff69b4)](https://maven.apache.org/) ![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)

A clean and beginner‑friendly desktop app for taking and managing classroom attendance. Built with Java (Swing/JFrame), packaged with Maven, and ready for small teams or personal use.

---

## Table of Contents
- [Features](#features)
- [Demo / Screenshots](#demo--screenshots)
- [Getting Started](#getting-started)
  - [Windows (easy)](#windows-easy)
  - [Any OS (terminal)](#any-os-terminal)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Build and Run Scripts](#build-and-run-scripts)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features
- Fast attendance marking
- Student and subject management
- QR code check‑in/out
- Optional face detection (OpenCV)
- Simple authentication
- Export basic reports

## Demo / Screenshots
> Add your screenshots to `docs/images/` and update the paths below.

![Login](docs/images/login.png)
![Dashboard](docs/images/dashboard.png)

---

## Getting Started

### Windows (easy)
- Double‑click `run-app.bat` to build and start the app
- Already built? Use `run-standalone.bat`

Default admin (first run):
- Username: `Husnu_007`
- Password: `MSD007`

### Any OS (terminal)
```bash
mvn -q -DskipTests clean package
java -jar target/attendance-app.jar
```

---

## Configuration
- Copy: `src/main/resources/config.example.properties` → `src/main/resources/config.properties`
- Using MySQL? Update DB URL, username, password there
- Not using MySQL? Dev mode can fall back to an embedded H2 database
- Database schema (MySQL): `src/db/schema.sql`

OpenCV and webcam tips:
- Close other apps that might be using the camera
- Ensure your webcam drivers are installed and accessible

---

## Project Structure
```
attendance-management-system/
├─ pom.xml                     # Maven build configuration
├─ src/
│  ├─ main/
│  │  ├─ java/                # Application source code
│  │  └─ resources/           # Config + resources
│  └─ db/
│     └─ schema.sql           # Database schema (MySQL)
├─ run-app.bat                # Build and run (Windows)
└─ run-standalone.bat         # Run built JAR (Windows)
```

---

## Build and Run Scripts
- `run-app.bat` — builds (with Maven) then launches the app
- `run-standalone.bat` — runs `target/attendance-app.jar` if it already exists

Equivalent commands:
```bash
mvn -q -DskipTests clean package
java -jar target/attendance-app.jar
```

---

## Troubleshooting
- Build fails? Check:
  - `java -version` → must be 17+
  - `mvn -version` works and is on PATH
- Webcam doesn’t start? Close other camera apps, try a different USB port
- MySQL not connecting? Verify credentials and that the DB is running

---

## Roadmap
- Better reporting and export options
- Attendance analytics (daily/weekly summaries)
- Improved face detection workflow and accuracy
- Dark/light theme toggle

---

## Contributing
Contributions are welcome. To propose a change:
1. Fork the repo
2. Create a feature branch: `git checkout -b feature/my-change`
3. Commit your changes: `git commit -m "Describe change"`
4. Push and open a Pull Request

---

## License
Consider adding a `LICENSE` file (MIT is a simple and popular choice) if you plan to share/redistribute this project.