import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/nanhai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "12345";
        
        System.out.println("Testing database connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 尝试连接
            Connection connection = DriverManager.getConnection(url, username, password);
            
            if (connection != null) {
                System.out.println("SUCCESS: Database connection successful!");
                System.out.println("Database Product: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("Database Version: " + connection.getMetaData().getDatabaseProductVersion());
                
                // Test query
                java.sql.Statement statement = connection.createStatement();
                
                // Show tables in nanhai database
                java.sql.ResultSet resultSet = statement.executeQuery("SHOW TABLES FROM nanhai");
                System.out.println("\nTables in nanhai database:");
                while (resultSet.next()) {
                    System.out.println("- " + resultSet.getString(1));
                }
                
                // Show user_info table structure
                resultSet = statement.executeQuery("DESCRIBE nanhai.user_info");
                System.out.println("\nuser_info table structure:");
                while (resultSet.next()) {
                    System.out.println("- " + resultSet.getString(1) + " | " + resultSet.getString(2) + " | " + resultSet.getString(3) + " | " + resultSet.getString(4) + " | " + resultSet.getString(5) + " | " + resultSet.getString(6));
                }
                
                // Show data statistics
                resultSet = statement.executeQuery("SELECT COUNT(*) FROM nanhai.user_info");
                resultSet.next();
                int totalCount = resultSet.getInt(1);
                System.out.println("\nTotal users in user_info table: " + totalCount);
                
                // Show group statistics
                resultSet = statement.executeQuery("SELECT group_type, COUNT(*) FROM nanhai.user_info GROUP BY group_type");
                System.out.println("\nGroup statistics:");
                while (resultSet.next()) {
                    System.out.println("- " + resultSet.getString(1) + ": " + resultSet.getInt(2) + " users");
                }
                
                // Show sub-group statistics
                resultSet = statement.executeQuery("SELECT group_type, sub_group, COUNT(*) FROM nanhai.user_info GROUP BY group_type, sub_group ORDER BY group_type, sub_group");
                System.out.println("\nSub-group statistics:");
                while (resultSet.next()) {
                    System.out.println("- " + resultSet.getString(1) + " - " + resultSet.getString(2) + ": " + resultSet.getInt(3) + " users");
                }
                
                // Show sample data (first 5 records)
                resultSet = statement.executeQuery("SELECT * FROM nanhai.user_info LIMIT 5");
                System.out.println("\nSample data (first 5 records):");
                while (resultSet.next()) {
                    System.out.println("- ID: " + resultSet.getLong(1) + ", Name: " + resultSet.getString(2) + ", EmployID: " + resultSet.getString(3) + ", EngName: " + resultSet.getString(4) + ", GroupType: " + resultSet.getString(5) + ", SubGroup: " + resultSet.getString(6));
                }
                
                connection.close();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("ERROR: Database connection failed: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
        }
    }
}
