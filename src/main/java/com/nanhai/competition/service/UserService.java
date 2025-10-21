package com.nanhai.competition.service;

import com.nanhai.competition.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    User createUser(String username, String employeeId, String groupType, String subGroup, String userCategory);
    
    /**
     * 获取用户
     */
    User getUser(Long userId);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 获取组的所有用户
     */
    List<User> getGroupUsers(String groupType);
    
    /**
     * 获取小组的所有用户
     */
    List<User> getSubGroupUsers(String subGroup);
    
    /**
     * 获取用户总数
     */
    Long getTotalUserCount();
    
    /**
     * 获取组用户数
     */
    Long getGroupUserCount(String groupType);
}

