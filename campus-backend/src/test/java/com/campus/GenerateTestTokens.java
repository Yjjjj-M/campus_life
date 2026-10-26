package com.campus;

import com.campus.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 批量生成测试用户 Token 的工具类
 * 用于 JMeter 压力测试
 */
@SpringBootTest
public class GenerateTestTokens {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void generateTokens() throws IOException {
        // 配置：生成 1000 个用户的 token
        // 假设测试用户的 ID 范围是 100-1099（根据实际数据库调整）
        int startUserId = 3;
        int count = 1000;
        String outputPath = "tokens1.csv";

        FileWriter writer = new FileWriter(outputPath);

        // 写入 CSV 头部
        writer.write("token\n");

        // 生成每个用户的 token
        for (int i = 0; i < count; i++) {
            Long userId = (long) (startUserId + i);
            Integer role = 0; // 普通用户

            String token = jwtUtils.generateToken(userId, role);
            writer.write(token + "\n");

            if ((i + 1) % 100 == 0) {
                System.out.println("已生成 " + (i + 1) + " 个 token");
            }
        }

        writer.close();
        System.out.println("===========================================");
        System.out.println("Token 生成完成！");
        System.out.println("文件路径: " + outputPath);
        System.out.println("Token 数量: " + count);
        System.out.println("用户 ID 范围: " + startUserId + " - " + (startUserId + count - 1));
        System.out.println("===========================================");
    }
}
