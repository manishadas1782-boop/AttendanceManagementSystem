#!/usr/bin/env powershell
param()
$ErrorActionPreference = "Stop"

Write-Host "Manual Build Script for Attendance Management System" -ForegroundColor Green

# Define paths
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $rootDir "src\src\main\java"
$resourcesDir = Join-Path $rootDir "src\src\main\resources"
$targetDir = Join-Path $rootDir "src\target"
$classesDir = Join-Path $targetDir "classes"
$libDir = Join-Path $rootDir "lib"
$jarName = "attendance-app-updated.jar"

Write-Host "Root directory: $rootDir" -ForegroundColor Yellow
Write-Host "Source directory: $srcDir" -ForegroundColor Yellow
Write-Host "Target directory: $targetDir" -ForegroundColor Yellow

# Create target directories
if (Test-Path $targetDir) {
    Remove-Item -Recurse -Force $targetDir
}
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

# Check if we need to download dependencies
if (-not (Test-Path $libDir)) {
    Write-Host "Creating lib directory for dependencies..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $libDir -Force | Out-Null
    
    Write-Host "Warning: This is a simplified build. Some dependencies may be missing." -ForegroundColor Red
    Write-Host "For a complete build, please install Maven and run: mvn clean package" -ForegroundColor Red
    
    # Try to copy existing JAR dependencies if they exist
    $existingJar = Join-Path $rootDir "src\target\attendance-app.jar"
    if (Test-Path $existingJar) {
        Write-Host "Extracting dependencies from existing JAR..." -ForegroundColor Yellow
        $tempExtract = Join-Path $targetDir "temp-extract"
        New-Item -ItemType Directory -Path $tempExtract -Force | Out-Null
        
        Set-Location $tempExtract
        & jar -xf $existingJar
        Set-Location $rootDir
        
        # Copy the dependencies to our classes directory
        Copy-Item -Path "$tempExtract\*" -Destination $classesDir -Recurse -Force
        Remove-Item -Recurse -Force $tempExtract
    }
}

# Find all Java source files
Write-Host "Finding Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $srcDir -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

if ($javaFiles.Count -eq 0) {
    Write-Error "No Java source files found in $srcDir"
    exit 1
}

Write-Host "Found $($javaFiles.Count) Java source files" -ForegroundColor Green

# Build classpath from existing JAR (extract dependencies)
$existingJar = Join-Path $rootDir "src\target\attendance-app.jar"
$classpath = $classesDir
if (Test-Path $existingJar) {
    $classpath += ";$existingJar"
}

Write-Host "Compiling Java sources..." -ForegroundColor Yellow
try {
    $compileArgs = @(
        "-d", $classesDir,
        "-cp", $classpath,
        "-encoding", "UTF-8"
    ) + $javaFiles
    
    & javac @compileArgs
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Compilation failed"
        exit 1
    }
    
    Write-Host "Compilation successful!" -ForegroundColor Green
} catch {
    Write-Error "Compilation failed: $_"
    exit 1
}

# Copy resources
if (Test-Path $resourcesDir) {
    Write-Host "Copying resources..." -ForegroundColor Yellow
    Copy-Item -Path "$resourcesDir\*" -Destination $classesDir -Recurse -Force
    Write-Host "Resources copied successfully!" -ForegroundColor Green
}

# Create manifest
$manifestContent = @"
Manifest-Version: 1.0
Main-Class: com.ams.App
Class-Path: .

"@

$manifestFile = Join-Path $targetDir "MANIFEST.MF"
$manifestContent | Out-File -FilePath $manifestFile -Encoding ASCII

# Create JAR file
Write-Host "Creating JAR file..." -ForegroundColor Yellow
$jarPath = Join-Path $targetDir $jarName

Set-Location $classesDir
try {
    & jar -cfm $jarPath $manifestFile *
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "JAR creation failed"
        exit 1
    }
    
    Write-Host "JAR file created successfully: $jarPath" -ForegroundColor Green
} finally {
    Set-Location $rootDir
}

# Test the JAR
Write-Host "Testing the new JAR..." -ForegroundColor Yellow
Write-Host "You can now run the updated application with:" -ForegroundColor Cyan
Write-Host "java -jar `"$jarPath`"" -ForegroundColor Cyan

Write-Host "Build completed successfully!" -ForegroundColor Green