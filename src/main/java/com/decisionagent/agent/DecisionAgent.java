package com.decisionagent.agent;

import com.decisionagent.dto.*;
import com.decisionagent.service.LlmService;
import com.decisionagent.service.ToolExecutor;
import com.decisionagent.service.ToolPlanningService;
import com.decisionagent.service.UserProfileExtractor;
import com.decisionagent.tool.AnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 决策 Agent（模型驱动版本）
 *
 * 【核心职责】
 * 这是整个系统的大脑，负责协调各个服务，完成智能的职业决策分析
 *
 * 【架构特点】
 * 以前（固定流程）：所有工具都会被调用，不管用户是否需要
 * 现在（模型驱动）：LLM 根据用户问题，智能选择需要的工具
 *
 * 【完整执行流程】
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 用户输入："我现在20k，想跳槽能涨多少？"                      │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 1：提取用户信息                                         │
 * │ UserProfileExtractor.extract()                              │
 * │ 结果：{ salary: 20k, years: null, position: null }          │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 2：LLM 规划工具                                         │
 * │ ToolPlanningService.planTools()                             │
 * │ Prompt: "用户询问薪资...需要哪些工具？"                      │
 * │ 结果：{ needs_analysis: true, tools: ["salary_analysis"] }  │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 3：判断是否需要工具分析                                 │
 * │ if (!selection.getNeedsAnalysis())                          │
 * │ → 如果不需要，直接生成友好回复（例如：问候语）                │
 * │ → 如果需要，继续下一步                                       │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 4：执行选中的工具                                       │
 * │ ToolExecutor.executeTools()                                 │
 * │ 执行：SalaryAnalysisTool.analyze()                          │
 * │ 结果：{                                                     │
 * │   "salary_analysis": "【收入维度分析】\n• 当前薪资：20k..."  │
 * │ }                                                            │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 5：格式化工具结果                                       │
 * │ ToolExecutor.formatResults()                                │
 * │ 结果："## 多维度分析报告\n\n【收入维度分析】..."              │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 6：LLM 综合生成建议                                     │
 * │ LlmService.ask()                                            │
 * │ Prompt: "用户询问薪资...以下是分析结果，请给建议..."          │
 * │ 结果："根据你的薪资情况...建议跳槽，涨幅预计20-30%..."        │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 步骤 7：组合完整响应                                         │
 * │ buildFullResponse()                                         │
 * │ 结果：详细分析 + LLM 建议的完整报告                          │
 * └────────────────────────┬────────────────────────────────────┘
 *                          │
 *                          ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 返回给用户                                                   │
 * │ "## 📊 职业决策分析报告..."                                  │
 * └─────────────────────────────────────────────────────────────┘
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionAgent {

    // ==================== 依赖的服务 ====================

    /** 所有分析工具列表（Spring 自动注入） */
    private final List<com.decisionagent.tool.AnalysisTool> analysisTools;

    /** 工具执行器：负责执行 LLM 选中的分析工具 */
    private final ToolExecutor toolExecutor;

    /** LLM 服务：负责调用大模型生成回复 */
    private final LlmService llmService;

    /** 工具规划服务：负责让 LLM 决定需要哪些工具 */
    private final ToolPlanningService planningService;

    /** 用户信息提取器：从用户问题中提取关键信息 */
    private final UserProfileExtractor profileExtractor;

    /**
     * 系统提示词（System Prompt）
     *
     * 这个 Prompt 定义了 LLM 的角色和输出风格
     * 当 LLM 生成最终建议时，会参考这个 Prompt
     */
    private static final String SYSTEM_PROMPT = """
            你是一位资深的职业规划顾问，拥有10年+的互联网行业从业经验。
            你擅长从收入、成长、市场、风险四个维度为求职者提供专业建议。

            你的输出风格：
            1. 客观中立，基于事实分析
            2. 结构清晰，分点论述
            3. 给出具体可执行的建议
            4. 语气温和但有专业性
            5. 使用emoji增强可读性

            请基于分析结果，为用户提供综合性的跳槽建议。
            """;

    /**
     * 【核心入口方法】处理用户消息
     *
     * 这是整个 Agent 的入口，接收用户的自然语言输入，返回完整的分析报告
     *
     * 【返回值说明】
     * 返回 AgentExecutionResult，包含：
     * - response: 最终响应内容
     * - trace: 完整的执行轨迹（用于前端展示思考过程）
     *
     * @param userMessage 用户的自然语言问题（例如："我现在20k，想跳槽能涨多少？"）
     * @return 执行结果，包含响应内容和执行轨迹
     */
    public AgentExecutionResult process(String userMessage) {
        // ===================================================================
        // 初始化执行轨迹
        // ===================================================================
        String conversationId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();

        AgentExecutionTrace trace = AgentExecutionTrace.builder()
                .conversationId(conversationId)
                .userMessage(userMessage)
                .startTime(startTime)
                .toolExecutions(new ArrayList<>())
                .build();

        log.info("【Agent】开始处理用户消息：{}", userMessage);

        try {
            // ===================================================================
            // 步骤 1：提取用户信息
            // ===================================================================
            // 从用户的自然语言中提取关键信息：薪资、工作年限、职位等
            UserProfile profile = profileExtractor.extract(userMessage);
            trace.setExtractedProfile(profile);
            log.info("【Agent】提取到的用户信息 - 薪资：{}k，年限：{}年，职位：{}",
                    profile.getMonthlySalary(), profile.getWorkYears(), profile.getCurrentPosition());

            // ===================================================================
            // 步骤 2：LLM 规划工具
            // ===================================================================
            // 让 LLM 判断：这个问题需要调用哪些分析工具？
            // LLM 会返回一个 JSON，告诉我们需要哪些工具
            ToolSelectionResponse selection = planningService.planTools(userMessage, profile);
            log.info("【Agent】LLM 规划结果 - 需要分析：{}，工具：{}，原因：{}",
                    selection.getNeedsAnalysis(), selection.getTools(), selection.getReasoning());

            // 记录规划步骤到轨迹
            AgentExecutionTrace.PlanningStep planningStep = AgentExecutionTrace.PlanningStep.builder()
                    .needsAnalysis(selection.getNeedsAnalysis())
                    .selectedTools(selection.getTools())
                    .reasoning(selection.getReasoning())
                    .build();
            trace.setPlanning(planningStep);

            // ===================================================================
            // 步骤 3：判断是否需要工具分析
            // ===================================================================
            // 如果 LLM 判断不需要工具（例如：问候语），直接生成回复
            if (!selection.getNeedsAnalysis() || selection.getToolCount() == 0) {
                log.info("【Agent】无需工具分析，直接生成回复");
                trace.setSkippedAnalysis(true);

                String directResponse = generateDirectResponse(userMessage, profile);
                trace.setLlmAdvice(directResponse);
                trace.setFinalResponse(directResponse);
                trace.setEndTime(LocalDateTime.now());
                trace.calculateDuration();

                return AgentExecutionResult.builder()
                        .response(directResponse)
                        .trace(trace)
                        .build();
            }

            trace.setSkippedAnalysis(false);

            // ===================================================================
            // 步骤 4：执行选中的工具
            // ===================================================================
            // 根据 LLM 的选择，动态执行需要的分析工具
            Map<String, String> toolResults = executeToolsWithTrace(selection, profile, userMessage, trace);

            // ===================================================================
            // 步骤 5：格式化工具执行结果
            // ===================================================================
            // 把各个工具的输出结果，拼接成统一的格式
            String rawAnalysis = toolExecutor.formatResults(toolResults);
            log.debug("【Agent】工具执行完成，分析结果长度：{}", rawAnalysis.length());

            // ===================================================================
            // 步骤 6：LLM 综合生成建议
            // ===================================================================
            // 把工具的分析结果交给 LLM，让它生成最终的综合建议
            String finalPrompt = buildSynthesisPrompt(userMessage, profile, rawAnalysis, selection);
            log.debug("【Agent】调用 LLM 生成综合建议");

            String llmResponse = llmService.ask(finalPrompt);
            trace.setLlmAdvice(llmResponse);

            // ===================================================================
            // 步骤 7：组合完整响应
            // ===================================================================
            // 把详细分析和 LLM 建议组合成最终的报告
            String fullResponse = buildFullResponse(rawAnalysis, llmResponse, selection);
            trace.setFinalResponse(fullResponse);

            // 完成轨迹记录
            trace.setEndTime(LocalDateTime.now());
            trace.calculateDuration();

            log.info("【Agent】处理完成，会话 ID：{}，耗时：{}ms", conversationId, trace.getTotalDurationMs());

            return AgentExecutionResult.builder()
                    .response(fullResponse)
                    .trace(trace)
                    .build();

        } catch (Exception e) {
            log.error("【Agent】处理失败", e);
            trace.setEndTime(LocalDateTime.now());
            trace.calculateDuration();

            // 返回错误响应
            String errorResponse = "抱歉，处理您的请求时出现错误：" + e.getMessage();
            return AgentExecutionResult.builder()
                    .response(errorResponse)
                    .trace(trace)
                    .build();
        }
    }

    /**
     * 执行工具并记录轨迹
     *
     * @param selection 工具选择结果
     * @param profile 用户信息
     * @param userMessage 用户消息
     * @param trace 执行轨迹
     * @return 工具执行结果 Map
     */
    private Map<String, String> executeToolsWithTrace(
            ToolSelectionResponse selection,
            UserProfile profile,
            String userMessage,
            AgentExecutionTrace trace) {

        Map<String, String> results = new LinkedHashMap<>();

        for (String toolName : selection.getTools()) {
            long toolStartTime = System.currentTimeMillis();

            // 使用 ToolExecutor 来执行工具
            com.decisionagent.tool.AnalysisTool tool = findToolByName(toolName);
            if (tool == null) {
                log.warn("【Agent】找不到工具：{}", toolName);
                continue;
            }

            try {
                log.debug("【Agent】正在执行工具：{}", toolName);

                // 执行工具
                String result = tool.analyze(profile, userMessage);
                results.put(toolName, result);

                long duration = System.currentTimeMillis() - toolStartTime;

                // 记录成功的执行步骤
                AgentExecutionTrace.ToolExecutionStep step = AgentExecutionTrace.ToolExecutionStep.builder()
                        .toolName(toolName)
                        .displayName(tool.getName())
                        .status("success")
                        .result(result)
                        .durationMs(duration)
                        .build();

                trace.getToolExecutions().add(step);

                log.debug("【Agent】工具 {} 执行成功，耗时：{}ms", toolName, duration);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - toolStartTime;

                log.error("【Agent】工具 {} 执行失败", toolName, e);

                // 记录失败的执行步骤
                AgentExecutionTrace.ToolExecutionStep step = AgentExecutionTrace.ToolExecutionStep.builder()
                        .toolName(toolName)
                        .displayName(tool.getName())
                        .status("failed")
                        .errorMessage(e.getMessage())
                        .durationMs(duration)
                        .build();

                trace.getToolExecutions().add(step);
            }
        }

        return results;
    }

    /**
     * 根据工具名称找到对应的工具
     */
    private com.decisionagent.tool.AnalysisTool findToolByName(String toolName) {
        // 需要注入所有的 AnalysisTool
        // 这里我需要一个不同的方法来获取工具列表
        // 暂时使用 ToolExecutor 的逻辑
        List<AnalysisTool> allTools = getAllTools();
        return allTools.stream()
                .filter(tool -> {
                    String className = tool.getClass().getSimpleName();
                    return switch (toolName) {
                        case "salary_analysis" -> className.contains("Salary");
                        case "growth_analysis" -> className.contains("Growth");
                        case "market_analysis" -> className.contains("Market");
                        case "risk_analysis" -> className.contains("Risk");
                        default -> false;
                    };
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有工具
     */
    private List<com.decisionagent.tool.AnalysisTool> getAllTools() {
        return analysisTools;
    }

    /**
     * 生成直接回复（无需工具分析）
     */
    private String generateDirectResponse(String userMessage, UserProfile profile) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(SYSTEM_PROMPT).append("\n\n");
        prompt.append("【用户咨询】\n");
        prompt.append(userMessage).append("\n\n");

        if (profile != null && !profile.isEmpty()) {
            prompt.append("【用户背景】\n");
            if (profile.getMonthlySalary() != null) {
                prompt.append("• 当前月薪：").append(profile.getMonthlySalary()).append("k\n");
            }
            if (profile.getWorkYears() != null) {
                prompt.append("• 工作年限：").append(profile.getWorkYears()).append("年\n");
            }
            if (profile.getCurrentPosition() != null) {
                prompt.append("• 当前职位：").append(profile.getCurrentPosition()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("【任务要求】\n");
        prompt.append("请直接回答用户的问题，给出友好、专业的回复。\n");
        prompt.append("如果需要更多信息才能给出建议，请礼貌地询问。");

        String response = llmService.ask(prompt.toString());

        return "## 💡 职业顾问回复\n\n" + response;
    }

    /**
     * 构建"综合建议 Prompt"
     */
    private String buildSynthesisPrompt(
            String userMessage,
            UserProfile profile,
            String analysis,
            ToolSelectionResponse selection) {

        StringBuilder prompt = new StringBuilder();

        prompt.append(SYSTEM_PROMPT).append("\n\n");

        prompt.append("【用户咨询】\n");
        prompt.append(userMessage).append("\n\n");

        prompt.append("【用户信息提取】\n");
        if (profile.getMonthlySalary() != null) {
            prompt.append("• 当前月薪：").append(profile.getMonthlySalary()).append("k\n");
        }
        if (profile.getWorkYears() != null) {
            prompt.append("• 工作年限：").append(profile.getWorkYears()).append("年\n");
        }
        if (profile.getCurrentPosition() != null) {
            prompt.append("• 当前职位：").append(profile.getCurrentPosition()).append("\n");
        }
        if (profile.getTargetCompany() != null) {
            prompt.append("• 目标公司：").append(profile.getTargetCompany()).append("\n");
        }
        prompt.append("\n");

        prompt.append("【已执行的分析维度】\n");
        for (String tool : selection.getTools()) {
            String dimensionName = getDimensionName(tool);
            prompt.append("• ").append(dimensionName).append("\n");
        }
        prompt.append("\n");

        prompt.append("【多维度分析结果】\n");
        prompt.append(analysis).append("\n\n");

        prompt.append("【任务要求】\n");
        prompt.append("请基于以上分析结果，给用户一个清晰的结论和建议。要求：\n");
        prompt.append("1. 先给出明确结论（建议跳槽/不建议跳槽/视情况而定）\n");
        prompt.append("2. 用简练的语言总结3-5个关键理由\n");
        prompt.append("3. 给出具体的下一步行动建议\n");
        prompt.append("4. 如果信息不足，指出需要补充的信息\n");

        return prompt.toString();
    }

    /**
     * 构建完整的响应
     */
    private String buildFullResponse(
            String detailedAnalysis,
            String summary,
            ToolSelectionResponse selection) {

        StringBuilder response = new StringBuilder();

        response.append("## 📊 职业决策分析报告\n\n");
        response.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        response.append("**分析维度**：");
        if (selection.getTools() != null && !selection.getTools().isEmpty()) {
            for (int i = 0; i < selection.getTools().size(); i++) {
                String tool = selection.getTools().get(i);
                response.append(getDimensionName(tool));
                if (i < selection.getTools().size() - 1) {
                    response.append("、");
                }
            }
        }
        response.append("\n\n");

        response.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        response.append(detailedAnalysis);

        response.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        response.append("## 💡 综合建议\n\n");
        response.append(summary);

        return response.toString();
    }

    /**
     * 将工具名称转换为中文维度名称
     */
    private String getDimensionName(String toolName) {
        return switch (toolName) {
        case "salary_analysis" -> "收入维度";
        case "growth_analysis" -> "成长维度";
        case "market_analysis" -> "市场维度";
        case "risk_analysis" -> "风险维度";
        default -> toolName;
        };
    }
}
