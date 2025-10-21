package com.nanhai.competition.entity;

import javax.persistence.*;
import lombok.Data;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 工号
     */
    @Column(length = 50)
    private String employeeId;

    /**
     * 所属组（AI或非AI）
     */
    @Column(nullable = false, length = 20)
    private String groupType;

    /**
     * 所属小组编号（如AI-1, AI-2, 非AI-1等）
     */
    @Column(nullable = false, length = 20)
    private String subGroup;

    /**
     * 用户类别
     */
    @Column(length = 50)
    private String userCategory;

    /**
     * 头像URL
     */
    @Column(length = 255)
    private String avatar;
}

