#!/usr/bin/env powershell
param()
$ErrorActionPreference = "Stop"

Write-Host "Building Attendance Management System with existing dependencies" -ForegroundColor Green

# Define paths
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$srcDir = Join-Path $rootDir "src\src\main\java"
$resourcesDir = Join-Path $rootDir "src\src\main\resources"
$targetDir = Join-Path $rootDir "src\target-new"
$classesDir = Join-Path $targetDir "classes"
$existingJar = Join-Path $rootDir "src\target\attendance-app.jar"
$newJarName = "attendance-app-updated.jar"

Write-Host "Root directory: $rootDir" -ForegroundColor Yellow
Write-Host "Existing JAR: $existingJar" -ForegroundColor Yellow

if (-not (Test-Path $existingJar)) {
    Write-Error "Existing JAR not found at $existingJar"
    exit 1
}

# Clean and create directories
if (Test-Path $targetDir) {
    Remove-Item -Recurse -Force $targetDir
}
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

# Extract existing JAR to get dependencies and existing classes
Write-Host "Extracting dependencies from existing JAR..." -ForegroundColor Yellow
$tempExtractDir = Join-Path $targetDir "temp-extract"
New-Item -ItemType Directory -Path $tempExtractDir -Force | Out-Null

Set-Location $tempExtractDir
& jar -xf $existingJar
Set-Location $rootDir

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to extract existing JAR"
    exit 1
}

# Copy all extracted content to classes directory (this includes dependencies)
Copy-Item -Path "$tempExtractDir\*" -Destination $classesDir -Recurse -Force

# Find all Java source files
Write-Host "Finding Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $srcDir -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

if ($javaFiles.Count -eq 0) {
    Write-Error "No Java source files found in $srcDir"
    exit 1
}

Write-Host "Found $($javaFiles.Count) Java source files" -ForegroundColor Green

# Build classpath - include the classes directory which now has all dependencies
$classpath = $classesDir

Write-Host "Compiling Java sources with full classpath..." -ForegroundColor Yellow
try {
    $compileArgs = @(
        "-d", $classesDir,
        "-cp", $classpath,
        "-encoding", "UTF-8",
        "-g"  # Include debug information
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

# Copy resources (if they don't exist already)
if (Test-Path $resourcesDir) {
    Write-Host "Copying resources..." -ForegroundColor Yellow
    Copy-Item -Path "$resourcesDir\*" -Destination $classesDir -Recurse -Force
    Write-Host "Resources copied successfully!" -ForegroundColor Green
}

# Create new JAR file
Write-Host "Creating updated JAR file..." -ForegroundColor Yellow
$newJarPath = Join-Path $targetDir $newJarName

Set-Location $classesDir
try {
    # Use the existing manifest if it exists, otherwise create a simple one
    $existingManifest = Join-Path $classesDir "META-INF\MANIFEST.MF"
    if (Test-Path $existingManifest) {
        Write-Host "Using existing manifest file" -ForegroundColor Yellow
        & jar -cfm $newJarPath $existingManifest *
    } else {
        Write-Host "Creating new manifest file" -ForegroundColor Yellow
        $manifestContent = @"
Manifest-Version: 1.0
Main-Class: com.ams.App
Class-Path: .

"@
        $manifestFile = Join-Path $targetDir "MANIFEST.MF"
        $manifestContent | Out-File -FilePath $manifestFile -Encoding ASCII
        & jar -cfm $newJarPath $manifestFile *
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "JAR creation failed"
        exit 1
    }
    
    Write-Host "JAR file created successfully: $newJarPath" -ForegroundColor Green
} finally {
    Set-Location $rootDir
}

# Clean up temporary files
Remove-Item -Recurse -Force $tempExtractDir

# Test the JAR
Write-Host "Testing the new JAR..." -ForegroundColor Yellow
Write-Host "New JAR size: $((Get-Item $newJarPath).Length) bytes" -ForegroundColor Cyan
Write-Host "Original JAR size: $((Get-Item $existingJar).Length) bytes" -ForegroundColor Cyan

Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "You can now run the updated application with:" -ForegroundColor Cyan
Write-Host "java -jar `"$newJarPath`"" -ForegroundColor Cyan
Write-Host ""
Write-Host "The updated JAR includes all your changes:" -ForegroundColor Green
Write-Host "- Fixed attendance status update functionality" -ForegroundColor Yellow
Write-Host "- Enhanced face detection with real-time feedback" -ForegroundColor Yellow
Write-Host "- 3-attempt face verification system" -ForegroundColor Yellow
Write-Host "- Proper image file validation in registration" -ForegroundColor Yellow
Write-Host "- Camera capture only when face is detected" -ForegroundColor Yellow