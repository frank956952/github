import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;

/**
 * A launcher for the fitness tracker application.
 * NOTE: This class is implemented using Swing since JavaFX dependencies
 * may not be properly set up in the current environment.
 */
public class FitnessTrackerFX {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 建立真實的資料庫連接
                String url = "jdbc:mysql://140.119.19.73:3315/113306052";
                String user = "113306052";
                String password = "2rnei";
                Connection conn = DriverManager.getConnection(url, user, password);
                
                FitnessTrackerFrame frame = new FitnessTrackerFrame(conn);
                frame.setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "無法連接到資料庫: " + e.getMessage(), "資料庫錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
}
    private WorkoutSessionDAO sessionDAO;
    private WeeklyPlanDAO planDAO;
    private ExerciseDAO exerciseDAO;
    private WorkoutExerciseDAO workoutExerciseDAO;
    private Connection connection;

    // Current workout session
    private WorkoutSession currentWorkoutSession = new WorkoutSession();
    
    // Main layout components
    private TabPane tabPane;
    private Tab recordTab;
    private Tab planTab;
    private Tab historyTab;
    
    // Record workout components
    private ComboBox<BodyPart> bodyPartComboBox;
    private TextField startTimeField;
    private TextField endTimeField;
    private ListView<Exercise> exerciseListView;
    private ListView<WorkoutExercise> selectedExercisesListView;
    private TextField setsField;
    private TextField repsField;
    private TextField weightField;
    private ObservableList<Exercise> exercisesObservableList = FXCollections.observableArrayList();
    private ObservableList<WorkoutExercise> selectedExercisesObservableList = FXCollections.observableArrayList();
    
    // Weekly plan components
    private ComboBox<java.time.DayOfWeek> dayComboBox;
    private ListView<BodyPart> bodyPartListView;
    private TextArea weeklyPlanTextArea;
    private WeeklyPlan weeklyPlan = new WeeklyPlan();
    
    // History components
    private ListView<WorkoutSession> historyListView;
    private TextArea sessionDetailsTextArea;
    private ObservableList<WorkoutSession> sessionsObservableList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // Setup database connection
        setupDatabase();
        
        // Create UI
        BorderPane root = new BorderPane();
        tabPane = new TabPane();
        
        // Create tabs
        recordTab = new Tab("記錄健身");
        recordTab.setClosable(false);
        recordTab.setContent(createRecordWorkoutPane());
        
        planTab = new Tab("每週計劃");
        planTab.setClosable(false);
        planTab.setContent(createWeeklyPlanPane());
        
        historyTab = new Tab("歷史記錄");
        historyTab.setClosable(false);
        historyTab.setContent(createHistoryPane());
        
        tabPane.getTabs().addAll(recordTab, planTab, historyTab);
        root.setCenter(tabPane);
        
        // Load data
        loadExercises();
        loadWeeklyPlan();
        loadWorkoutHistory();
        
        // Setup scene
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("健身追蹤器 (JavaFX 版)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void setupDatabase() {
        try {
            String url = "jdbc:mysql://140.119.19.73:3315/113306052";
            String user = "113306052";
            String password = "2rnei";
            
            connection = DriverManager.getConnection(url, user, password);
            sessionDAO = new WorkoutSessionDAO(connection);
            planDAO = new WeeklyPlanDAO(connection);
            exerciseDAO = new ExerciseDAO(connection);
            workoutExerciseDAO = new WorkoutExerciseDAO(connection);
            
            // Create tables if they don't exist
            sessionDAO.createTables();
            planDAO.createTable();
            exerciseDAO.createTable();
            workoutExerciseDAO.createTable();
            
            // Insert default exercises if needed
            exerciseDAO.insertDefaultExercises();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "資料庫錯誤", "無法連接到資料庫", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Pane createRecordWorkoutPane() {
        VBox mainVBox = new VBox(10);
        mainVBox.setPadding(new Insets(10));
        
        // Top controls (bodyPart, start time, end time)
        GridPane topGrid = new GridPane();
        topGrid.setHgap(10);
        topGrid.setVgap(10);
        
        // Body part selection
        Label bodyPartLabel = new Label("選擇身體部位:");
        bodyPartComboBox = new ComboBox<>(FXCollections.observableArrayList(BodyPart.values()));
        bodyPartComboBox.getSelectionModel().selectFirst();
        bodyPartComboBox.setOnAction(e -> updateExerciseList());
        
        topGrid.add(bodyPartLabel, 0, 0);
        topGrid.add(bodyPartComboBox, 1, 0);
        
        // Start time
        Label startTimeLabel = new Label("開始時間 (YYYY-MM-DD HH:MM):");
        startTimeField = new TextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        topGrid.add(startTimeLabel, 0, 1);
        topGrid.add(startTimeField, 1, 1);
        
        // End time
        Label endTimeLabel = new Label("結束時間 (YYYY-MM-DD HH:MM):");
        endTimeField = new TextField(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        topGrid.add(endTimeLabel, 0, 2);
        topGrid.add(endTimeField, 1, 2);
        
        mainVBox.getChildren().add(topGrid);
        
        // Middle section with exercise list and details
        HBox middleHBox = new HBox(10);
        
        // Available exercises
        VBox exercisesVBox = new VBox(5);
        exercisesVBox.setPadding(new Insets(5));
        exercisesVBox.setMinWidth(300);
        
        Label exercisesLabel = new Label("可用練習:");
        exerciseListView = new ListView<>(exercisesObservableList);
        exerciseListView.setPrefHeight(200);
        
        // Exercise details
        GridPane exerciseDetailsGrid = new GridPane();
        exerciseDetailsGrid.setHgap(5);
        exerciseDetailsGrid.setVgap(5);
        exerciseDetailsGrid.setPadding(new Insets(5));
        
        Label setsLabel = new Label("組數:");
        setsField = new TextField("3");
        exerciseDetailsGrid.add(setsLabel, 0, 0);
        exerciseDetailsGrid.add(setsField, 1, 0);
        
        Label repsLabel = new Label("次數:");
        repsField = new TextField("12");
        exerciseDetailsGrid.add(repsLabel, 0, 1);
        exerciseDetailsGrid.add(repsField, 1, 1);
        
        Label weightLabel = new Label("重量 (kg):");
        weightField = new TextField("20.0");
        exerciseDetailsGrid.add(weightLabel, 0, 2);
        exerciseDetailsGrid.add(weightField, 1, 2);
        
        Button addExerciseButton = new Button("添加練習");
        addExerciseButton.setOnAction(e -> addExerciseToWorkout());
        exerciseDetailsGrid.add(addExerciseButton, 1, 3);
        
        exercisesVBox.getChildren().addAll(exercisesLabel, exerciseListView, exerciseDetailsGrid);
        
        // Selected exercises
        VBox selectedExercisesVBox = new VBox(5);
        selectedExercisesVBox.setPadding(new Insets(5));
        selectedExercisesVBox.setMinWidth(300);
        
        Label selectedExercisesLabel = new Label("已選練習:");
        selectedExercisesListView = new ListView<>(selectedExercisesObservableList);
        selectedExercisesListView.setPrefHeight(200);
        
        Button removeExerciseButton = new Button("移除練習");
        removeExerciseButton.setOnAction(e -> removeSelectedExercise());
        
        selectedExercisesVBox.getChildren().addAll(selectedExercisesLabel, selectedExercisesListView, removeExerciseButton);
        
        middleHBox.getChildren().addAll(exercisesVBox, selectedExercisesVBox);
        mainVBox.getChildren().add(middleHBox);
        
        // Bottom buttons
        HBox bottomHBox = new HBox(10);
        bottomHBox.setPadding(new Insets(10, 0, 0, 0));
        bottomHBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button recordButton = new Button("記錄健身課程");
        recordButton.setOnAction(e -> recordWorkoutSession());
        
        bottomHBox.getChildren().add(recordButton);
        mainVBox.getChildren().add(bottomHBox);
        
        return mainVBox;
    }
    
    private Pane createWeeklyPlanPane() {
        VBox mainVBox = new VBox(10);
        mainVBox.setPadding(new Insets(10));
        
        // Top controls
        HBox topHBox = new HBox(10);
        topHBox.setPadding(new Insets(5));
        
        Label dayLabel = new Label("選擇星期:");
        dayComboBox = new ComboBox<>(FXCollections.observableArrayList(java.time.DayOfWeek.values()));
        dayComboBox.getSelectionModel().selectFirst();
        
        topHBox.getChildren().addAll(dayLabel, dayComboBox);
        mainVBox.getChildren().add(topHBox);
        
        // Body part list
        VBox bodyPartsVBox = new VBox(5);
        Label bodyPartsLabel = new Label("選擇身體部位:");
        bodyPartListView = new ListView<>(FXCollections.observableArrayList(BodyPart.values()));
        bodyPartListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bodyPartListView.setPrefHeight(150);
        
        bodyPartsVBox.getChildren().addAll(bodyPartsLabel, bodyPartListView);
        mainVBox.getChildren().add(bodyPartsVBox);
        
        // Buttons
        HBox buttonsHBox = new HBox(10);
        buttonsHBox.setPadding(new Insets(5));
        
        Button addToPlanButton = new Button("添加到計劃");
        addToPlanButton.setOnAction(e -> addBodyPartsToPlan());
        
        Button savePlanButton = new Button("保存計劃");
        savePlanButton.setOnAction(e -> saveWeeklyPlan());
        
        Button clearPlanButton = new Button("清除計劃");
        clearPlanButton.setOnAction(e -> clearWeeklyPlan());
        
        buttonsHBox.getChildren().addAll(addToPlanButton, savePlanButton, clearPlanButton);
        mainVBox.getChildren().add(buttonsHBox);
        
        // Plan display
        VBox planDisplayVBox = new VBox(5);
        planDisplayVBox.setPadding(new Insets(5));
        
        Label planLabel = new Label("當前計劃:");
        weeklyPlanTextArea = new TextArea();
        weeklyPlanTextArea.setEditable(false);
        weeklyPlanTextArea.setPrefHeight(200);
        
        planDisplayVBox.getChildren().addAll(planLabel, weeklyPlanTextArea);
        mainVBox.getChildren().add(planDisplayVBox);
        
        return mainVBox;
    }
    
    private Pane createHistoryPane() {
        VBox mainVBox = new VBox(10);
        mainVBox.setPadding(new Insets(10));
        
        // Workout sessions list
        VBox sessionsVBox = new VBox(5);
        Label sessionsLabel = new Label("歷史健身記錄:");
        historyListView = new ListView<>(sessionsObservableList);
        historyListView.setCellFactory(param -> new ListCell<WorkoutSession>() {
            @Override
            protected void updateItem(WorkoutSession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + 
                            " - " + item.getBodyPartsString());
                }
            }
        });
        historyListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displaySessionDetails(newVal);
            }
        });
        historyListView.setPrefHeight(200);
        
        sessionsVBox.getChildren().addAll(sessionsLabel, historyListView);
        mainVBox.getChildren().add(sessionsVBox);
        
        // Session details
        VBox detailsVBox = new VBox(5);
        Label detailsLabel = new Label("課程詳情:");
        sessionDetailsTextArea = new TextArea();
        sessionDetailsTextArea.setEditable(false);
        sessionDetailsTextArea.setPrefHeight(200);
        
        detailsVBox.getChildren().addAll(detailsLabel, sessionDetailsTextArea);
        mainVBox.getChildren().add(detailsVBox);
        
        // Buttons
        HBox buttonsHBox = new HBox(10);
        buttonsHBox.setPadding(new Insets(5));
        buttonsHBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> loadWorkoutHistory());
        
        Button deleteButton = new Button("刪除記錄");
        deleteButton.setOnAction(e -> deleteSelectedSession());
        
        buttonsHBox.getChildren().addAll(refreshButton, deleteButton);
        mainVBox.getChildren().add(buttonsHBox);
        
        return mainVBox;
    }
    
    private void updateExerciseList() {
        exercisesObservableList.clear();
        BodyPart selectedBodyPart = bodyPartComboBox.getValue();
        
        if (selectedBodyPart != null) {
            try {
                List<Exercise> exercises = exerciseDAO.getExercisesByBodyPart(selectedBodyPart);
                exercisesObservableList.addAll(exercises);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "資料庫錯誤", "無法載入練習", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void loadExercises() {
        updateExerciseList();
    }
    
    private void loadWeeklyPlan() {
        try {
            WeeklyPlan currentPlan = planDAO.findCurrentPlan();
            weeklyPlan = currentPlan;
            updateWeeklyPlanDisplay();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "資料庫錯誤", "無法載入每週計劃", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadWorkoutHistory() {
        try {
            List<WorkoutSession> sessions = sessionDAO.findAll();
            sessionsObservableList.clear();
            sessionsObservableList.addAll(sessions);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "資料庫錯誤", "無法載入健身記錄", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addExerciseToWorkout() {
        Exercise selectedExercise = exerciseListView.getSelectionModel().getSelectedItem();
        if (selectedExercise == null) {
            showAlert(Alert.AlertType.WARNING, "警告", "未選擇練習", "請先選擇一個練習。");
            return;
        }
        
        try {
            int sets = Integer.parseInt(setsField.getText().trim());
            int reps = Integer.parseInt(repsField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setExercise(selectedExercise);
            workoutExercise.setSets(sets);
            workoutExercise.setReps(reps);
            workoutExercise.setWeight(weight);
            
            selectedExercisesObservableList.add(workoutExercise);
            currentWorkoutSession.addExercise(workoutExercise);
            
            // Reset fields
            setsField.setText("3");
            repsField.setText("12");
            weightField.setText("20.0");
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "輸入錯誤", "無效的數字", 
                    "請輸入有效的數字。組數和次數必須是整數，重量必須是數字。");
        }
    }
    
    private void removeSelectedExercise() {
        WorkoutExercise selectedExercise = selectedExercisesListView.getSelectionModel().getSelectedItem();
        if (selectedExercise == null) {
            showAlert(Alert.AlertType.WARNING, "警告", "未選擇練習", "請先選擇一個要移除的練習。");
            return;
        }
        
        selectedExercisesObservableList.remove(selectedExercise);
        currentWorkoutSession.getExercises().remove(selectedExercise);
    }
    
    private void recordWorkoutSession() {
        try {
            // Parse times
            LocalDateTime startTime = LocalDateTime.parse(startTimeField.getText(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endTime = LocalDateTime.parse(endTimeField.getText(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            // Set session properties
            currentWorkoutSession.setStartTime(startTime);
            currentWorkoutSession.setEndTime(endTime);
            
            // Get selected body part
            BodyPart selectedPart = bodyPartComboBox.getValue();
            List<BodyPart> bodyParts = new ArrayList<>();
            bodyParts.add(selectedPart);
            currentWorkoutSession.setBodyParts(bodyParts);
            
            // Check if there are exercises
            if (currentWorkoutSession.getExercises().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("確認");
                alert.setHeaderText("未添加練習");
                alert.setContentText("您尚未添加任何練習。是否繼續保存？");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    return;
                }
            }
            
            // Save the session
            int sessionId = sessionDAO.save(currentWorkoutSession);
            showAlert(Alert.AlertType.INFORMATION, "成功", "健身記錄已保存", 
                    "健身記錄已成功保存，ID: " + sessionId);
            
            // Reset the session
            currentWorkoutSession = new WorkoutSession();
            selectedExercisesObservableList.clear();
            
            // Reset the UI
            bodyPartComboBox.getSelectionModel().selectFirst();
            startTimeField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            endTimeField.setText(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            // Refresh history
            loadWorkoutHistory();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "錯誤", "保存健身記錄失敗", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addBodyPartsToPlan() {
        java.time.DayOfWeek selectedDay = dayComboBox.getValue();
        List<BodyPart> selectedParts = bodyPartListView.getSelectionModel().getSelectedItems();
        
        if (selectedDay == null || selectedParts.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "警告", "未選擇", "請選擇星期和至少一個身體部位。");
            return;
        }
        
        for (BodyPart part : selectedParts) {
            weeklyPlan.addBodyPart(selectedDay, part);
        }
        
        updateWeeklyPlanDisplay();
    }
    
    private void saveWeeklyPlan() {
        try {
            // First clear existing plans
            planDAO.deleteAllPlans();
            
            // Then save the new plan
            int planId = planDAO.save(weeklyPlan);
            showAlert(Alert.AlertType.INFORMATION, "成功", "計劃已保存", 
                    "每週計劃已成功保存，ID: " + planId);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "錯誤", "保存計劃失敗", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearWeeklyPlan() {
        try {
            planDAO.deleteAllPlans();
            weeklyPlan = new WeeklyPlan();
            updateWeeklyPlanDisplay();
            showAlert(Alert.AlertType.INFORMATION, "成功", "計劃已清除", "每週計劃已成功清除。");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "錯誤", "清除計劃失敗", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateWeeklyPlanDisplay() {
        StringBuilder sb = new StringBuilder();
        
        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            List<BodyPart> parts = weeklyPlan.getPlan().get(day);
            if (parts != null && !parts.isEmpty()) {
                sb.append(day.toString()).append(":\n");
                for (BodyPart part : parts) {
                    sb.append("  - ").append(part.toString()).append("\n");
                }
                sb.append("\n");
            }
        }
        
        if (sb.length() == 0) {
            sb.append("目前沒有已保存的每週計劃。");
        }
        
        weeklyPlanTextArea.setText(sb.toString());
    }
    
    private void displaySessionDetails(WorkoutSession session) {
        if (session == null) {
            sessionDetailsTextArea.clear();
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("日期: ").append(session.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append("時間: ").append(session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
          .append(" - ").append(session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
        sb.append("身體部位: ").append(session.getBodyPartsString()).append("\n\n");
        sb.append("練習:\n");
        
        List<WorkoutExercise> exercises = session.getExercises();
        if (exercises != null && !exercises.isEmpty()) {
            for (WorkoutExercise ex : exercises) {
                sb.append("  - ").append(ex.toString()).append("\n");
            }
        } else {
            sb.append("  (無練習記錄)\n");
        }
        
        sessionDetailsTextArea.setText(sb.toString());
    }
    
    private void deleteSelectedSession() {
        WorkoutSession selectedSession = historyListView.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            showAlert(Alert.AlertType.WARNING, "警告", "未選擇記錄", "請先選擇一個要刪除的健身記錄。");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("確認刪除");
        alert.setHeaderText("刪除健身記錄");
        alert.setContentText("您確定要刪除此健身記錄嗎？此操作無法恢復。");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = sessionDAO.delete(selectedSession.getId());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "成功", "記錄已刪除", "健身記錄已成功刪除。");
                    loadWorkoutHistory();
                    sessionDetailsTextArea.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "錯誤", "刪除失敗", "無法刪除選定的健身記錄。");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "錯誤", "刪除失敗", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        // Close database connection
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}