package com.decisionagent.service;

import com.decisionagent.dto.ToolSelectionResponse;
import com.decisionagent.dto.UserProfile;
import com.decisionagent.tool.AnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具执行服务
 *
 * 【核心职责】
 * 这个服务负责执行 LLM 选中的分析工具
 *
 * 【工作流程】
 * 1. 接收 LLM 返回的工具选择结果（例如：["salary_analysis", "growth_analysis"]）
 * 2. 根据工具名称，找到对应的 Java 类
 * 3. 调用每个工具的 analyze() 方法，获取分析结果
 * 4. 把所有分析结果收集起来，返回给 Agent
 *
 * 【工具名称映射】
 * "salary_analysis"   → SalaryAnalysisTool（收入分析）
 * "growth_analysis"   → GrowthAnalysisTool（成长分析）
 * "market_analysis"   → MarketAnalysisTool（市场分析）
 * "risk_analysis"     → RiskAnalysisTool（风险分析）
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutor {

    // Spring 会自动注入所有的 AnalysisTool 实现类
    // 这里会包含：SalaryAnalysisTool, GrowthAnalysisTool, MarketAnalysisTool, RiskAnalysisTool
    private final List<AnalysisTool> analysisTools;

    // ==================== 工具名称常量 ====================
    // 这些常量对应 LLM 返回的工具名称
    private static final String SALARY_ANALYSIS = "salary_analysis";    // 收入分析
    private static final String GROWTH_ANALYSIS = "growth_analysis";    // 成长分析
    private static final String MARKET_ANALYSIS = "market_analysis";    // 市场分析
    private static final String RISK_ANALYSIS = "risk_analysis";        // 风险分析

    /**
     * 【核心方法】执行选中的工具
     *
     * 步骤：
     * 1. 遍历 LLM 选择的工具列表
     * 2. 为每个工具找到对应的 Java 类
     * 3. 调用工具的 analyze() 方法
     * 4. 收集所有工具的分析结果
     * 5. 返回一个 Map，key 是工具名称，value 是分析结果
     *
     * 例如：
     * 输入：selection.tools = ["salary_analysis", "growth_analysis"]
     * 输出：
     * {
     *   "salary_analysis": "【收入维度分析】\n• 当前薪资：20k...",
     *   "growth_analysis": "【成长维度分析】\n• 职业阶段：成长阶段..."
     * }
     *
     * @param selection LLM 返回的工具选择结果（包含需要执行的工具列表）
     * @param profile 用户信息（薪资、工作年限等）
     * @param userMessage 用户的原始问题
     * @return Map 结构，key 是工具名称，value 是该工具的分析结果
     */
    public Map<String, String> executeTools(
            ToolSelectionResponse selection,
            UserProfile profile,
            String userMessage) {

        log.info("【工具执行】开始执行 {} 个工具", selection.getToolCount());

        // 用 LinkedHashMap 保持执行顺序
        Map<String, String> results = new LinkedHashMap<>();

        // 遍历每个需要执行的工具
        for (String toolName : selection.getTools()) {
            // 根据工具名称找到对应的 Java 类
            AnalysisTool tool = findToolByName(toolName);

            if (tool != null) {
                try {
                    log.debug("【工具执行】正在执行：{}", toolName);

                    // 调用工具的 analyze 方法，获取分析结果
                    String result = tool.analyze(profile, userMessage);

                    // 把结果存入 Map
                    results.put(toolName, result);

                    log.debug("【工具执行】{} 执行成功", toolName);

                } catch (Exception e) {
                    // 如果某个工具执行失败，记录错误但不影响其他工具
                    log.error("【工具执行】{} 执行失败", toolName, e);

                    // 生成错误信息
                    results.put(toolName, buildErrorMessage(tool, e));
                }
            } else {
                log.warn("【工具执行】找不到工具：{}", toolName);
            }
        }

        log.info("【工具执行】执行完成，成功 {}/{} 个",
                results.size(), selection.getToolCount());

        return results;
    }

    /**
     * 根据工具名称找到对应的 AnalysisTool 对象
     *
     * 【查找逻辑】
     * 通过类名匹配：
     * - "salary_analysis"   → 类名包含 "Salary"   → SalaryAnalysisTool
     * - "growth_analysis"   → 类名包含 "Growth"   → GrowthAnalysisTool
     * - "market_analysis"   → 类名包含 "Market"   → MarketAnalysisTool
     * - "risk_analysis"     → 类名包含 "Risk"     → RiskAnalysisTool
     *
     * @param toolName 工具名称（如 "salary_analysis"）
     * @return 对应的 AnalysisTool 对象，如果找不到返回 null
     */
    private AnalysisTool findToolByName(String toolName) {
        return analysisTools.stream()
                .filter(tool -> isToolMatch(tool, toolName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断一个工具对象是否匹配给定的工具名称
     *
     * 【匹配规则】
     * 通过类的简单名称（SimpleClassName）进行模糊匹配
     * 例如：SalaryAnalysisTool 的简单类名是 "SalaryAnalysisTool"
     *       它包含 "Salary"，所以匹配 "salary_analysis"
     *
     * @param tool 工具对象
     * @param toolName 工具名称（如 "salary_analysis"）
     * @return 如果匹配返回 true
     */
    private boolean isToolMatch(AnalysisTool tool, String toolName) {
        // 获取类的简单名称，例如 "SalaryAnalysisTool"
        String className = tool.getClass().getSimpleName();

        // 使用 switch 表达式匹配
        return switch (toolName) {
            case SALARY_ANALYSIS -> className.contains("Salary");   // 类名包含 "Salary"
            case GROWTH_ANALYSIS -> className.contains("Growth");   // 类名包含 "Growth"
            case MARKET_ANALYSIS -> className.contains("Market");   // 类名包含 "Market"
            case RISK_ANALYSIS -> className.contains("Risk");       // 类名包含 "Risk"
            default -> false;  // 未知工具名称，不匹配
        };
    }

    /**
     * 生成工具执行失败的错误信息
     *
     * 当工具执行抛出异常时，用这个方法生成友好的错误提示
     * 而不是直接抛出异常，保证其他工具能继续执行
     *
     * @param tool 执行失败的工具
     * @param e 异常对象
     * @return 格式化的错误信息字符串
     */
    private String buildErrorMessage(AnalysisTool tool, Exception e) {
        StringBuilder error = new StringBuilder();

        // 工具名称
        error.append("【").append(tool.getName()).append("】\n");

        // 错误提示
        error.append("⚠️ 分析失败：").append(e.getMessage()).append("\n");

        return error.toString();
    }

    /**
     * 格式化工具执行结果，用于展示给用户
     *
     * 【输入示例】
     * {
     *   "salary_analysis": "【收入维度分析】\n• 当前薪资：20k...",
     *   "growth_analysis": "【成长维度分析】\n• 职业阶段：..."
     * }
     *
     * 【输出示例】
     * ## 多维度分析报告
     *
     * 【收入维度分析】
     * • 当前薪资：20k
     * ...
     *
     * 【成长维度分析】
     * • 职业阶段：成长阶段
     * ...
     *
     * @param results 工具执行结果 Map
     * @return 格式化后的字符串，可以直接展示给用户
     */
    public String formatResults(Map<String, String> results) {
        if (results.isEmpty()) {
            return "未执行任何工具分析。";
        }

        StringBuilder formatted = new StringBuilder();

        // 标题
        formatted.append("## 多维度分析报告\n\n");

        // 遍历所有结果，按顺序拼接
        for (Map.Entry<String, String> entry : results.entrySet()) {
            formatted.append(entry.getValue()).append("\n\n");
        }

        return formatted.toString();
    }

    /**
     * 获取所有可用的工具名称列表
     *
     * 【用途】
     * 用于日志记录、调试、或者展示给用户看有哪些工具可用
     *
     * @return 工具名称列表
     */
    public List<String> getAvailableToolNames() {
        return List.of(SALARY_ANALYSIS, GROWTH_ANALYSIS, MARKET_ANALYSIS, RISK_ANALYSIS);
    }
}
