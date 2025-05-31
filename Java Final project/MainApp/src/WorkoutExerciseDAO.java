import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WorkoutExerciseDAO {
    private Connection connection;
    private ExerciseDAO exerciseDAO;
    
    public WorkoutExerciseDAO(Connection connection) {
        this.connection = connection;
        this.exerciseDAO = new ExerciseDAO(connection);
    }
    
    // Create the workout_exercises table if it doesn't exist
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS workout_exercises (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "workout_session_id INT NOT NULL," +
                     "exercise_id INT NOT NULL," +
                     "sets INT NOT NULL," +
                     "reps INT NOT NULL," +
                     "weight DOUBLE NOT NULL," +
                     "FOREIGN KEY (workout_session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE," +
                     "FOREIGN KEY (exercise_id) REFERENCES exercises(id)" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    // Add a workout exercise
    public int addWorkoutExercise(WorkoutExercise workoutExercise) throws SQLException {
        String sql = "INSERT INTO workout_exercises (workout_session_id, exercise_id, sets, reps, weight) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, workoutExercise.getWorkoutSessionId());
            pstmt.setInt(2, workoutExercise.getExerciseId());
            pstmt.setInt(3, workoutExercise.getSets());
            pstmt.setInt(4, workoutExercise.getReps());
            pstmt.setDouble(5, workoutExercise.getWeight());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    workoutExercise.setId(id);
                    return id;
                } else {
                    throw new SQLException("Creating workout exercise failed, no ID obtained.");
                }
            }
        }
    }
    
    // Get all workout exercises for a session
    public List<WorkoutExercise> getWorkoutExercisesBySessionId(int sessionId) throws SQLException {
        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        String sql = "SELECT * FROM workout_exercises WHERE workout_session_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutExercise workoutExercise = new WorkoutExercise(
                        rs.getInt("id"),
                        rs.getInt("workout_session_id"),
                        rs.getInt("exercise_id"),
                        rs.getInt("sets"),
                        rs.getInt("reps"),
                        rs.getDouble("weight")
                    );
                    
                    // Load the exercise
                    Exercise exercise = exerciseDAO.getExerciseById(workoutExercise.getExerciseId());
                    workoutExercise.setExercise(exercise);
                    
                    workoutExercises.add(workoutExercise);
                }
            }
        }
        
        return workoutExercises;
    }
    
    // Get a workout exercise by id
    public WorkoutExercise getWorkoutExerciseById(int id) throws SQLException {
        String sql = "SELECT * FROM workout_exercises WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    WorkoutExercise workoutExercise = new WorkoutExercise(
                        rs.getInt("id"),
                        rs.getInt("workout_session_id"),
                        rs.getInt("exercise_id"),
                        rs.getInt("sets"),
                        rs.getInt("reps"),
                        rs.getDouble("weight")
                    );
                    
                    // Load the exercise
                    Exercise exercise = exerciseDAO.getExerciseById(workoutExercise.getExerciseId());
                    workoutExercise.setExercise(exercise);
                    
                    return workoutExercise;
                } else {
                    return null;
                }
            }
        }
    }
    
    // Update a workout exercise
    public boolean updateWorkoutExercise(WorkoutExercise workoutExercise) throws SQLException {
        String sql = "UPDATE workout_exercises SET workout_session_id = ?, exercise_id = ?, sets = ?, reps = ?, weight = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, workoutExercise.getWorkoutSessionId());
            pstmt.setInt(2, workoutExercise.getExerciseId());
            pstmt.setInt(3, workoutExercise.getSets());
            pstmt.setInt(4, workoutExercise.getReps());
            pstmt.setDouble(5, workoutExercise.getWeight());
            pstmt.setInt(6, workoutExercise.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Delete a workout exercise
    public boolean deleteWorkoutExercise(int id) throws SQLException {
        String sql = "DELETE FROM workout_exercises WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Delete all workout exercises for a session
    public void deleteWorkoutExercisesBySessionId(int sessionId) throws SQLException {
        String sql = "DELETE FROM workout_exercises WHERE workout_session_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        }
    }
}