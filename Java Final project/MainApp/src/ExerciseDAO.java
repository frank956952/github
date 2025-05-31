import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseDAO {
    private Connection connection;
    
    public ExerciseDAO(Connection connection) {
        this.connection = connection;
    }
    
    // Create the exercises table if it doesn't exist
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS exercises (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100) NOT NULL," +
                     "body_part VARCHAR(50) NOT NULL," +
                     "type VARCHAR(50) NOT NULL" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    // Insert a new exercise
    public int addExercise(Exercise exercise) throws SQLException {
        String sql = "INSERT INTO exercises (name, body_part, type) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, exercise.getName());
            pstmt.setString(2, exercise.getBodyPart().toString());
            pstmt.setString(3, exercise.getType().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating exercise failed, no ID obtained.");
                }
            }
        }
    }
    
    // Get all exercises
    public List<Exercise> getAllExercises() throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM exercises";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Exercise exercise = new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    BodyPart.valueOf(rs.getString("body_part")),
                    Exercise.ExerciseType.valueOf(rs.getString("type"))
                );
                exercises.add(exercise);
            }
        }
        
        return exercises;
    }
    
    // Get exercises by body part
    public List<Exercise> getExercisesByBodyPart(BodyPart bodyPart) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM exercises WHERE body_part = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bodyPart.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Exercise exercise = new Exercise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        bodyPart,
                        Exercise.ExerciseType.valueOf(rs.getString("type"))
                    );
                    exercises.add(exercise);
                }
            }
        }
        
        return exercises;
    }
    
    // Get an exercise by id
    public Exercise getExerciseById(int id) throws SQLException {
        String sql = "SELECT * FROM exercises WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Exercise(
                        rs.getInt("id"),
                        rs.getString("name"),
                        BodyPart.valueOf(rs.getString("body_part")),
                        Exercise.ExerciseType.valueOf(rs.getString("type"))
                    );
                } else {
                    return null;
                }
            }
        }
    }
    
    // Delete an exercise by id
    public boolean deleteExercise(int id) throws SQLException {
        String sql = "DELETE FROM exercises WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Update an exercise
    public boolean updateExercise(Exercise exercise) throws SQLException {
        String sql = "UPDATE exercises SET name = ?, body_part = ?, type = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, exercise.getName());
            pstmt.setString(2, exercise.getBodyPart().toString());
            pstmt.setString(3, exercise.getType().toString());
            pstmt.setInt(4, exercise.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    // Insert default exercises if they don't exist
    public void insertDefaultExercises() throws SQLException {
        // Check if exercises already exist
        String checkSql = "SELECT COUNT(*) FROM exercises";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) > 0) {
                // Exercises already exist, no need to insert defaults
                return;
            }
        }
          // Define default exercises for each body part
        Map<BodyPart, List<String[]>> defaultExercises = new HashMap<>();
        
        // Chest exercises
        defaultExercises.put(BodyPart.CHEST, new ArrayList<>());
        defaultExercises.get(BodyPart.CHEST).add(new String[]{"Barbell Bench Press", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.CHEST).add(new String[]{"Dumbbell Bench Press", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.CHEST).add(new String[]{"Incline Bench Press", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.CHEST).add(new String[]{"Chest Fly", "MACHINE"});
        defaultExercises.get(BodyPart.CHEST).add(new String[]{"Push-up", "FREE_WEIGHT"});
        
        // Back exercises
        defaultExercises.put(BodyPart.BACK, new ArrayList<>());
        defaultExercises.get(BodyPart.BACK).add(new String[]{"Pull-up", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.BACK).add(new String[]{"Lat Pulldown", "MACHINE"});
        defaultExercises.get(BodyPart.BACK).add(new String[]{"Barbell Row", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.BACK).add(new String[]{"Dumbbell Row", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.BACK).add(new String[]{"Deadlift", "FREE_WEIGHT"});
          // Legs exercises
        defaultExercises.put(BodyPart.LEGS, new ArrayList<>());
        defaultExercises.get(BodyPart.LEGS).add(new String[]{"Squat", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.LEGS).add(new String[]{"Leg Press", "MACHINE"});
        defaultExercises.get(BodyPart.LEGS).add(new String[]{"Leg Extension", "MACHINE"});
        defaultExercises.get(BodyPart.LEGS).add(new String[]{"Leg Curl", "MACHINE"});
        defaultExercises.get(BodyPart.LEGS).add(new String[]{"Calf Raise", "MACHINE"});
        
        // Shoulders exercises
        defaultExercises.put(BodyPart.SHOULDERS, new ArrayList<>());
        defaultExercises.get(BodyPart.SHOULDERS).add(new String[]{"Overhead Press", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.SHOULDERS).add(new String[]{"Lateral Raise", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.SHOULDERS).add(new String[]{"Front Raise", "FREE_WEIGHT"});
        defaultExercises.get(BodyPart.SHOULDERS).add(new String[]{"Reverse Fly", "MACHINE"});
        defaultExercises.get(BodyPart.SHOULDERS).add(new String[]{"Shrug", "FREE_WEIGHT"});
        
        // Arms exercises
        defaultExercises.put(BodyPart.ARMS, new ArrayList<>());
        defaultExercises.get(BodyPart.ARMS).add(new String[]{"Bicep Curl", "STRENGTH"});
        defaultExercises.get(BodyPart.ARMS).add(new String[]{"Tricep Extension", "STRENGTH"});
        defaultExercises.get(BodyPart.ARMS).add(new String[]{"Hammer Curl", "STRENGTH"});
        defaultExercises.get(BodyPart.ARMS).add(new String[]{"Skull Crusher", "STRENGTH"});
        defaultExercises.get(BodyPart.ARMS).add(new String[]{"Dip", "CALISTHENICS"});
        
        // Core exercises
        defaultExercises.put(BodyPart.CORE, new ArrayList<>());
        defaultExercises.get(BodyPart.CORE).add(new String[]{"Crunch", "CALISTHENICS"});
        defaultExercises.get(BodyPart.CORE).add(new String[]{"Plank", "CALISTHENICS"});
        defaultExercises.get(BodyPart.CORE).add(new String[]{"Russian Twist", "CALISTHENICS"});
        defaultExercises.get(BodyPart.CORE).add(new String[]{"Leg Raise", "CALISTHENICS"});
        defaultExercises.get(BodyPart.CORE).add(new String[]{"Side Plank", "CALISTHENICS"});
        
        // Cardio exercises
        defaultExercises.put(BodyPart.CARDIO, new ArrayList<>());
        defaultExercises.get(BodyPart.CARDIO).add(new String[]{"Running", "CARDIO"});
        defaultExercises.get(BodyPart.CARDIO).add(new String[]{"Cycling", "CARDIO"});
        defaultExercises.get(BodyPart.CARDIO).add(new String[]{"Jump Rope", "CARDIO"});
        defaultExercises.get(BodyPart.CARDIO).add(new String[]{"Rowing", "CARDIO"});
        defaultExercises.get(BodyPart.CARDIO).add(new String[]{"Elliptical", "CARDIO"});
        
        // Insert all default exercises
        for (Map.Entry<BodyPart, List<String[]>> entry : defaultExercises.entrySet()) {
            BodyPart bodyPart = entry.getKey();
            List<String[]> exercises = entry.getValue();
            
            for (String[] exerciseData : exercises) {
                Exercise exercise = new Exercise();
                exercise.setName(exerciseData[0]);
                exercise.setBodyPart(bodyPart);
                exercise.setType(Exercise.ExerciseType.valueOf(exerciseData[1]));
                
                addExercise(exercise);
            }
        }
    }
}
