package com.nanhai.competition.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交记录实体类
 */
@Data
@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（关联user_info表的主键）
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 分支名称
     */
    @Column(length = 100)
    private String branch;

    /**
     * 通过用例数
     */
    @Column
    private Integer passed;

    /**
     * 完成时间（秒）
     */
    @Column
    private Integer completionTime;

    /**
     * 提交时刻
     */
    @Column
    private LocalDateTime submitTime;
}

