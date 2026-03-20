package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天响应数据传输对象
 *
 * 【用途说明】
 * 这是 POST /api/chat 接口的返回值结构
 * 前端会接收到这个 JSON 对象
 *
 * 【返回内容】
 * - response：最终回复（完整报告）
 * - trace：Agent 执行轨迹（用于展示思考过程）
 * - conversationId：会话 ID（用于追踪）
 * - timestamp：时间戳
 * - success：是否成功
 * - errorMessage：错误信息（如果失败）
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@Builder  // Lombok 注解：支持构建者模式
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
public class ChatResponse {

    /**
     * 最终回复（完整报告）
     *
     * 【说明】
     * 前端直接展示给用户看的内容
     * 包含多维度分析和 LLM 综合建议
     *
     * 【示例】
     * ## 📊 职业决策分析报告
     * ...
     */
    private String response;

    /**
     * Agent 执行轨迹
     *
     * 【说明】
     * 前端可以用来展示"思考过程"
     * 包含 Agent 的完整执行流程
     *
     * 【示例内容】
     * - LLM 规划：选择了哪些工具？
     * - 工具执行：每个工具的分析结果
     * - LLM 建议：最终的综合建议
     */
    private AgentExecutionTrace trace;

    /**
     * 会话 ID
     *
     * 【用途】
     * - 用于追踪同一个会话的多次对话
     * - 前端可以用它关联多轮对话
     */
    private String conversationId;

    /**
     * 时间戳
     *
     * 【用途】
     * 记录响应生成的时刻
     */
    private LocalDateTime timestamp;

    /**
     * 是否成功
     *
     * 【说明】
     * - true：请求处理成功
     * - false：请求处理失败（此时 errorMessage 会有值）
     */
    private Boolean success;

    /**
     * 错误信息
     *
     * 【说明】
     * 当 success = false 时，这个字段包含错误详情
     * 例如："抱歉，处理您的请求时出现错误：..."
     */
    private String errorMessage;

    // ==================== 工厂方法 ====================

    /**
     * 快速创建成功响应
     *
     * @param response 最终响应内容
     * @param trace 执行轨迹
     * @param conversationId 会话 ID
     * @return ChatResponse 对象
     */
    public static ChatResponse success(String response, AgentExecutionTrace trace, String conversationId) {
        return ChatResponse.builder()
                .response(response)
                .trace(trace)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    /**
     * 快速创建失败响应
     *
     * @param errorMessage 错误信息
     * @return ChatResponse 对象
     */
    public static ChatResponse error(String errorMessage) {
        return ChatResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
