import java.sql.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public class WeeklyPlanDAO {
    private Connection connection;

    public WeeklyPlanDAO(Connection connection) {
        this.connection = connection;
    }
    
    // Create the weekly_plans table if it doesn't exist
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS weekly_plans (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "day_of_week VARCHAR(20) NOT NULL," +
                     "body_part VARCHAR(50) NOT NULL" +
                     ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    // 保存每週計劃
    public int save(WeeklyPlan plan) throws SQLException {
        String sql = "INSERT INTO weekly_plans (day_of_week, body_part) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Map.Entry<java.time.DayOfWeek, List<BodyPart>> entry : plan.getPlan().entrySet()) {
                for (BodyPart part : entry.getValue()) {
                    pstmt.setString(1, entry.getKey().name());
                    pstmt.setString(2, part.name());
                    pstmt.executeUpdate();
                }
            }
            // 假設返回最新的 ID（需要根據資料庫設計調整）
            return getLatestPlanId();
        }
    }

    // 取得最新的計劃 ID
    private int getLatestPlanId() throws SQLException {
        String sql = "SELECT MAX(id) FROM weekly_plans";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    // 取得當前計劃
    public WeeklyPlan findCurrentPlan() throws SQLException {
        WeeklyPlan plan = new WeeklyPlan();
        String sql = "SELECT day_of_week, body_part FROM weekly_plans";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DayOfWeek day = DayOfWeek.valueOf(rs.getString("day_of_week"));
                BodyPart part = BodyPart.valueOf(rs.getString("body_part"));
                plan.addBodyPart(day, part);
            }
        }
        return plan;
    }

    // 新增刪除所有計劃的方法
    public void deleteAllPlans() throws SQLException {
        String sql = "DELETE FROM weekly_plans";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // 關閉連接（可選）
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}