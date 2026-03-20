package com.decisionagent.agent;

import com.decisionagent.dto.AgentExecutionResult;
import com.decisionagent.dto.ReActResult;
import com.decisionagent.dto.ReActStep;
import com.decisionagent.dto.UserProfile;
import com.decisionagent.service.LlmService;
import com.decisionagent.service.UserProfileExtractor;
import com.decisionagent.tool.AnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReAct 循环 Agent
 *
 * 【核心特性】
 * 实现真正的 ReAct (Reasoning + Acting) 模式
 * 逐步思考 → 行动 → 观察 → 决策，循环直到得出最终答案
 *
 * 【ReAct 循环】
 * Thought（思考）→ Action（行动）→ Observation（观察）→ 决策
 *
 * 【与原 DecisionAgent 的区别】
 * - 原版：一次性规划所有工具，全部执行完再总结
 * - ReAct：每轮只选择一个工具，根据结果动态调整下一步
 *
 * 【执行示例】
 * Round 1:
 *   Thought: 用户询问薪资，需要先了解当前收入水平
 *   Action: salary_analysis
 *   Observation: 用户当前薪资20k，低于市场平均...
 *
 * Round 2:
 *   Thought: 了解到薪资偏低，需要分析市场时机
 *   Action: market_analysis
 *   Observation: 当前是金三银四期间，机会较多...
 *
 * Round 3:
 *   Thought: 已了解薪资和市场情况，可以给出建议
 *   Final Answer: 建议跳槽，预计涨幅20-30%...
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReActAgent {

    // ==================== 依赖服务 ====================

    /** LLM 服务 */
    private final LlmService llmService;

    /** 用户信息提取器 */
    private final UserProfileExtractor profileExtractor;

    /** 所有分析工具 */
    private final List<AnalysisTool> analysisTools;

    // ==================== 常量配置 ====================

    /**
     * 最大循环次数
     *
     * 【说明】
     * 防止 Agent 陷入死循环
     * 一般 5-10 次足够得出结论
     */
    private static final int MAX_ITERATIONS = 5;

    /**
     * ReAct 系统 Prompt
     *
     * 【设计要点】
     * 1. 明确可用工具列表
     * 2. 严格定义输出格式
     * 3. 提供示例让 LLM 理解
     * 4. 强调只能调用一个工具
     */
    private static final String REACT_SYSTEM_PROMPT = """
            你是一个职业决策专家，使用 ReAct 模式逐步分析问题。

            【可用工具】
            - salary_analysis: 分析收入情况（需要用户提供薪资）
            - growth_analysis: 分析成长空间（需要用户提供工作年限）
            - market_analysis: 分析市场环境
            - risk_analysis: 分析风险因素

            【输出格式】

            方式1：需要调用工具时
            Thought: [你的思考过程，说明为什么需要这个工具]
            Action: [工具名称]
            Action Input: [工具参数，如果没有则填 "N/A"]

            方式2：已经有足够信息时
            Final Answer: [你的最终结论和建议]

            【重要规则】
            1. 每轮只能调用一个工具
            2. 先思考，再行动
            3. 基于上一步的观察结果决定下一步
            4. 当有足够信息时，立即给出 Final Answer
            5. 不要重复调用同一个工具

            【示例】
            User: 我现在20k，想跳槽能涨多少？

            Thought: 用户询问薪资涨幅，我需要先分析当前收入水平
            Action: salary_analysis
            Action Input: 当前薪资20k

            [观察结果：用户薪资低于市场平均...]

            Thought: 了解到薪资偏低，现在需要分析市场时机
            Action: market_analysis
            Action Input: N/A

            [观察结果：当前是金三银四...]

            Thought: 已收集到足够信息，可以给出建议
            Final Answer: 根据分析，建议跳槽，预计涨幅20-30%...
            """;

    // ==================== 核心方法 ====================

    /**
     * 【核心方法】处理用户消息（ReAct 循环）
     *
     * @param userMessage 用户消息
     * @return ReAct 执行结果
     */
    public ReActResult process(String userMessage) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("【ReAct Agent】开始处理");
        log.info("用户消息：{}", userMessage);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 初始化
        List<ReActStep> steps = new ArrayList<>();
        UserProfile profile = profileExtractor.extract(userMessage);
        StringBuilder contextBuilder = new StringBuilder();

        // 构建初始上下文
        contextBuilder.append("=== 用户问题 ===\n");
        contextBuilder.append(userMessage).append("\n\n");

        if (!profile.isEmpty()) {
            contextBuilder.append("=== 用户信息 ===\n");
            if (profile.getMonthlySalary() != null) {
                contextBuilder.append("• 薪资：").append(profile.getMonthlySalary()).append("k\n");
            }
            if (profile.getWorkYears() != null) {
                contextBuilder.append("• 工作年限：").append(profile.getWorkYears()).append("年\n");
            }
            if (profile.getCurrentPosition() != null) {
                contextBuilder.append("• 职位：").append(profile.getCurrentPosition()).append("\n");
            }
            contextBuilder.append("\n");
        }

        // ===================================================================
        // ReAct 主循环
        // ===================================================================
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            log.info("【ReAct Round {}】开始", iteration + 1);

            // 步骤 1：构建 Prompt
            String prompt = buildReActPrompt(contextBuilder.toString(), steps);
            log.debug("【ReAct Round {}】Prompt:\n{}", iteration + 1, prompt);

            // 步骤 2：调用 LLM
            String llmOutput = llmService.ask(REACT_SYSTEM_PROMPT, prompt);
            log.debug("【ReAct Round {}】LLM 输出:\n{}", iteration + 1, llmOutput);

            // 步骤 3：解析 LLM 输出
            ReActStep step = parseLlmOutput(llmOutput);
            step.setIteration(iteration + 1);
            steps.add(step);

            log.info("【ReAct Round {}】Thought: {}", iteration + 1, step.getThought());

            // 步骤 4：判断是否为 Final Answer
            if (step.isFinalAnswer()) {
                log.info("【ReAct Round {}】LLM 给出最终答案，结束循环", iteration + 1);
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

                return ReActResult.builder()
                        .steps(steps)
                        .finalAnswer(step.getFinalAnswer())
                        .totalIterations(iteration + 1)
                        .success(true)
                        .build();
            }

            // 步骤 5：执行工具
            log.info("【ReAct Round {}】Action: {}", iteration + 1, step.getAction());

            String observation = executeTool(
                    step.getAction(),
                    step.getActionInput(),
                    profile,
                    userMessage
            );

            step.setObservation(observation);
            log.info("【ReAct Round {}】Observation 长度: {} 字符", iteration + 1, observation.length());

            // 步骤 6：更新上下文
            contextBuilder.append("=== Round ").append(iteration + 1).append(" ===\n");
            contextBuilder.append("Thought: ").append(step.getThought()).append("\n");
            contextBuilder.append("Action: ").append(step.getAction()).append("\n");
            contextBuilder.append("Observation: ").append(truncate(observation, 500)).append("\n\n");
        }

        // ===================================================================
        // 达到最大循环次数
        // ===================================================================
        log.warn("【ReAct Agent】达到最大循环次数 ({} 次)，强制结束", MAX_ITERATIONS);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return ReActResult.builder()
                .steps(steps)
                .finalAnswer("抱歉，经过 " + MAX_ITERATIONS + " 轮思考仍未得出结论，请提供更多信息。")
                .totalIterations(MAX_ITERATIONS)
                .success(false)
                .maxIterationsReached(true)
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建 ReAct Prompt
     */
    private String buildReActPrompt(String context, List<ReActStep> previousSteps) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(context);

        if (!previousSteps.isEmpty()) {
            prompt.append("=== 历史步骤 ===\n");
            for (ReActStep step : previousSteps) {
                prompt.append("Round ").append(step.getIteration()).append(":\n");
                prompt.append("  Thought: ").append(step.getThought()).append("\n");
                if (step.getAction() != null) {
                    prompt.append("  Action: ").append(step.getAction()).append("\n");
                    prompt.append("  Observation: ").append(truncate(step.getObservation(), 300)).append("\n");
                }
            }
            prompt.append("\n");
        }

        prompt.append("=== 请继续 ===\n");
        prompt.append("基于以上信息，请给出下一步的 Thought、Action 或 Final Answer。");

        return prompt.toString();
    }

    /**
     * 解析 LLM 输出
     */
    private ReActStep parseLlmOutput(String output) {
        ReActStep step = new ReActStep();

        // 清理输出
        output = output.trim();

        // 检查是否为 Final Answer
        if (output.contains("Final Answer:")) {
            String finalAnswer = extractAfter(output, "Final Answer:");
            step.setFinalAnswer(finalAnswer);
            return step;
        }

        // 提取 Thought
        String thought = extractAfter(output, "Thought:");
        if (thought != null && !thought.isEmpty()) {
            // 移除可能的 Action 部分
            int actionIndex = thought.indexOf("\nAction:");
            if (actionIndex != -1) {
                thought = thought.substring(0, actionIndex).trim();
            }
            step.setThought(thought);
        }

        // 提取 Action
        String action = extractAfter(output, "Action:");
        if (action != null && !action.isEmpty()) {
            // 只取第一行
            action = action.split("\n")[0].trim();
            step.setAction(action);
        }

        // 提取 Action Input
        String actionInput = extractAfter(output, "Action Input:");
        if (actionInput != null && !actionInput.isEmpty()) {
            // 只取第一行
            actionInput = actionInput.split("\n")[0].trim();
            step.setActionInput(actionInput);
        }

        return step;
    }

    /**
     * 提取指定标记后的内容
     */
    private String extractAfter(String text, String marker) {
        int index = text.indexOf(marker);
        if (index == -1) {
            return null;
        }

        String after = text.substring(index + marker.length()).trim();

        // 移除可能的 markdown 标记
        if (after.startsWith("```")) {
            int codeEnd = after.indexOf("\n", 3);
            if (codeEnd != -1) {
                after = after.substring(codeEnd + 1);
            }
        }

        return after;
    }

    /**
     * 执行工具
     */
    private String executeTool(String toolName, String actionInput, UserProfile profile, String userMessage) {
        // 查找工具
        AnalysisTool tool = findToolByName(toolName);
        if (tool == null) {
            return "错误：找不到工具 '" + toolName + "'";
        }

        try {
            // 执行工具
            return tool.analyze(profile, userMessage);
        } catch (Exception e) {
            log.error("【ReAct Agent】工具执行失败：{}", toolName, e);
            return "错误：工具执行失败 - " + e.getMessage();
        }
    }

    /**
     * 根据工具名称查找工具
     */
    private AnalysisTool findToolByName(String toolName) {
        // 标准化工具名称
        String normalizedName = toolName.toLowerCase().trim();

        return analysisTools.stream()
                .filter(tool -> {
                    String className = tool.getClass().getSimpleName();
                    return switch (normalizedName) {
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
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
