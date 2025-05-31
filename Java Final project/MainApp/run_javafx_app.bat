@echo off
REM Batch script to run the Fitness Tracker JavaFX application
REM Edit this path to match your JavaFX installation location
SET JAVAFX_LIB_PATH=C:\Program Files\JavaFX\javafx-sdk-21\lib

REM Check if JavaFX path exists
IF NOT EXIST "%JAVAFX_LIB_PATH%" (
    echo Error: JavaFX path not found at %JAVAFX_LIB_PATH%
    echo Please update the JAVAFX_LIB_PATH variable in this script to point to your JavaFX installation.
    echo You can download JavaFX from https://openjfx.io/
    exit /b 1
)

REM Compile the Java files if needed
IF NOT EXIST ".\bin\MainFX.class" (
    echo Compiling Java files...
    javac -d bin -cp ".\bin;%JAVAFX_LIB_PATH%\*" src\*.java
    IF %ERRORLEVEL% NEQ 0 (
        echo Compilation failed. Please check your Java installation and code for errors.
        exit /b 1
    )
)

REM Run the application
echo Starting Fitness Tracker application...
java --module-path "%JAVAFX_LIB_PATH%" --add-modules javafx.controls,javafx.fxml -cp ".\bin;%JAVAFX_LIB_PATH%\*" MainFX

REM Check if application started successfully
IF %ERRORLEVEL% NEQ 0 (
    echo Failed to start the application. Error code: %ERRORLEVEL%
    exit /b 1
)