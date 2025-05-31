@echo off
REM Batch script to run the Fitness Tracker Swing application

REM Set the classpath with the MySQL connector
SET CLASSPATH=.\bin;C:\Users\angel\eclipse-workspace\mysql-connector-j-9.2.0.jar

echo Starting Fitness Tracker application...
java -cp %CLASSPATH% Main

IF %ERRORLEVEL% NEQ 0 (
    echo Failed to start the application. Error code: %ERRORLEVEL%
    pause
    exit /b 1
)