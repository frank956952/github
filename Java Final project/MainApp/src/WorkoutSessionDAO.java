import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkoutSessionDAO {
    private Connection connection;
    private WorkoutExerciseDAO workoutExerciseDAO;

    public WorkoutSessionDAO(Connection connection) {
        this.connection = connection;
        this.workoutExerciseDAO = new WorkoutExerciseDAO(connection);
    }
    
    // Create required tables if they don't exist
    public void createTables() throws SQLException {
        // Create workout_sessions table
        String createSessionsTable = "CREATE TABLE IF NOT EXISTS workout_sessions (" +
                                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                                     "start_time TIMESTAMP NOT NULL," +
                                     "end_time TIMESTAMP NOT NULL" +
                                     ")";
                                     
        // Create session_body_parts table
        String createBodyPartsTable = "CREATE TABLE IF NOT EXISTS session_body_parts (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY," +
                                      "session_id INT NOT NULL," +
                                      "body_part VARCHAR(50) NOT NULL," +
                                      "FOREIGN KEY (session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE" +
                                      ")";
                                      
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSessionsTable);
            stmt.execute(createBodyPartsTable);
        }
        
        // Create exercises and workout_exercises tables
        ExerciseDAO exerciseDAO = new ExerciseDAO(connection);
        exerciseDAO.createTable();
        workoutExerciseDAO.createTable();
        
        // Insert default exercises if none exist
        exerciseDAO.insertDefaultExercises();
    }

    // Save workout session
    public int save(WorkoutSession session) throws SQLException {
        String sql = "INSERT INTO workout_sessions (start_time, end_time) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(session.getStartTime()));
            pstmt.setTimestamp(2, Timestamp.valueOf(session.getEndTime()));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating workout session failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    session.setId(id);
                    saveBodyParts(id, session.getBodyParts());
                    
                    // Save associated exercises if any
                    if (session.getExercises() != null && !session.getExercises().isEmpty()) {
                        for (WorkoutExercise exercise : session.getExercises()) {
                            exercise.setWorkoutSessionId(id);
                            workoutExerciseDAO.addWorkoutExercise(exercise);
                        }
                    }
                    
                    return id;
                } else {
                    throw new SQLException("Creating workout session failed, no ID obtained.");
                }
            }
        }
    }

    // Save body parts for a session
    private void saveBodyParts(int sessionId, List<BodyPart> bodyParts) throws SQLException {
        if (bodyParts == null || bodyParts.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO session_body_parts (session_id, body_part) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (BodyPart part : bodyParts) {
                pstmt.setInt(1, sessionId);
                pstmt.setString(2, part.name());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // Find workout session by ID
    public WorkoutSession findById(int id) throws SQLException {
        String sql = "SELECT * FROM workout_sessions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                List<BodyPart> bodyParts = getBodyPartsForSession(id);
                
                WorkoutSession session = new WorkoutSession(id, startTime, endTime, bodyParts);
                
                // Load associated exercises
                List<WorkoutExercise> exercises = workoutExerciseDAO.getWorkoutExercisesBySessionId(id);
                session.setExercises(exercises);
                
                return session;
            }
            return null;
        }
    }

    // Get body parts for a session
    private List<BodyPart> getBodyPartsForSession(int sessionId) throws SQLException {
        List<BodyPart> bodyParts = new ArrayList<>();
        String sql = "SELECT body_part FROM session_body_parts WHERE session_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bodyParts.add(BodyPart.valueOf(rs.getString("body_part")));
            }
        }
        return bodyParts;
    }

    // Get all workout sessions
    public List<WorkoutSession> findAll() throws SQLException {
        List<WorkoutSession> sessions = new ArrayList<>();
        String sql = "SELECT id, start_time, end_time FROM workout_sessions ORDER BY start_time DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                List<BodyPart> bodyParts = getBodyPartsForSession(id);
                
                WorkoutSession session = new WorkoutSession(id, startTime, endTime, bodyParts);
                
                // Load associated exercises
                List<WorkoutExercise> exercises = workoutExerciseDAO.getWorkoutExercisesBySessionId(id);
                session.setExercises(exercises);
                
                sessions.add(session);
            }
        }
        return sessions;
    }
    
    // Update workout session
    public boolean update(WorkoutSession session) throws SQLException {
        String sql = "UPDATE workout_sessions SET start_time = ?, end_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(session.getStartTime()));
            pstmt.setTimestamp(2, Timestamp.valueOf(session.getEndTime()));
            pstmt.setInt(3, session.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Delete existing body parts and re-add
                deleteBodyParts(session.getId());
                saveBodyParts(session.getId(), session.getBodyParts());
                
                // Handle exercises - first delete existing ones
                workoutExerciseDAO.deleteWorkoutExercisesBySessionId(session.getId());
                
                // Then add the updated ones
                if (session.getExercises() != null && !session.getExercises().isEmpty()) {
                    for (WorkoutExercise exercise : session.getExercises()) {
                        exercise.setWorkoutSessionId(session.getId());
                        workoutExerciseDAO.addWorkoutExercise(exercise);
                    }
                }
                
                return true;
            }
            
            return false;
        }
    }
    
    // Delete body parts for a session
    private void deleteBodyParts(int sessionId) throws SQLException {
        String sql = "DELETE FROM session_body_parts WHERE session_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        }
    }
    
    // Delete workout session
    public boolean delete(int id) throws SQLException {
        // The cascade delete will handle deleting related body parts and exercises
        String sql = "DELETE FROM workout_sessions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // Delete all workout sessions
    public void deleteAll() throws SQLException {
        // The cascade delete will handle deleting related body parts and exercises
        String sql = "DELETE FROM workout_sessions";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // Close connection (optional)
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
