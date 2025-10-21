package com.nanhai.competition.controller;

import com.nanhai.competition.dto.ApiResponse;
import com.nanhai.competition.entity.User;
import com.nanhai.competition.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    public ApiResponse<User> createUser(
            @RequestParam String username,
            @RequestParam(required = false) String employeeId,
            @RequestParam String groupType,
            @RequestParam String subGroup,
            @RequestParam(required = false) String userCategory) {
        User user = userService.createUser(username, employeeId, groupType, subGroup, userCategory);
        return ApiResponse.success(user);
    }
    
    /**
     * 获取用户
     */
    @GetMapping("/{userId}")
    public ApiResponse<User> getUser(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return ApiResponse.success(user);
    }
    
    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ApiResponse.success(user);
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping("/all")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ApiResponse.success(users);
    }
    
    /**
     * 获取组的所有用户
     */
    @GetMapping("/group/{groupType}")
    public ApiResponse<List<User>> getGroupUsers(@PathVariable String groupType) {
        List<User> users = userService.getGroupUsers(groupType);
        return ApiResponse.success(users);
    }
    
    /**
     * 获取小组的所有用户
     */
    @GetMapping("/subgroup/{subGroup}")
    public ApiResponse<List<User>> getSubGroupUsers(@PathVariable String subGroup) {
        List<User> users = userService.getSubGroupUsers(subGroup);
        return ApiResponse.success(users);
    }
    
    /**
     * 获取用户总数
     */
    @GetMapping("/count/total")
    public ApiResponse<Long> getTotalUserCount() {
        Long count = userService.getTotalUserCount();
        return ApiResponse.success(count);
    }
    
    /**
     * 获取组用户数
     */
    @GetMapping("/count/group/{groupType}")
    public ApiResponse<Long> getGroupUserCount(@PathVariable String groupType) {
        Long count = userService.getGroupUserCount(groupType);
        return ApiResponse.success(count);
    }
    
    /**
     * 批量创建用户（使用@RequestBody确保正确的UTF-8编码）
     */
    @PostMapping("/batch")
    public ApiResponse<String> batchCreateUsers(@RequestBody List<User> users) {
        int successCount = 0;
        for (User user : users) {
            try {
                userService.createUser(
                    user.getUsername(),
                    user.getEmployeeId(),
                    user.getGroupType(),
                    user.getSubGroup(),
                    user.getUserCategory()
                );
                successCount++;
            } catch (Exception e) {
                // 如果用户已存在，跳过
                continue;
            }
        }
        return ApiResponse.success("成功导入 " + successCount + " 个用户");
    }
    
    /**
     * 从SQL文件导入测试用户
     */
    @PostMapping("/import-sql")
    public ApiResponse<String> importFromSql() {
        try {
            // 读取SQL文件（使用UTF-8编码）
            String sqlContent = new String(
                Files.readAllBytes(Paths.get("src/main/resources/data/test-users.sql")),
                StandardCharsets.UTF_8
            );
            
            // 分割SQL语句并执行
            String[] statements = sqlContent.split(";");
            int count = 0;
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    jdbcTemplate.execute(sql);
                    count++;
                }
            }
            
            // 获取导入后的用户数量
            Long totalUsers = userService.getTotalUserCount();
            Long aiUsers = userService.getGroupUserCount("AI");
            Long nonAiUsers = userService.getGroupUserCount("非AI");
            
            return ApiResponse.success(String.format(
                "SQL导入成功！执行了 %d 条语句。总用户数: %d (AI组: %d, 非AI组: %d)",
                count, totalUsers, aiUsers, nonAiUsers
            ));
        } catch (Exception e) {
            return ApiResponse.error(500, "SQL导入失败: " + e.getMessage());
        }
    }
}

