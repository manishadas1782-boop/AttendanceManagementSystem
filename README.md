<div align="center">

# Attendance Management System

Make attendance painless. Simple. Fast. Visual.

[![Java](https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white)](https://adoptium.net/) [![Build](https://img.shields.io/badge/Build-Maven-FF69B4?logo=apache-maven&logoColor=white)](https://maven.apache.org/) ![Platform](https://img.shields.io/badge/Platform-Windows%20|%20Linux%20|%20macOS-lightgrey) [![PRs](https://img.shields.io/badge/PRs-welcome-success)](#contributing)

</div>

<p align="center">
  <a href="#features">Features</a> ·
  <a href="#getting-started">Install & Run</a> ·
  <a href="#configuration">Config</a> ·
  <a href="#demo--screenshots">Screenshots</a> ·
  <a href="#roadmap">Roadmap</a> ·
  <a href="#faq">FAQ</a>
</p>

---

## Why you'll love it
- 🚀 Ultra‑fast attendance marking (QR + optional face detection)
- 🧭 Clean, beginner‑friendly setup and scripts
- 🧑‍🎓 Students, Subjects, Users — all in one place
- 📦 Single JAR packaging (Maven Shade)
- 🧰 Works offline; MySQL optional (H2 fallback for dev)

---

## Demo / Screenshots
> Add your screenshots to `docs/images/` and update paths below.

<p align="center">
  <img src="docs/images/login.png" alt="Login" width="420"/>
  <img src="docs/images/dashboard.png" alt="Dashboard" width="420"/>
</p>

---

## Getting Started

### Windows (one‑click)
- Double‑click `run-app.bat` to build and start
- Already built? Use `run-standalone.bat`

Default admin (first run):
- Username: `Husnu_007`
- Password: `MSD007`

### Any OS (terminal)
```bash
mvn -q -DskipTests clean package
java -jar target/attendance-app.jar
```

<details>
<summary><strong>Advanced: build from source with Maven</strong></summary>

```bash
# Clean build
mvn clean package

# Run with Maven exec
mvn -q exec:java -Dexec.mainClass=com.ams.App
```
</details>

---

## Configuration
- Copy: `src/main/resources/config.example.properties` → `src/main/resources/config.properties`
- Using MySQL? Update DB URL, username, password
- MySQL schema: `src/db/schema.sql`

```properties
# src/main/resources/config.properties
# Database
db.url=jdbc:mysql://localhost:3306/attendance
db.user=your_user
db.password=your_password

# Optional: face/QR toggles
feature.qr=true
feature.face=true
```

Tips for camera/OpenCV:
- Close other camera apps; ensure drivers are installed
- Good lighting improves recognition

---

## Project Structure
```
attendance-management-system/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/                # App source code
│  │  └─ resources/           # Config + assets
│  └─ db/
│     └─ schema.sql           # MySQL schema
├─ run-app.bat                # Build & run (Windows)
└─ run-standalone.bat         # Run built JAR (Windows)
```

---

## Troubleshooting
- Build fails? Check `java -version` (17+) and `mvn -version`
- Webcam not working? Close other apps; try a different USB port
- DB errors? Verify credentials and DB is running

---

## Roadmap
- [ ] Better reports and CSV/XLSX exports
- [ ] Attendance analytics (daily/weekly)
- [ ] Theming (light/dark)
- [ ] Improved face detection workflow
- [ ] Installer for Windows (msi)

---

## Contributing
1. Fork the repo
2. `git checkout -b feature/my-change`
3. Commit: `git commit -m "Describe change"`
4. Push and open a Pull Request

> New to Git? Start with: `git add -A && git commit -m "msg" && git push`

---

## FAQ
**Q: Do I need MySQL to try it?**  
A: No — dev mode can use an embedded H2 database.

**Q: Camera doesn’t start?**  
A: Close other apps using the camera; check drivers.

**Q: Which Java version?**  
A: Java 17 or newer.

---

## License
Add a `LICENSE` file if you plan to share/redistribute (MIT is a simple, permissive choice).

<p align="center">
  ⭐ If this project helps you, consider starring the repo!
</p>