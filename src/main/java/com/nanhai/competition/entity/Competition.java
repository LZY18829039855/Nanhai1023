package com.nanhai.competition.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 比赛实体类
 */
@Data
@Entity
@Table(name = "competitions")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 比赛开始时间
     */
    @Column
    private LocalDateTime startTime;

    /**
     * 总用例数
     */
    @Column
    private Integer totalCases;
}

