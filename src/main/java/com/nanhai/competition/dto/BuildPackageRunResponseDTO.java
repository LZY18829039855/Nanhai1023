package com.nanhai.competition.dto;

import lombok.Data;

import java.util.List;

/**
 * 通过job_id查询语言（包运行结果）响应DTO
 */
@Data
public class BuildPackageRunResponseDTO {

    private List<PackageRunResult> package_run_results;

    @Data
    public static class PackageRunResult {
        private String package_name;
        // 预留其他字段
    }
}



