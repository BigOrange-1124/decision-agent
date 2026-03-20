package com.decisionagent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工具选择结果数据传输对象
 *
 * 【用途说明】
 * 这个类用来接收 LLM 返回的决策结果
 * LLM 会用 JSON 格式告诉我们需要调用哪些分析工具
 *
 * 【LLM 返回的 JSON 示例】
 * {
 *   "needs_analysis": true,
 *   "tools": ["salary_analysis", "growth_analysis"],
 *   "reasoning": "用户询问薪资和成长空间"
 * }
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略 JSON 中的未知字段
public class ToolSelectionResponse {

    /**
     * 是否需要使用工具进行分析
     *
     * 【说明】
     * - 如果是 true，需要调用 tools 列表中的工具
     * - 如果是 false，LLM 会直接回答用户问题，不需要调用任何分析工具
     *
     * 【示例场景】
     * - needs_analysis = true：用户问"我现在20k，跳槽能涨多少？"
     * - needs_analysis = false：用户说"你好"、"谢谢"
     */
    @JsonProperty("needs_analysis")  // 映射 JSON 字段名（下划线命名）
    private Boolean needsAnalysis;

    /**
     * 需要执行的工具名称列表
     *
     * 【可选值】
     * - "salary_analysis"：收入维度分析
     * - "growth_analysis"：成长维度分析
     * - "market_analysis"：市场维度分析
     * - "risk_analysis"：风险维度分析
     *
     * 【示例】
     * - 用户只问薪资 → ["salary_analysis"]
     * - 用户问成长和风险 → ["growth_analysis", "risk_analysis"]
     * - 用户问综合问题 → ["salary_analysis", "growth_analysis", "market_analysis", "risk_analysis"]
     */
    @JsonProperty("tools")  // 映射 JSON 字段名
    private List<String> tools;

    /**
     * LLM 的选择原因（可选）
     *
     * 【用途】
     * - 用于记录日志和调试
     * - 帮助我们理解为什么 LLM 选择了这些工具
     *
     * 【示例】
     * - "用户询问薪资涨幅"
     * - "用户关心跳槽风险"
     * - "用户需要综合分析"
     */
    @JsonProperty("reasoning")  // 映射 JSON 字段名
    private String reasoning;

    // ==================== 辅助方法 ====================

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
