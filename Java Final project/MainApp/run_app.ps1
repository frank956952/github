#!/usr/bin/env powershell

# Navigate to the project directory
Set-Location -Path "c:\Users\angel\eclipse-workspace\Java Final project\MainApp"

# Define the classpath
$classpath = "bin;C:\Users\angel\eclipse-workspace\mysql-connector-j-9.2.0.jar"

# Run the application
Write-Host "Starting Fitness Tracker application..." -ForegroundColor Green
& java "-cp" $classpath "Main"

# Check for errors
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start the application. Error code: $LASTEXITCODE" -ForegroundColor Red
    Read-Host "Press Enter to continue..."
    exit 1
}
