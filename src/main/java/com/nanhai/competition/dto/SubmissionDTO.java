package com.nanhai.competition.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交记录DTO
 */
@Data
public class SubmissionDTO {
    
    private Long id;
    private Long userId;
    private String branch;
    private Integer passed;
    private Integer completionTime;
    private LocalDateTime submitTime;
}

