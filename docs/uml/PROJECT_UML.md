# System Architecture and UML

This document provides a high‑level view of the system using Mermaid diagrams (rendered on GitHub).

## 1) High‑level architecture
```mermaid
flowchart LR
  subgraph UI[UI Layer]
    DashboardFrame
    LoginFrame
    AttendancePanel
    SubjectsPanel
    UsersPanel
    QrPanel
  end

  subgraph Services[Service Layer]
    AttendanceServiceSvc[AttendanceService]
    AuthServiceSvc[AuthService]
    QRServiceSvc[QRService]
    FaceServiceSvc[FaceService]
    ExportServiceSvc[ExportService]
  end

  subgraph Data[Persistence]
    AttendanceDaoDao[AttendanceDao]
    UserDaoDao[UserDao]
    SubjectDaoDao[SubjectDao]
    QrDaoDao[QrDao]
    Database[(Database / Db)]
  end

  subgraph Domain[Domain Models]
    User
    Subject
    Student
    AttendanceRecord
    QrCodeRecord
  end

  Config[Config] --> Database
  UI --> Services
  Services --> Domain
  Services --> Data
  AttendanceDaoDao --> Database
  UserDaoDao --> Database
  SubjectDaoDao --> Database
  QrDaoDao --> Database
```

## 2) Core domain model (class diagram)
```mermaid
classDiagram
  direction LR
  class User {
    +Long id
    +String username
    +String role
  }
  class Student {
    +Long id
    +String name
    +String rollNo
  }
  class Subject {
    +Long id
    +String code
    +String title
  }
  class AttendanceRecord {
    +Long id
    +Date date
    +String status
  }
  class QrCodeRecord {
    +Long id
    +String token
    +Date issuedAt
  }

  Student "*" --> "*" Subject : enrolls
  AttendanceRecord "*" --> "1" Student : for
  AttendanceRecord "*" --> "1" Subject : in
  QrCodeRecord "*" --> "1" User : issuedTo
```

## 3) Services and DAOs (dependencies)
```mermaid
classDiagram
  direction LR
  class AttendanceService {
    +markAttendance(...)
    +getRecords(...)
  }
  class AuthService {
    +login(...)
  }
  class QRService {
    +generateQr(...)
    +scanQr(...)
  }
  class FaceService {
    +detectFace(...)
  }
  class ExportService {
    +exportCsv(...)
  }

  class AttendanceDao
  class UserDao
  class SubjectDao
  class QrDao
  class Database
  class Config

  AttendanceService ..> AttendanceDao : uses
  AttendanceService ..> UserDao : uses
  AttendanceService ..> SubjectDao : uses
  AuthService ..> UserDao : uses
  QRService ..> QrDao : uses
  FaceService ..> "OpenCV (external)" : calls
  ExportService ..> AttendanceDao : reads

  AttendanceDao ..> Database : JDBC
  UserDao ..> Database : JDBC
  SubjectDao ..> Database : JDBC
  QrDao ..> Database : JDBC
  Config ..> Database : configures
```

## 4) UI to Services
```mermaid
classDiagram
  direction LR
  class DashboardFrame
  class LoginFrame
  class AttendancePanel
  class SubjectsPanel
  class UsersPanel
  class QrPanel

  class AttendanceService
  class AuthService
  class QRService

  LoginFrame ..> AuthService : authenticate
  AttendancePanel ..> AttendanceService : mark/view
  SubjectsPanel ..> AttendanceService : subject data
  UsersPanel ..> AuthService : manage users
  QrPanel ..> QRService : generate/scan
  DashboardFrame ..> AttendanceService : orchestrates
```

Notes:
- Diagrams are simplified and focus on key relationships from the current code structure.
- If you prefer PlantUML instead of Mermaid, I can add equivalent `.puml` files.
