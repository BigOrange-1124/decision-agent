package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 执行轨迹
 *
 * 【核心职责】
 * 用来记录 Agent 的完整执行过程，方便在前端展示"模型驱动"的思考过程
 *
 * 【为什么要记录执行轨迹？】
 * - 让用户看到 LLM 是如何决策的
 * - 展示哪些工具被调用，为什么调用
 * - 提供透明的决策过程
 * - 体现"模型驱动 Agent"的特点
 *
 * 【执行流程】
 * 1. 用户输入 → 2. 提取信息 → 3. LLM 规划 → 4. 执行工具 → 5. LLM 建议 → 6. 完成响应
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@Builder  // Lombok 注解：支持构建者模式
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
public class AgentExecutionTrace {

    /**
     * 会话 ID（用于追踪）
     *
     * 【用途】
     * - 唯一标识一次完整的对话
     * - 可用于日志查询和问题排查
     */
    private String conversationId;

    /**
     * 用户原始输入
     *
     * 【示例】
     * "我现在20k，工作3年，想跳槽能涨多少？"
     */
    private String userMessage;

    /**
     * 提取到的用户信息
     *
     * 【说明】
     * 从用户消息中提取的结构化数据
     * 例如：薪资 20k，工作年限 3年
     */
    private UserProfile extractedProfile;

    /**
     * LLM 工具规划结果
     *
     * 【说明】
     * 记录 LLM 决策：需要哪些工具？为什么？
     *
     * 【示例】
     * - needsAnalysis: true
     * - selectedTools: ["salary_analysis", "growth_analysis"]
     * - reasoning: "用户询问薪资涨幅和成长空间"
     */
    private PlanningStep planning;

    /**
     * 工具执行步骤列表
     *
     * 【说明】
     * 记录每个工具的执行过程和结果
     *
     * 【示例】
     * [
     *   { toolName: "salary_analysis", status: "success", result: "..." },
     *   { toolName: "growth_analysis", status: "success", result: "..." }
     * ]
     */
    private List<ToolExecutionStep> toolExecutions;

    /**
     * 是否跳过了工具分析
     *
     * 【说明】
     * - 如果是 true，说明 LLM 判断无需工具，直接回答了
     * - 例如：用户说"你好"，LLM 会直接回复问候语
     */
    private Boolean skippedAnalysis;

    /**
     * LLM 最终生成的建议
     *
     * 【说明】
     * LLM 基于工具分析结果生成的综合建议
     * 不包含详细的工具输出，只保留核心建议
     */
    private String llmAdvice;

    /**
     * 最终响应（完整报告）
     *
     * 【说明】
     * 完整的报告内容
     * 包含：详细分析 + LLM 建议
     */
    private String finalResponse;

    /**
     * 开始时间
     *
     * 【用途】
     * 记录 Agent 开始处理的时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     *
     * 【用途】
     * 记录 Agent 完成处理的时间
     */
    private LocalDateTime endTime;

    /**
     * 总耗时（毫秒）
     *
     * 【说明】
     * 从开始到结束的总耗时
     * 用于性能监控和优化
     */
    private Long totalDurationMs;

    // ==================== 内部类 ====================

    /**
     * LLM 规划步骤
     *
     * 【说明】
     * 记录 LLM 的工具规划决策
     */
    @Data  // Lombok 注解：自动生成 getter/setter
    @Builder  // Lombok 注解：支持构建者模式
    @NoArgsConstructor  // Lombok 注解：生成无参构造函数
    @AllArgsConstructor  // Lombok 注解：生成全参构造函数
    public static class PlanningStep {
        /**
         * 是否需要工具分析
         */
        private Boolean needsAnalysis;

        /**
         * LLM 选择的工具列表
         * 例如：["salary_analysis", "growth_analysis"]
         */
        private List<String> selectedTools;

        /**
         * LLM 的选择原因
         * 例如："用户询问薪资涨幅"
         */
        private String reasoning;

        /**
         * 原始 LLM 响应（用于调试）
         *
         * 【说明】
         * 保存 LLM 返回的原始 JSON 字符串
         * 用于调试和日志记录
         */
        private String rawLlmResponse;
    }

    /**
     * 工具执行步骤
     *
     * 【说明】
     * 记录单个工具的执行情况
     */
    @Data  // Lombok 注解：自动生成 getter/setter
    @Builder  // Lombok 注解：支持构建者模式
    @NoArgsConstructor  // Lombok 注解：生成无参构造函数
    @AllArgsConstructor  // Lombok 注解：生成全参构造函数
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
         * 执行状态
         * - "success"：执行成功
         * - "failed"：执行失败
         * - "skipped"：跳过执行
         */
        private String status;

        /**
         * 分析结果
         *
         * 【说明】
         * 工具执行成功后返回的分析内容
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

    // ==================== 辅助方法 ====================

    /**
     * 计算总耗时
     *
     * 【说明】
     * 根据 startTime 和 endTime 计算 totalDurationMs
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            totalDurationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
