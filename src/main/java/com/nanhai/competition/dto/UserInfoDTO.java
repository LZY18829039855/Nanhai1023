package com.nanhai.competition.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {
    
    private Long id;
    private String userName;
    private String employId;
    private String userEngName;
    private String groupType;
    private String subGroup;
    private String isDeleted;
}
