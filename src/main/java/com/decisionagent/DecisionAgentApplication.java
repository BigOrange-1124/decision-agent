package com.decisionagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 决策 Agent 应用程序主入口
 *
 * 【项目说明】
 * 这是一个基于 Spring Boot 的 AI 职业决策助手系统
 * 通过大语言模型（LLM）为用户提供跳槽决策建议
 *
 * 【技术栈】
 * - Spring Boot 3.2.0：Web 框架
 * - LangChain4j 0.34.0：LLM 集成框架
 * - OpenAI/智谱AI：大语言模型服务
 *
 * 【启动方式】
 * 方式一：IDE 中直接运行本类的 main 方法
 * 方式二：命令行执行 mvn spring-boot:run
 * 方式三：打包后运行 java -jar decision-agent.jar
 *
 * 【默认端口】
 * 8199（可在 application.yml 中配置）
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@SpringBootApplication
public class DecisionAgentApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数（可选）
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        // Spring Boot 会自动扫描 @Component、@Service、@Controller 等注解
        // 并完成依赖注入和 Bean 的初始化
        SpringApplication.run(DecisionAgentApplication.class, args);
    }
}
