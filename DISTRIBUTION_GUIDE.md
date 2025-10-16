# Attendance Management System - Distribution Guide

## Requirements on Target Device

### 1. **Java Development Kit (JDK) 17 or higher**
- Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
- Verify installation: `java -version` and `javac -version`

### 2. **Apache Maven 3.6+ (Optional but Recommended)**
- Download from: https://maven.apache.org/download.cgi
- Or use the included Maven in `apache-maven-3.9.9` folder

## Distribution Options

### Option 1: Complete Source Distribution (Recommended)
**What to include in ZIP:**
- `src/` folder (complete source code)
- `pom.xml` (Maven configuration)
- `README.md` and documentation files
- `run-app.bat` (Windows run script)
- `apache-maven-3.9.9/` (included Maven - optional)

**Exclude from ZIP:**
- `target/` folder (compiled files)
- `devdb.mv.db` and `devdb.trace.db` (database files)
- `captured/` and `uploads/` folders (user data)
- `.class` files

**To run on target device:**
1. Extract ZIP file
2. Open command prompt/terminal in project folder
3. Run: `mvn clean compile exec:java`

### Option 2: Standalone Executable JAR
**To create:**
1. Run: `mvn clean package`
2. Share `target/attendance-app.jar` file

**To run on target device:**
1. Ensure Java 17+ is installed
2. Run: `java -jar attendance-app.jar`

### Option 3: Complete Development Environment
Share the entire current folder as ZIP, including:
- All source files
- Maven installation
- Database files (if you want to preserve data)

## Quick Setup for Recipients

### Windows Users:
1. Install Java 17+
2. Extract ZIP file
3. Double-click `run-app.bat` (if included)
4. Or run `mvn exec:java` in command prompt

### Default Admin Login:
- Username: `Husnu_007`
- Password: `MSD007`

## Network Considerations
- Application uses local H2 database by default
- For MySQL: Recipients need to configure `src/main/resources/config.properties`
- Database schema is auto-created on first run

## Troubleshooting
- If Maven not found: Use included `apache-maven-3.9.9/bin/mvn`
- If Java not found: Install JDK 17+ and set JAVA_HOME
- Database issues: Delete `devdb.*` files to reset database