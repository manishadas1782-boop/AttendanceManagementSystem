@echo off
setlocal
set JAR=target\attendance-app.jar

if not exist "%JAR%" (
  echo Building app, please wait...
  "C:\Users\mulla\tools\apache-maven-3.9.9\bin\mvn.cmd" -q -DskipTests clean package
  if errorlevel 1 (
    echo Build failed. Ensure Java 17+ is installed.
    pause
    exit /b 1
  )
)

echo Launching Attendance System...
java -jar "%JAR%"
endlocal
