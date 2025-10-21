import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GenerateUserData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/nanhai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "12345";
        
        System.out.println("Starting to generate 60 user data...");
        
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            Connection connection = DriverManager.getConnection(url, username, password);
            
            // 清空现有数据
            PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM user_info");
            deleteStmt.executeUpdate();
            deleteStmt.close();
            System.out.println("Cleared existing user data");
            
            // Prepare insert statement
            String insertSql = "INSERT INTO user_info (user_name, employ_id, user_eng_name, group_type, sub_group) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertSql);
            
            // Generate AI group data (4 sub-groups, 10 people each)
            String[] aiSubGroups = {"Algorithm", "Model", "Data", "Engineering"};
            for (int i = 0; i < 4; i++) {
                String subGroup = aiSubGroups[i];
                for (int j = 1; j <= 10; j++) {
                    int userNum = i * 10 + j;
                    String userName = "AI User " + String.format("%02d", userNum);
                    String employId = "AI" + String.format("%03d", userNum);
                    String userEngName = "ai_user_" + String.format("%02d", userNum);
                    
                    insertStmt.setString(1, userName);
                    insertStmt.setString(2, employId);
                    insertStmt.setString(3, userEngName);
                    insertStmt.setString(4, "AI Group");
                    insertStmt.setString(5, subGroup);
                    
                    insertStmt.executeUpdate();
                    System.out.println("Created AI group user: " + userName + " (" + subGroup + ")");
                }
            }
            
            // Generate non-AI group data (2 sub-groups, 10 people each)
            String[] nonAiSubGroups = {"Testing", "Operations"};
            for (int i = 0; i < 2; i++) {
                String subGroup = nonAiSubGroups[i];
                for (int j = 1; j <= 10; j++) {
                    int userNum = i * 10 + j;
                    String userName = "Non-AI User " + String.format("%02d", userNum);
                    String employId = "NAI" + String.format("%03d", userNum);
                    String userEngName = "non_ai_user_" + String.format("%02d", userNum);
                    
                    insertStmt.setString(1, userName);
                    insertStmt.setString(2, employId);
                    insertStmt.setString(3, userEngName);
                    insertStmt.setString(4, "Non-AI Group");
                    insertStmt.setString(5, subGroup);
                    
                    insertStmt.executeUpdate();
                    System.out.println("Created non-AI group user: " + userName + " (" + subGroup + ")");
                }
            }
            
            insertStmt.close();
            connection.close();
            
            System.out.println("\nSUCCESS: Data generation completed!");
            System.out.println("Total: 60 users");
            System.out.println("- AI Group: 40 users (Algorithm 10, Model 10, Data 10, Engineering 10)");
            System.out.println("- Non-AI Group: 20 users (Testing 10, Operations 10)");
            
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: MySQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("ERROR: Database operation failed: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
        }
    }
}
