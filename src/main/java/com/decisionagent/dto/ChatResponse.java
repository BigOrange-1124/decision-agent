package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天响应 DTO
 *
 * 返回给前端的数据结构，包含：
 * - 最终回复
 * - Agent 执行轨迹（用于展示思考过程）
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 最终回复（完整报告）
     * 前端直接展示给用户看的内容
     */
    private String response;

    /**
     * Agent 执行轨迹
     * 前端可以用来展示"思考过程"
     *
     * 例如：
     * - LLM 规划：选择了哪些工具？
     * - 工具执行：每个工具的分析结果
     * - LLM 建议：最终的综合建议
     */
    private AgentExecutionTrace trace;

    /**
     * 会话 ID（用于追踪）
     */
    private String conversationId;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 快速创建成功响应
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
     */
    public static ChatResponse error(String errorMessage) {
        return ChatResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
