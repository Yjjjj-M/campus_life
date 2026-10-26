package com.campus.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置
 * Long 序列化为 String（解决 JS 精度丢失）
 * 使用 modulesToInstall 不影响 Spring Boot 自动配置的 JavaTimeModule 等默认模块
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            // 使用 modulesToInstall 追加模块，不会覆盖 Spring Boot 默认的 JavaTimeModule
            builder.modulesToInstall(module);
        };
    }
}
