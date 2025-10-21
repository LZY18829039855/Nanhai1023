package com.nanhai.competition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UpdateUserSoftDelete {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/nanhai?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "12345";

        System.out.println("Starting to update user soft delete status...");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            
            // Add is_deleted column if not exists
            try (PreparedStatement alterStmt = connection.prepareStatement(
                    "ALTER TABLE user_info ADD COLUMN is_deleted VARCHAR(1) DEFAULT 'N' NOT NULL")) {
                alterStmt.executeUpdate();
                System.out.println("Added is_deleted column to user_info table");
            } catch (SQLException e) {
                if (e.getMessage().contains("Duplicate column name")) {
                    System.out.println("is_deleted column already exists");
                } else {
                    throw e;
                }
            }
            
            // Set all users to not deleted status
            try (PreparedStatement updateAllStmt = connection.prepareStatement(
                    "UPDATE user_info SET is_deleted = 'N'")) {
                int updatedCount = updateAllStmt.executeUpdate();
                System.out.println("Set " + updatedCount + " users to not deleted (N)");
            }
            
            // Get all user IDs
            List<Long> userIds = new ArrayList<>();
            try (PreparedStatement selectStmt = connection.prepareStatement("SELECT id FROM user_info")) {
                ResultSet resultSet = selectStmt.executeQuery();
                while (resultSet.next()) {
                    userIds.add(resultSet.getLong("id"));
                }
            }
            
            // Randomly select one user to set as deleted
            if (!userIds.isEmpty()) {
                Random random = new Random();
                Long randomUserId = userIds.get(random.nextInt(userIds.size()));
                
                try (PreparedStatement updateOneStmt = connection.prepareStatement(
                        "UPDATE user_info SET is_deleted = 'Y' WHERE id = ?")) {
                    updateOneStmt.setLong(1, randomUserId);
                    int updatedCount = updateOneStmt.executeUpdate();
                    System.out.println("Set user ID " + randomUserId + " to deleted (Y)");
                }
            }
            
            // Verify results
            try (PreparedStatement countStmt = connection.prepareStatement(
                    "SELECT is_deleted, COUNT(*) as count FROM user_info GROUP BY is_deleted")) {
                ResultSet resultSet = countStmt.executeQuery();
                System.out.println("\nFinal status:");
                while (resultSet.next()) {
                    String status = resultSet.getString("is_deleted");
                    int count = resultSet.getInt("count");
                    System.out.println("- " + status + ": " + count + " users");
                }
            }
            
            System.out.println("\nSUCCESS: Soft delete update completed!");

        } catch (SQLException e) {
            System.err.println("ERROR: Database operation failed: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
        }
    }
}
