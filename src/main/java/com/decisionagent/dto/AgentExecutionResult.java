package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 执行结果
 *
 * 包含：
 * - 最终响应内容
 * - 完整的执行轨迹
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionResult {

    /**
     * 最终响应内容
     */
    private String response;

    /**
     * 执行轨迹
     */
    private AgentExecutionTrace trace;
}
