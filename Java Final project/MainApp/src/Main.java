import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {        String url = "jdbc:mysql://140.119.19.73:3315/113306052"; // 資料庫名稱
        String user = "113306052";
        String password = "2rnei";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Initialize DAOs and create necessary tables
            WorkoutSessionDAO sessionDAO = new WorkoutSessionDAO(conn);
            WeeklyPlanDAO planDAO = new WeeklyPlanDAO(conn);
            ExerciseDAO exerciseDAO = new ExerciseDAO(conn);
            WorkoutExerciseDAO workoutExerciseDAO = new WorkoutExerciseDAO(conn);
            
            // Create tables if they don't exist
            sessionDAO.createTables();
            planDAO.createTable();
            exerciseDAO.createTable();
            workoutExerciseDAO.createTable();
            
            // Insert default exercises if needed
            exerciseDAO.insertDefaultExercises();
            
            // 記錄含有詳細練習的健身課程
            WorkoutSession session = new WorkoutSession(LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                    Arrays.asList(BodyPart.CHEST, BodyPart.ARMS));
            
            // Get available exercises for the selected body part
            List<Exercise> chestExercises = exerciseDAO.getExercisesByBodyPart(BodyPart.CHEST);
            if (!chestExercises.isEmpty()) {
                // Add a chest exercise to the workout session
                Exercise benchPress = chestExercises.get(0); // 槓鈴臥推
                WorkoutExercise workoutExercise = new WorkoutExercise();
                workoutExercise.setExercise(benchPress);
                workoutExercise.setSets(4);
                workoutExercise.setReps(12);
                workoutExercise.setWeight(60.0);
                session.addExercise(workoutExercise);
                
                // Add another chest exercise
                if (chestExercises.size() > 1) {
                    Exercise dumbbellPress = chestExercises.get(1); // 啞鈴臥推
                    WorkoutExercise workoutExercise2 = new WorkoutExercise();
                    workoutExercise2.setExercise(dumbbellPress);
                    workoutExercise2.setSets(3);
                    workoutExercise2.setReps(10);
                    workoutExercise2.setWeight(22.5);
                    session.addExercise(workoutExercise2);
                }
            }
            
            // Get exercises for arms
            List<Exercise> armsExercises = exerciseDAO.getExercisesByBodyPart(BodyPart.ARMS);
            if (!armsExercises.isEmpty()) {
                // Add an arms exercise to the workout session
                Exercise bicepCurls = armsExercises.get(0); // 槓鈴彎舉
                WorkoutExercise workoutExercise = new WorkoutExercise();
                workoutExercise.setExercise(bicepCurls);
                workoutExercise.setSets(3);
                workoutExercise.setReps(15);
                workoutExercise.setWeight(15.0);
                session.addExercise(workoutExercise);
                
                // Add another arms exercise (triceps exercise)
                if (armsExercises.size() > 1) {
                    Exercise tricepExtension = armsExercises.get(1); // 三頭下壓
                    WorkoutExercise workoutExercise2 = new WorkoutExercise();
                    workoutExercise2.setExercise(tricepExtension);
                    workoutExercise2.setSets(3);
                    workoutExercise2.setReps(12);
                    workoutExercise2.setWeight(20.0);
                    session.addExercise(workoutExercise2);
                }
                
                // Add one more arms exercise if available
                if (armsExercises.size() > 2) {
                    Exercise hammerCurls = armsExercises.get(2); // 錘式捲舉
                    WorkoutExercise workoutExercise3 = new WorkoutExercise();
                    workoutExercise3.setExercise(hammerCurls);
                    workoutExercise3.setSets(3);
                    workoutExercise3.setReps(10);
                    workoutExercise3.setWeight(12.5);
                    session.addExercise(workoutExercise3);
                }
            }
            
            // Save the workout session with exercises
            int sessionId = sessionDAO.save(session);
            System.out.println("Saved workout session with ID: " + sessionId);
            
            // Retrieve the saved session with exercises
            WorkoutSession retrievedSession = sessionDAO.findById(sessionId);
            System.out.println("Retrieved session: " + retrievedSession.getStartTime() + " - " + retrievedSession.getEndTime());
            System.out.println("Body parts: " + retrievedSession.getBodyPartsString());
            System.out.println("Exercises:");
            for (WorkoutExercise ex : retrievedSession.getExercises()) {
                System.out.println("  - " + ex);
            }

            // 規劃每週訓練
            WeeklyPlan plan = new WeeklyPlan();
            plan.addBodyPart(java.time.DayOfWeek.MONDAY, BodyPart.CHEST);
            plan.addBodyPart(java.time.DayOfWeek.WEDNESDAY, BodyPart.BACK);
            plan.addBodyPart(java.time.DayOfWeek.FRIDAY, BodyPart.LEGS);
            int planId = planDAO.save(plan);
            System.out.println("\nSaved weekly plan with ID: " + planId);

            // 檢索計劃
            WeeklyPlan retrievedPlan = planDAO.findCurrentPlan();
            System.out.println("Retrieved Plan: " + retrievedPlan.getPlan());
            
            // Launch the fitness tracker UI
            javax.swing.SwingUtilities.invokeLater(() -> new FitnessTrackerFrame(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
