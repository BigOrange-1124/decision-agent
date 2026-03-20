package com.decisionagent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享（CORS）配置
 *
 * 【什么是 CORS？】
 * CORS（Cross-Origin Resource Sharing）是浏览器的安全机制
 * 用于限制一个域的网页向另一个域发起请求
 *
 * 【为什么需要配置？】
 * 如果前端和后端部署在不同的域名/端口上
 * 浏览器会阻止前端访问后端 API，除非后端明确允许
 *
 * 【配置说明】
 * - allowedOrigins("*")：允许所有来源（生产环境应限制具体域名）
 * - allowedMethods：允许的 HTTP 方法
 * - allowedHeaders：允许的请求头
 * - maxAge：预检请求的缓存时间（秒）
 *
 * 【安全提示】
 * 生产环境建议将 allowedOrigins 设置为具体的前端域名
 * 例如：allowedOrigins("https://example.com")
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS 映射规则
     *
     * @param registry CORS 注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有 /api/** 路径的接口应用 CORS 规则
        registry.addMapping("/api/**")
                .allowedOrigins("*")  // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowedHeaders("*")  // 允许所有请求头
                .maxAge(3600);  // 预检请求缓存 1 小时
    }
}
