package com.nanhai.competition.service;

import com.nanhai.competition.dto.UserInfoDTO;

import java.util.List;

/**
 * 用户信息服务接口
 */
public interface UserInfoService {
    
    /**
     * 创建用户信息
     */
    UserInfoDTO createUserInfo(UserInfoDTO userInfoDTO);
    
    /**
     * 根据ID获取用户信息
     */
    UserInfoDTO getUserInfoById(Long id);
    
    /**
     * 根据工号获取用户信息
     */
    UserInfoDTO getUserInfoByEmployId(String employId);
    
    /**
     * 获取所有用户信息（仅未删除的）
     */
    List<UserInfoDTO> getAllUserInfos();
    
    /**
     * 获取所有用户信息（包含已删除的）
     */
    List<UserInfoDTO> getAllUserInfosIncludingDeleted();
    
    /**
     * 更新用户信息
     */
    UserInfoDTO updateUserInfo(Long id, UserInfoDTO userInfoDTO);
    
    /**
     * 软删除用户信息
     */
    void deleteUserInfo(Long id);
    
    /**
     * 恢复已删除的用户信息
     */
    void restoreUserInfo(Long id);
}
