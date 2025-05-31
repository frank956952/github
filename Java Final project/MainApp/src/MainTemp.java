import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class MainTemp {
    public static void main(String[] args) {
        String url = "jdbc:mysql://140.119.19.73:3315/113306052"; // Database name
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
            
            // Launch the fitness tracker UI
            javax.swing.SwingUtilities.invokeLater(() -> new FitnessTrackerFrame(conn));
        } catch (SQLException e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Unable to connect to database: " + e.getMessage(), "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
