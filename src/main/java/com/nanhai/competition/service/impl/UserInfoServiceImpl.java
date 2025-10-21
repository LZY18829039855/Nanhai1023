package com.nanhai.competition.service.impl;

import com.nanhai.competition.dto.UserInfoDTO;
import com.nanhai.competition.entity.UserInfo;
import com.nanhai.competition.exception.ResourceNotFoundException;
import com.nanhai.competition.repository.UserInfoRepository;
import com.nanhai.competition.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    
    private final UserInfoRepository userInfoRepository;
    
    @Override
    @Transactional
    public UserInfoDTO createUserInfo(UserInfoDTO userInfoDTO) {
        log.info("创建用户信息: {}", userInfoDTO);
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userInfoDTO.getUserName());
        userInfo.setEmployId(userInfoDTO.getEmployId());
        userInfo.setUserEngName(userInfoDTO.getUserEngName());
        userInfo.setGroupType(userInfoDTO.getGroupType());
        userInfo.setSubGroup(userInfoDTO.getSubGroup());
        
        UserInfo savedUserInfo = userInfoRepository.save(userInfo);
        log.info("用户信息创建成功，ID: {}", savedUserInfo.getId());
        
        return convertToDTO(savedUserInfo);
    }
    
    @Override
    public UserInfoDTO getUserInfoById(Long id) {
        log.info("根据ID获取用户信息: {}", id);
        
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户信息不存在，ID: " + id));
        
        return convertToDTO(userInfo);
    }
    
    @Override
    public UserInfoDTO getUserInfoByEmployId(String employId) {
        log.info("根据工号获取用户信息: {}", employId);
        
        UserInfo userInfo = userInfoRepository.findByEmployIdAndIsDeleted(employId)
                .orElseThrow(() -> new ResourceNotFoundException("用户信息不存在，工号: " + employId));
        
        return convertToDTO(userInfo);
    }
    
    @Override
    public List<UserInfoDTO> getAllUserInfos() {
        log.info("获取所有用户信息（仅未删除的）");
        
        List<UserInfo> userInfos = userInfoRepository.findAllActive();
        return userInfos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserInfoDTO> getAllUserInfosIncludingDeleted() {
        log.info("获取所有用户信息（包含已删除的）");
        
        List<UserInfo> userInfos = userInfoRepository.findAll();
        return userInfos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserInfoDTO updateUserInfo(Long id, UserInfoDTO userInfoDTO) {
        log.info("更新用户信息，ID: {}, 数据: {}", id, userInfoDTO);
        
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户信息不存在，ID: " + id));
        
        userInfo.setUserName(userInfoDTO.getUserName());
        userInfo.setEmployId(userInfoDTO.getEmployId());
        userInfo.setUserEngName(userInfoDTO.getUserEngName());
        userInfo.setGroupType(userInfoDTO.getGroupType());
        userInfo.setSubGroup(userInfoDTO.getSubGroup());
        
        UserInfo updatedUserInfo = userInfoRepository.save(userInfo);
        log.info("用户信息更新成功，ID: {}", updatedUserInfo.getId());
        
        return convertToDTO(updatedUserInfo);
    }
    
    @Override
    @Transactional
    public void deleteUserInfo(Long id) {
        log.info("软删除用户信息，ID: {}", id);
        
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户信息不存在，ID: " + id));
        
        userInfo.setIsDeleted("Y");
        userInfoRepository.save(userInfo);
        log.info("用户信息软删除成功，ID: {}", id);
    }
    
    @Override
    @Transactional
    public void restoreUserInfo(Long id) {
        log.info("恢复用户信息，ID: {}", id);
        
        UserInfo userInfo = userInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户信息不存在，ID: " + id));
        
        userInfo.setIsDeleted("N");
        userInfoRepository.save(userInfo);
        log.info("用户信息恢复成功，ID: {}", id);
    }
    
    /**
     * 将实体转换为DTO
     */
    private UserInfoDTO convertToDTO(UserInfo userInfo) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(userInfo.getId());
        dto.setUserName(userInfo.getUserName());
        dto.setEmployId(userInfo.getEmployId());
        dto.setUserEngName(userInfo.getUserEngName());
        dto.setGroupType(userInfo.getGroupType());
        dto.setSubGroup(userInfo.getSubGroup());
        dto.setIsDeleted(userInfo.getIsDeleted());
        return dto;
    }
}
