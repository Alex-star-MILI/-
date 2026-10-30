package com.gym;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gym.mapper")
public class GymApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymApplication.class, args);
        System.out.println("\n==================================");
        System.out.println("  健身房管理系统后端已启动");
        System.out.println("  API:   http://localhost:8080/api");
        System.out.println("  H2:    http://localhost:8080/api/h2-console");
        System.out.println("==================================\n");
    }
}
