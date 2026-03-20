package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ReAct 执行步骤
 *
 * 【用途说明】
 * 记录 ReAct 循环中的单一步骤
 * 每个步骤包含：Thought → Action → Observation
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReActStep {

    /**
     * 循环轮次（从 1 开始）
     */
    private Integer iteration;

    /**
     * 思考过程
     *
     * 【说明】
     * LLM 的推理过程，说明：
     * - 当前处于什么阶段
     * - 需要什么信息
     * - 为什么选择这个工具
     *
     * 【示例】
     * "用户询问薪资涨幅，我需要先分析当前收入水平"
     */
    private String thought;

    /**
     * 行动（工具名称）
     *
     * 【可选值】
     * - salary_analysis
     * - growth_analysis
     * - market_analysis
     * - risk_analysis
     */
    private String action;

    /**
     * 行动输入
     *
     * 【说明】
     * 传递给工具的参数
     * 如果工具不需要参数，则为 "N/A"
     */
    private String actionInput;

    /**
     * 观察结果
     *
     * 【说明】
     * 工具执行后返回的结果
     * 包含详细的维度分析内容
     */
    private String observation;

    /**
     * 最终答案
     *
     * 【说明】
     * 当 LLM 认为有足够信息时，会设置这个字段
     * 设置此字段后，不再执行 Action
     */
    private String finalAnswer;

    /**
     * 判断是否为最终答案步骤
     *
     * @return 如果 finalAnswer 不为空，返回 true
     */
    public boolean isFinalAnswer() {
        return finalAnswer != null && !finalAnswer.isEmpty();
    }
}
