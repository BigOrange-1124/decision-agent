package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 执行结果
 *
 * 【用途说明】
 * DecisionAgent.process() 方法的返回值
 * 包含最终响应和完整的执行轨迹
 *
 * 【返回内容】
 * - response：最终响应内容（完整报告，可直接展示给用户）
 * - trace：执行轨迹（包含 Agent 的思考过程）
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@Builder  // Lombok 注解：支持构建者模式
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
public class AgentExecutionResult {

    /**
     * 最终响应内容
     *
     * 【内容说明】
     * 完整的职业决策分析报告
     * 包含：多维度分析 + LLM 综合建议
     *
     * 【示例】
     * ## 📊 职业决策分析报告
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     *
     * **分析维度**：收入维度、成长维度
     *
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 【收入维度分析】
     * • 当前月薪：20k
     * ...
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * ## 💡 综合建议
     * 根据你的情况...
     */
    private String response;

    /**
     * 执行轨迹
     *
     * 【用途】
     * 记录 Agent 的完整执行过程
     * 前端可以用它展示"思考过程"
     *
     * 【包含内容】
     * - conversationId：会话 ID
     * - userMessage：用户原始输入
     * - extractedProfile：提取到的用户信息
     * - planning：LLM 工具规划结果
     * - toolExecutions：工具执行步骤列表
     * - llmAdvice：LLM 最终建议
     * - totalDurationMs：总耗时
     */
    private AgentExecutionTrace trace;
}
