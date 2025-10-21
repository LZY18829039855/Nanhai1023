package com.nanhai.competition.service.impl;

import com.nanhai.competition.entity.User;
import com.nanhai.competition.exception.BusinessException;
import com.nanhai.competition.exception.ResourceNotFoundException;
import com.nanhai.competition.repository.UserRepository;
import com.nanhai.competition.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(String username, String employeeId, String groupType, String subGroup, String userCategory) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmployeeId(employeeId);
        user.setGroupType(groupType);
        user.setSubGroup(subGroup);
        user.setUserCategory(userCategory);

        user = userRepository.save(user);
        log.info("创建用户成功: {}, 工号: {}", username, employeeId);
        
        return user;
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getGroupUsers(String groupType) {
        return userRepository.findByGroupType(groupType);
    }

    @Override
    public List<User> getSubGroupUsers(String subGroup) {
        return userRepository.findBySubGroup(subGroup);
    }

    @Override
    public Long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public Long getGroupUserCount(String groupType) {
        return userRepository.countByGroupType(groupType);
    }
}

