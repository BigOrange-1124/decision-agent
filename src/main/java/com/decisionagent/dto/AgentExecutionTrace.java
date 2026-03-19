package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Agent 执行轨迹
 *
 * 用来记录 Agent 的完整执行过程，方便在前端展示"模型驱动"的思考过程
 *
 * 【为什么要记录执行轨迹？】
 * - 让用户看到 LLM 是如何决策的
 * - 展示哪些工具被调用，为什么调用
 * - 提供透明的决策过程
 * - 体现"模型驱动 Agent"的特点
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionTrace {

    /**
     * 会话 ID（用于追踪）
     */
    private String conversationId;

    /**
     * 用户原始输入
     */
    private String userMessage;

    /**
     * 提取到的用户信息
     */
    private UserProfile extractedProfile;

    /**
     * LLM 工具规划结果
     * 记录 LLM 决策：需要哪些工具？为什么？
     */
    private PlanningStep planning;

    /**
     * 工具执行步骤列表
     * 记录每个工具的执行过程
     */
    private List<ToolExecutionStep> toolExecutions;

    /**
     * 是否跳过了工具分析
     * 如果是 true，说明 LLM 判断无需工具，直接回答了
     */
    private Boolean skippedAnalysis;

    /**
     * LLM 最终生成的建议
     */
    private String llmAdvice;

    /**
     * 最终响应（完整报告）
     */
    private String finalResponse;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总耗时（毫秒）
     */
    private Long totalDurationMs;

    /**
     * LLM 规划步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanningStep {
        /**
         * 是否需要工具分析
         */
        private Boolean needsAnalysis;

        /**
         * LLM 选择的工具列表
         */
        private List<String> selectedTools;

        /**
         * LLM 的选择原因
         */
        private String reasoning;

        /**
         * 原始 LLM 响应（用于调试）
         */
        private String rawLlmResponse;
    }

    /**
     * 工具执行步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolExecutionStep {
        /**
         * 工具名称（如 "salary_analysis"）
         */
        private String toolName;

        /**
         * 工具显示名称（如 "收入维度分析"）
         */
        private String displayName;

        /**
         * 执行状态：success, failed, skipped
         */
        private String status;

        /**
         * 分析结果
         */
        private String result;

        /**
         * 错误信息（如果执行失败）
         */
        private String errorMessage;

        /**
         * 执行耗时（毫秒）
         */
        private Long durationMs;
    }

    /**
     * 计算总耗时
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            totalDurationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
