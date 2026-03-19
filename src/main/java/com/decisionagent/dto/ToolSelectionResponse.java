package com.decisionagent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工具选择结果 DTO
 *
 * 这个类用来接收 LLM 返回的决策结果
 * LLM 会用 JSON 格式告诉我们需要调用哪些分析工具
 *
 * LLM 返回的 JSON 示例：
 * {
 *   "needs_analysis": true,
 *   "tools": ["salary_analysis", "growth_analysis"],
 *   "reasoning": "用户询问薪资和成长空间"
 * }
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolSelectionResponse {

    /**
     * 是否需要使用工具进行分析
     * 如果是 false，LLM 会直接回答用户问题，不需要调用任何分析工具
     * 例如：用户说"你好"、"谢谢"等简单对话时，这个字段就是 false
     */
    @JsonProperty("needs_analysis")
    private Boolean needsAnalysis;

    /**
     * 需要执行的工具名称列表
     * 可选值：salary_analysis, growth_analysis, market_analysis, risk_analysis
     *
     * 例如：
     * - 用户只问薪资 → ["salary_analysis"]
     * - 用户问成长和风险 → ["growth_analysis", "risk_analysis"]
     * - 用户问综合问题 → ["salary_analysis", "growth_analysis", "market_analysis", "risk_analysis"]
     */
    @JsonProperty("tools")
    private List<String> tools;

    /**
     * LLM 的选择原因（可选）
     * 用于记录日志和调试，帮助我们理解为什么 LLM 选择了这些工具
     * 例如："用户询问薪资涨幅"、"用户关心跳槽风险"等
     */
    @JsonProperty("reasoning")
    private String reasoning;

    /**
     * 判断某个工具是否需要被执行
     *
     * @param toolName 工具名称（如 "salary_analysis"）
     * @return 如果工具在已选择的列表中，返回 true
     */
    public boolean shouldUseTool(String toolName) {
        return tools != null && tools.contains(toolName);
    }

    /**
     * 获取需要执行的工具数量
     *
     * @return 工具数量（0-4个）
     */
    public int getToolCount() {
        return tools != null ? tools.size() : 0;
    }
}
