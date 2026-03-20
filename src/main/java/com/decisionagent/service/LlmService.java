package com.decisionagent.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 大语言模型服务
 *
 * 【核心职责】
 * 封装与大语言模型（LLM）的交互逻辑
 * 提供统一的调用接口，支持多种 LLM 提供商
 *
 * 【支持的 LLM 提供商】
 * - OpenAI：GPT-4、GPT-3.5-turbo 等
 * - 智谱AI：GLM-4-flash、GLM-4-plus 等
 * - 其他兼容 OpenAI API 格式的服务
 *
 * 【工作原理】
 * 1. 使用 LangChain4j 框架创建 ChatLanguageModel 实例
 * 2. 通过配置文件读取 API Key、Base URL、模型名称等参数
 * 3. 调用 generate() 方法发送 Prompt 并获取响应
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LlmService {

    /**
     * LangChain4j 聊天模型实例
     * 用于实际调用 LLM API
     */
    private final ChatLanguageModel chatModel;

    /**
     * 构造函数 - 通过依赖注入初始化 LLM 服务
     *
     * 【参数说明】
     * 这些参数都是从 application.yml 配置文件中读取的
     * Spring Boot 会自动将配置值注入到构造函数参数中
     *
     * @param apiKey LLM 提供商的 API 密钥（必填）
     * @param baseUrl LLM API 的基础地址（可选，留空则使用 OpenAI 官方地址）
     *                智谱AI 应设置为：https://open.bigmodel.cn/api/paas/v4
     * @param modelName 要使用的模型名称（默认：gpt-4o-mini）
     *                 OpenAI：gpt-4、gpt-4o-mini、gpt-3.5-turbo
     *                 智谱AI：glm-4-flash、glm-4-plus、glm-4-0520
     * @param temperature 温度参数，控制响应的随机性（0.0-1.0）
     *                   0.0 = 完全确定性输出（适合生成代码）
     *                   1.0 = 高度随机性（适合创意写作）
     *                   0.7 = 平衡值（推荐用于对话）
     * @param maxTokens 单次响应的最大 token 数量
     *                 控制响应的最大长度，避免输出过长
     * @param timeout API 请求超时时间（单位：秒）
     *               超过这个时间会抛出异常
     */
    public LlmService(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.base-url:}") String baseUrl,
            @Value("${llm.model-name:gpt-4o-mini}") String modelName,
            @Value("${llm.temperature:0.7}") Double temperature,
            @Value("${llm.max-tokens:2000}") Integer maxTokens,
            @Value("${llm.timeout:60}") Integer timeout) {

        // 步骤 1：创建 OpenAI 聊天模型构建器
        // LangChain4j 的 OpenAiChatModel 支持所有兼容 OpenAI API 格式的服务
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)           // 设置 API 密钥
                .modelName(modelName)      // 设置模型名称
                .temperature(temperature)  // 设置温度参数
                .maxTokens(maxTokens)      // 设置最大 token 数
                .timeout(Duration.ofSeconds(timeout));  // 设置超时时间

        // 步骤 2：如果提供了自定义 Base URL，则使用它
        // 这样可以支持智谱AI等其他兼容 OpenAI API 格式的服务
        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
            log.info("【LLM 服务】使用自定义 Base URL：{}", baseUrl);
        }

        // 步骤 3：构建聊天模型实例
        this.chatModel = builder.build();
        log.info("【LLM 服务】初始化完成 - 模型：{}，提供商：{}",
                modelName, baseUrl.isEmpty() ? "OpenAI" : baseUrl);
    }

    /**
     * 【核心方法】向 LLM 发送单个提示词并获取响应
     *
     * 【使用场景】
     * - 简单的单轮问答
     * - 不需要系统提示词的场景
     * - 快速测试和调试
     *
     * @param prompt 输入提示词（用户的自然语言问题或指令）
     * @return LLM 的响应文本
     */
    public String ask(String prompt) {
        log.debug("【LLM 服务】发送提示词到 LLM，长度：{} 字符", prompt.length());

        try {
            // 调用 LLM API，生成响应
            String response = chatModel.generate(prompt);
            log.debug("【LLM 服务】收到 LLM 响应，长度：{} 字符", response.length());
            return response;
        } catch (Exception e) {
            // 捕获异常并返回友好的错误提示
            // 这样可以避免向用户暴露技术细节
            log.error("【LLM 服务】调用 LLM 失败", e);
            return "抱歉，AI 分析服务暂时不可用。请稍后重试。\n\n错误信息：" + e.getMessage();
        }
    }

    /**
     * 【核心方法】向 LLM 发送带系统提示词的消息
     *
     * 【使用场景】
     * - 需要设置 LLM 角色的场景（例如：职业顾问）
     * - 需要指定输出格式的场景
     * - 需要多轮对话的场景
     *
     * 【参数说明】
     * @param systemMessage 系统提示词，用于设置 LLM 的角色和行为模式
     *                     例如："你是一位资深的职业规划顾问..."
     * @param userMessage 用户的实际问题或请求
     * @return LLM 的响应文本
     */
    public String ask(String systemMessage, String userMessage) {
        // 将系统消息和用户消息拼接成完整的提示词
        // 注意：这是一种简化实现，生产环境建议使用 LangChain4j 的 ChatMessages
        String fullPrompt = String.format("System: %s\n\nUser: %s", systemMessage, userMessage);
        return ask(fullPrompt);
    }
}
