# JavaFX Setup Guide

This guide will help you set up JavaFX for the Fitness Tracker application.

## Prerequisites

1. Make sure you have Java JDK 11 or newer installed
2. Eclipse IDE or another Java IDE of your choice

## Setting up JavaFX in Eclipse

### Step 1: Download JavaFX SDK

1. Go to [OpenJFX](https://openjfx.io/) website
2. Download the JavaFX SDK appropriate for your operating system
3. Extract the downloaded file to a location on your computer (e.g., `C:\Program Files\JavaFX\javafx-sdk-21`)

### Step 2: Configure Eclipse

1. Open Eclipse
2. Go to Window > Preferences > Java > Build Path > User Libraries
3. Click "New..." and enter "JavaFX21" as the name
4. Select the new "JavaFX21" library and click "Add External JARs..."
5. Navigate to the "lib" folder of your JavaFX SDK installation
6. Select all JAR files and click "Open"
7. Click "Apply and Close"

### Step 3: Configure Project Properties

1. Right-click on the project in Eclipse's Package Explorer
2. Select Properties > Java Build Path > Libraries
3. Click "Add Library..." > User Library
4. Check "JavaFX21" and click "Finish"
5. Click "Apply and Close"

### Step 4: Configure VM Arguments

1. Right-click on the project and select "Run As" > "Run Configurations..."
2. Select your main class configuration (or create a new one)
3. Go to the "Arguments" tab
4. In the "VM arguments" section, add:
   ```
   --module-path "PATH_TO_JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml
   ```
   Replace `PATH_TO_JAVAFX_LIB` with the path to your JavaFX lib folder (e.g., `C:\Program Files\JavaFX\javafx-sdk-21\lib`)
5. Click "Apply" and "Run"

## Using the Launch Configuration File

For convenience, a launch configuration file (`FitnessTrackerFX.launch.example`) is provided:

1. Copy `FitnessTrackerFX.launch.example` to `FitnessTrackerFX.launch`
2. Edit the file to update the JavaFX path to match your installation
3. Use this launch configuration to run the application

## Alternative: Using Run Scripts

You can also use the provided batch or PowerShell scripts:

- Windows: Edit `run_javafx_app.bat` to set the correct JavaFX path
- PowerShell: Edit `run_javafx_app.ps1` to set the correct JavaFX path

Run the appropriate script to launch the JavaFX application.