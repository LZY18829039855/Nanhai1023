package com.nanhai.competition.controller;

import com.nanhai.competition.dto.ApiResponse;
import com.nanhai.competition.dto.UserInfoDTO;
import com.nanhai.competition.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    
    private final UserInfoService userInfoService;
    
    /**
     * 创建用户信息
     */
    @PostMapping
    public ApiResponse<UserInfoDTO> createUserInfo(@RequestBody UserInfoDTO userInfoDTO) {
        log.info("创建用户信息请求: {}", userInfoDTO);
        UserInfoDTO result = userInfoService.createUserInfo(userInfoDTO);
        return ApiResponse.success("用户信息创建成功", result);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public ApiResponse<UserInfoDTO> getUserInfoById(@PathVariable Long id) {
        log.info("获取用户信息请求，ID: {}", id);
        UserInfoDTO result = userInfoService.getUserInfoById(id);
        return ApiResponse.success("获取用户信息成功", result);
    }
    
    /**
     * 根据工号获取用户信息
     */
    @GetMapping("/employ-id/{employId}")
    public ApiResponse<UserInfoDTO> getUserInfoByEmployId(@PathVariable String employId) {
        log.info("根据工号获取用户信息请求，工号: {}", employId);
        UserInfoDTO result = userInfoService.getUserInfoByEmployId(employId);
        return ApiResponse.success("获取用户信息成功", result);
    }
    
    /**
     * 获取所有用户信息（仅未删除的）
     */
    @GetMapping
    public ApiResponse<List<UserInfoDTO>> getAllUserInfos() {
        log.info("获取所有用户信息请求");
        List<UserInfoDTO> result = userInfoService.getAllUserInfos();
        return ApiResponse.success("获取用户信息列表成功", result);
    }
    
    /**
     * 获取所有用户信息（包含已删除的）
     */
    @GetMapping("/all-including-deleted")
    public ApiResponse<List<UserInfoDTO>> getAllUserInfosIncludingDeleted() {
        log.info("获取所有用户信息请求（包含已删除的）");
        List<UserInfoDTO> result = userInfoService.getAllUserInfosIncludingDeleted();
        return ApiResponse.success("获取用户信息列表成功（包含已删除的）", result);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserInfoDTO> updateUserInfo(@PathVariable Long id, @RequestBody UserInfoDTO userInfoDTO) {
        log.info("更新用户信息请求，ID: {}, 数据: {}", id, userInfoDTO);
        UserInfoDTO result = userInfoService.updateUserInfo(id, userInfoDTO);
        return ApiResponse.success("用户信息更新成功", result);
    }
    
    /**
     * 软删除用户信息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUserInfo(@PathVariable Long id) {
        log.info("软删除用户信息请求，ID: {}", id);
        userInfoService.deleteUserInfo(id);
        return ApiResponse.success("用户信息删除成功", null);
    }
    
    /**
     * 恢复已删除的用户信息
     */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restoreUserInfo(@PathVariable Long id) {
        log.info("恢复用户信息请求，ID: {}", id);
        userInfoService.restoreUserInfo(id);
        return ApiResponse.success("用户信息恢复成功", null);
    }
}
