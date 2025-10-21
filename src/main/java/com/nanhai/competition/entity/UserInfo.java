package com.nanhai.competition.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 用户信息表
 */
@Data
@Entity
@Table(name = "user_info")
public class UserInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;
    
    @Column(name = "employ_id", nullable = false, length = 20)
    private String employId;
    
    @Column(name = "user_eng_name", length = 50)
    private String userEngName;
    
    @Column(name = "group_type", nullable = false, length = 20)
    private String groupType;
    
    @Column(name = "sub_group", nullable = false, length = 50)
    private String subGroup;
    
    @Column(name = "is_deleted", nullable = false, length = 1)
    private String isDeleted = "N";
}
