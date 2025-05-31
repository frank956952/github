# PowerShell script to run the Fitness Tracker JavaFX application
# Edit this path to match your JavaFX installation location
$JAVAFX_LIB_PATH = "C:\Program Files\JavaFX\javafx-sdk-21\lib"

# Check if JavaFX path exists
if (-not (Test-Path $JAVAFX_LIB_PATH)) {
    Write-Host "Error: JavaFX path not found at $JAVAFX_LIB_PATH" -ForegroundColor Red
    Write-Host "Please update the JAVAFX_LIB_PATH variable in this script to point to your JavaFX installation." -ForegroundColor Yellow
    Write-Host "You can download JavaFX from https://openjfx.io/" -ForegroundColor Yellow
    exit 1
}

# Compile the Java files if needed
if (-not (Test-Path ".\bin\MainFX.class")) {
    Write-Host "Compiling Java files..." -ForegroundColor Cyan
    javac -d bin -cp ".\bin;$JAVAFX_LIB_PATH\*" src\*.java
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed. Please check your Java installation and code for errors." -ForegroundColor Red
        exit 1
    }
}

# Run the application
Write-Host "Starting Fitness Tracker application..." -ForegroundColor Green
java --module-path "$JAVAFX_LIB_PATH" --add-modules javafx.controls,javafx.fxml -cp ".\bin;$JAVAFX_LIB_PATH\*" MainFX

# Check if application started successfully
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start the application. Error code: $LASTEXITCODE" -ForegroundColor Red
    exit 1
}