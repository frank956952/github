import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class FitnessTrackerFrame extends JFrame {    private WorkoutSessionDAO sessionDAO;
    private WeeklyPlanDAO planDAO;
    private ExerciseDAO exerciseDAO;

    // GUI 組件
    private JTabbedPane tabbedPane;
    private JPanel recordPanel;
    private JPanel planPanel;

    // 記錄健身課程的組件
    private JComboBox<BodyPart> bodyPartComboBox;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JButton recordButton;
    
    // 練習選擇的組件
    private JList<Exercise> exerciseList;
    private DefaultListModel<Exercise> exerciseListModel;
    private JPanel exerciseDetailsPanel;
    private JTextField setsField;
    private JTextField repsField;
    private JTextField weightField;
    private JButton addExerciseButton;
    private JList<WorkoutExercise> selectedExercisesList;
    private DefaultListModel<WorkoutExercise> selectedExercisesModel;

    // 規劃每週訓練的組件
    private JComboBox<DayOfWeek> dayComboBox;
    private JList<BodyPart> bodyPartList;
    private DefaultListModel<BodyPart> bodyPartListModel;
    private JButton addBodyPartButton;
    private JButton savePlanButton;
    private JTextArea weeklyPlanDisplayArea;
    
    // 當前的健身課程
    private WorkoutSession currentWorkoutSession;    public FitnessTrackerFrame(Connection connection) {
        this.sessionDAO = new WorkoutSessionDAO(connection);
        this.planDAO = new WeeklyPlanDAO(connection);
        this.exerciseDAO = new ExerciseDAO(connection);
        
        // 初始化當前會話
        this.currentWorkoutSession = new WorkoutSession();

        // 設置主框架
        setTitle("健身追蹤器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 創建標籤頁
        tabbedPane = new JTabbedPane();
        recordPanel = new JPanel(new BorderLayout(10, 10));
        planPanel = new JPanel(new BorderLayout(10, 10));

        // 初始化資料庫表 - important to do this before initializing UI components
        try {
            sessionDAO.createTables();
            planDAO.createTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "初始化資料庫表時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
        planPanel = new JPanel(new BorderLayout(10, 10));

        // 初始化記錄面板
        initRecordPanel();
        // 初始化計劃面板
        initPlanPanel();

        // 將面板添加到標籤頁
        tabbedPane.addTab("記錄健身", recordPanel);
        tabbedPane.addTab("每週計劃", planPanel);

        // 將標籤頁添加到主框架
        add(tabbedPane);
        
        // 初始化資料庫表
        try {
            sessionDAO.createTables();
            planDAO.createTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "初始化資料庫表時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
        
        // 顯示窗口
        setVisible(true);
    }

    private void initRecordPanel() {
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        // 身體部位選擇
        JLabel bodyPartLabel = new JLabel("選擇身體部位：");
        bodyPartComboBox = new JComboBox<>(BodyPart.values());
        bodyPartComboBox.addActionListener(e -> updateExerciseList());
        topPanel.add(bodyPartLabel);
        topPanel.add(bodyPartComboBox);

        // 開始時間
        JLabel startTimeLabel = new JLabel("開始時間 (YYYY-MM-DD HH:MM)：");
        // Default to current time
        String currentTime = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        startTimeField = new JTextField(currentTime);
        topPanel.add(startTimeLabel);
        topPanel.add(startTimeField);

        // 結束時間
        JLabel endTimeLabel = new JLabel("結束時間 (YYYY-MM-DD HH:MM)：");
        // Default to current time + 1 hour
        String endTimeDefault = LocalDateTime.now().plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        endTimeField = new JTextField(endTimeDefault);
        topPanel.add(endTimeLabel);
        topPanel.add(endTimeField);
        
        recordPanel.add(topPanel, BorderLayout.NORTH);
        
        // 中央面板，包含可用練習和已選練習
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // 左側：可用練習列表面板
        JPanel availableExercisesPanel = new JPanel(new BorderLayout(5, 5));
        availableExercisesPanel.setBorder(BorderFactory.createTitledBorder("可用練習"));
        
        exerciseListModel = new DefaultListModel<>();
        exerciseList = new JList<>(exerciseListModel);
        exerciseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane exerciseScrollPane = new JScrollPane(exerciseList);
        availableExercisesPanel.add(exerciseScrollPane, BorderLayout.CENTER);
        
        // 練習詳情面板
        exerciseDetailsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        exerciseDetailsPanel.setBorder(BorderFactory.createTitledBorder("練習詳情"));
        
        exerciseDetailsPanel.add(new JLabel("組數:"));
        setsField = new JTextField("3");
        exerciseDetailsPanel.add(setsField);
        
        exerciseDetailsPanel.add(new JLabel("次數:"));
        repsField = new JTextField("12");
        exerciseDetailsPanel.add(repsField);
        
        exerciseDetailsPanel.add(new JLabel("重量 (kg):"));
        weightField = new JTextField("20.0");
        exerciseDetailsPanel.add(weightField);
        
        addExerciseButton = new JButton("添加練習");
        addExerciseButton.addActionListener(e -> addExerciseToWorkout());
        exerciseDetailsPanel.add(addExerciseButton);
        exerciseDetailsPanel.add(new JLabel()); // 空白佔位
        
        availableExercisesPanel.add(exerciseDetailsPanel, BorderLayout.SOUTH);
        
        // 右側：已選練習列表面板
        JPanel selectedExercisesPanel = new JPanel(new BorderLayout(5, 5));
        selectedExercisesPanel.setBorder(BorderFactory.createTitledBorder("已選練習"));
        
        selectedExercisesModel = new DefaultListModel<>();
        selectedExercisesList = new JList<>(selectedExercisesModel);
        JScrollPane selectedExercisesScrollPane = new JScrollPane(selectedExercisesList);
        selectedExercisesPanel.add(selectedExercisesScrollPane, BorderLayout.CENTER);
        
        JPanel selectedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeExerciseButton = new JButton("移除練習");
        removeExerciseButton.addActionListener(e -> removeSelectedExercise());
        selectedButtonsPanel.add(removeExerciseButton);
        selectedExercisesPanel.add(selectedButtonsPanel, BorderLayout.SOUTH);
        
        centerPanel.add(availableExercisesPanel);
        centerPanel.add(selectedExercisesPanel);
        
        recordPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 底部面板：記錄按鈕
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        recordButton = new JButton("記錄健身課程");
        recordButton.addActionListener(e -> recordWorkoutSession());
        bottomPanel.add(recordButton);
        
        JButton viewSessionsButton = new JButton("查看已記錄的課程");
        viewSessionsButton.addActionListener(e -> viewWorkoutSessions());
        bottomPanel.add(viewSessionsButton);
        
        recordPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // 初始加載練習列表
        updateExerciseList();
    }

    private void initPlanPanel() {
        // 星期選擇
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel dayLabel = new JLabel("選擇星期：");
        dayComboBox = new JComboBox<>(DayOfWeek.values());
        topPanel.add(dayLabel);
        topPanel.add(dayComboBox);

        // 身體部位清單
        bodyPartListModel = new DefaultListModel<>();
        bodyPartList = new JList<>(bodyPartListModel);
        bodyPartList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        for (BodyPart part : BodyPart.values()) {
            bodyPartListModel.addElement(part);
        }
        JScrollPane listScrollPane = new JScrollPane(bodyPartList);

        // 添加身體部位按鈕
        addBodyPartButton = new JButton("添加到計劃");
        addBodyPartButton.addActionListener(e -> addBodyPartToPlan());
        topPanel.add(addBodyPartButton);

        // 保存計劃按鈕
        savePlanButton = new JButton("保存計劃");
        savePlanButton.addActionListener(e -> saveWeeklyPlan());
        topPanel.add(savePlanButton);

        // 新增刪除所有紀錄按鈕
        JButton deleteAllButton = new JButton("刪除所有紀錄");
        deleteAllButton.addActionListener(e -> deleteAllWeeklyPlans());
        topPanel.add(deleteAllButton);

        planPanel.add(topPanel, BorderLayout.NORTH);
        planPanel.add(listScrollPane, BorderLayout.CENTER);

        // 新增：用於顯示已保存計劃的面板和文本區域
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("當前已保存計劃"));
        weeklyPlanDisplayArea = new JTextArea(10, 40); // 可根據需要調整行數和列數
        weeklyPlanDisplayArea.setEditable(false); // 設為不可編輯
        JScrollPane displayScrollPane = new JScrollPane(weeklyPlanDisplayArea);
        displayPanel.add(displayScrollPane, BorderLayout.CENTER);

        planPanel.add(displayPanel, BorderLayout.SOUTH); // 將顯示面板添加到主計劃面板的南部

        updateWeeklyPlanDisplay(); // 初始化時加載並顯示計劃
    }
    
    // 更新可用練習列表
    private void updateExerciseList() {
        exerciseListModel.clear();
        BodyPart selectedBodyPart = (BodyPart) bodyPartComboBox.getSelectedItem();
        
        try {
            List<Exercise> exercises = exerciseDAO.getExercisesByBodyPart(selectedBodyPart);
            for (Exercise exercise : exercises) {
                exerciseListModel.addElement(exercise);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "載入練習列表時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 添加練習到當前健身課程
    private void addExerciseToWorkout() {
        Exercise selectedExercise = exerciseList.getSelectedValue();
        if (selectedExercise == null) {
            JOptionPane.showMessageDialog(this, "請先選擇一個練習。");
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
            
            // Add to the current session
            if (currentWorkoutSession.getExercises() == null) {
                currentWorkoutSession.setExercises(new ArrayList<>());
            }
            currentWorkoutSession.addExercise(workoutExercise);
            
            // Update the UI
            selectedExercisesModel.addElement(workoutExercise);
            
            // Reset fields to default values
            setsField.setText("3");
            repsField.setText("12");
            weightField.setText("20.0");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "請輸入有效的數字。組數和次數必須是整數，重量必須是數字。");
        }
    }
    
    // 從當前健身課程中移除練習
    private void removeSelectedExercise() {
        int selectedIndex = selectedExercisesList.getSelectedIndex();
        if (selectedIndex != -1) {
            // Remove from model
            WorkoutExercise exerciseToRemove = selectedExercisesModel.getElementAt(selectedIndex);
            selectedExercisesModel.removeElementAt(selectedIndex);
            
            // Remove from current session
            List<WorkoutExercise> exercises = currentWorkoutSession.getExercises();
            if (exercises != null) {
                exercises.remove(exerciseToRemove);
            }
        } else {
            JOptionPane.showMessageDialog(this, "請先選擇要移除的練習。");
        }
    }

    // 記錄包含練習的健身課程
    private void recordWorkoutSession() {
        try {
            // 解析時間
            LocalDateTime startTime = LocalDateTime.parse(startTimeField.getText(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endTime = LocalDateTime.parse(endTimeField.getText(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            // 獲取選擇的身體部位
            BodyPart selectedPart = (BodyPart) bodyPartComboBox.getSelectedItem();
            List<BodyPart> bodyParts = new ArrayList<>();
            bodyParts.add(selectedPart);

            // 更新當前健身課程的時間和身體部位
            currentWorkoutSession.setStartTime(startTime);
            currentWorkoutSession.setEndTime(endTime);
            currentWorkoutSession.setBodyParts(bodyParts);
            
            // 檢查是否有選擇練習
            if (currentWorkoutSession.getExercises() == null || currentWorkoutSession.getExercises().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "您尚未添加任何練習。是否繼續保存？", 
                    "確認保存", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // 保存健身課程
            int sessionId = sessionDAO.save(currentWorkoutSession);
            JOptionPane.showMessageDialog(this, "健身記錄已保存，ID：" + sessionId);
            
            // 重設當前健身課程
            currentWorkoutSession = new WorkoutSession();
            selectedExercisesModel.clear();
            
            // 更新界面
            bodyPartComboBox.setSelectedIndex(0);
            String currentTime = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            startTimeField.setText(currentTime);
            String endTimeDefault = LocalDateTime.now().plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            endTimeField.setText(endTimeDefault);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "錯誤：" + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // 查看已記錄的健身課程
    private void viewWorkoutSessions() {
        try {
            List<WorkoutSession> sessions = sessionDAO.findAll();
            if (sessions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "暫無健身記錄。");
                return;
            }
            
            JFrame sessionsFrame = new JFrame("已記錄的健身課程");
            sessionsFrame.setSize(600, 400);
            sessionsFrame.setLocationRelativeTo(this);
            
            // 創建顯示區域
            JTextArea sessionsTextArea = new JTextArea(20, 50);
            sessionsTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(sessionsTextArea);
            
            StringBuilder sb = new StringBuilder();
            for (WorkoutSession session : sessions) {                sb.append("ID: ").append(session.getId())
                  .append("\n日期: ").append(session.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                  .append("\n時間: ").append(session.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                  .append(" - ").append(session.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                  .append("\n身體部位: ").append(session.getBodyPartsString())
                  .append("\n練習:\n");
                
                List<WorkoutExercise> exercises = session.getExercises();
                if (exercises != null && !exercises.isEmpty()) {
                    for (WorkoutExercise ex : exercises) {
                        sb.append("  - ").append(ex).append("\n");
                    }
                } else {
                    sb.append("  (無練習記錄)\n");
                }
                sb.append("\n------------------------------\n\n");
            }
            
            sessionsTextArea.setText(sb.toString());
            sessionsFrame.add(scrollPane);
            sessionsFrame.setVisible(true);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "載入健身記錄時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private WeeklyPlan weeklyPlan = new WeeklyPlan();

    private void addBodyPartToPlan() {
        DayOfWeek selectedDay = (DayOfWeek) dayComboBox.getSelectedItem();
        List<BodyPart> selectedParts = bodyPartList.getSelectedValuesList();
        for (BodyPart part : selectedParts) {
            weeklyPlan.addBodyPart(selectedDay, part);
        }
        JOptionPane.showMessageDialog(this, "已將部位添加到 " + selectedDay);
    }

    private void saveWeeklyPlan() {
        try {
            int planId = planDAO.save(weeklyPlan);
            JOptionPane.showMessageDialog(this, "每週計劃已保存，ID：" + planId);
            // Clear the local weeklyPlan object after saving to prevent duplicate entries on next save
            weeklyPlan = new WeeklyPlan();
            updateWeeklyPlanDisplay(); // 刷新顯示
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "錯誤：" + ex.getMessage());
        }
    }

    private void deleteAllWeeklyPlans() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "您確定要刪除所有每週訓練計劃紀錄嗎？此操作無法復原。",
                "確認刪除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                planDAO.deleteAllPlans(); // This method needs to be created in WeeklyPlanDAO
                JOptionPane.showMessageDialog(this, "所有每週訓練計劃紀錄已成功刪除。");
                updateWeeklyPlanDisplay(); // 刷新顯示
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "刪除紀錄時發生錯誤：" + ex.getMessage(), "刪除錯誤", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // 更新每週計劃顯示
    private void updateWeeklyPlanDisplay() {
        try {
            WeeklyPlan currentPlan = planDAO.findCurrentPlan();
            if (currentPlan != null && currentPlan.getPlan() != null && !currentPlan.getPlan().isEmpty()) {
                StringBuilder displayText = new StringBuilder();
                List<DayOfWeek> sortedDays = new ArrayList<>(currentPlan.getPlan().keySet());
                sortedDays.sort(null); // 按星期幾的自然順序排序

                boolean hasEntries = false;
                for (DayOfWeek day : sortedDays) {
                    List<BodyPart> parts = currentPlan.getPlan().get(day);
                    if (parts != null && !parts.isEmpty()) {
                        hasEntries = true;
                        displayText.append(day.toString()).append(":\n");
                        for (BodyPart part : parts) {
                            displayText.append("  - ").append(part.toString()).append("\n");
                        }
                        displayText.append("\n");
                    }
                }
                if (hasEntries) {
                    weeklyPlanDisplayArea.setText(displayText.toString());
                } else {
                    weeklyPlanDisplayArea.setText("目前沒有已保存的每週計劃。");
                }
            } else {
                weeklyPlanDisplayArea.setText("目前沒有已保存的每週計劃。");
            }
        } catch (SQLException e) {
            weeklyPlanDisplayArea.setText("讀取計劃時發生錯誤：\n" + e.getMessage());
            e.printStackTrace(); // 方便調試
        }
    }

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