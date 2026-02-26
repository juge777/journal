package com.journal.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 运行 main 方法生成 BCrypt 加密密码
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成 test 用户的密码
        String password = "****";
        String hashedPassword = encoder.encode(password);
        System.out.println("用户名: test");
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + hashedPassword);
        System.out.println();
        System.out.println("SQL 插入语句:");
        System.out.println("INSERT INTO user (username, password, created_at) VALUES");
        System.out.println("('test', '" + hashedPassword + "', NOW());");
    }
}
