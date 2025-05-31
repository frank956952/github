# PowerShell script to run the Fitness Tracker Swing application

# Set the classpath with the MySQL connector
$CLASSPATH = ".\bin;C:\Users\angel\eclipse-workspace\mysql-connector-j-9.2.0.jar"

Write-Host "Starting Fitness Tracker application..." -ForegroundColor Green
java -cp $CLASSPATH Main

# Check if application started successfully
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start the application. Error code: $LASTEXITCODE" -ForegroundColor Red
    Read-Host "Press Enter to continue..."
    exit 1
}