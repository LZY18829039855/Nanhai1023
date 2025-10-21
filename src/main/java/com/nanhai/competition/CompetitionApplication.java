package com.nanhai.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI编程大赛后端应用启动类
 * 
 * @author nanhai
 * @date 2025-01-18
 */
@SpringBootApplication
@EnableScheduling
public class CompetitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  AI Competition Backend Started Successfully!");
        System.out.println("  Access URL: http://localhost:8080/api");
        System.out.println("========================================\n");
    }
}

