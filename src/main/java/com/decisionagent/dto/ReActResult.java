package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ReAct 执行结果
 *
 * 【用途说明】
 * ReActAgent.process() 方法的返回值
 * 包含完整的思考循环轨迹和最终答案
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReActResult {

    /**
     * 执行步骤列表
     *
     * 【说明】
     * 记录每一轮的 Thought、Action、Observation
     */
    private List<ReActStep> steps;

    /**
     * 最终答案
     *
     * 【说明】
     * LLM 经过多轮思考后得出的最终结论
     */
    private String finalAnswer;

    /**
     * 总循环次数
     */
    private Integer totalIterations;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 是否达到最大循环次数
     */
    private Boolean maxIterationsReached;
}
