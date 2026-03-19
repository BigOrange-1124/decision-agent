package com.decisionagent.service;

import com.decisionagent.dto.ToolSelectionResponse;
import com.decisionagent.dto.UserProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工具规划服务
 *
 * 【核心职责】
 * 这个服务用 LLM 来智能判断：用户的问题需要调用哪些分析工具？
 *
 * 以前：所有工具都会被调用（固定流程）
 * 现在：LLM 根据用户问题，只调用相关的工具（智能选择）
 *
 * 【工作流程】
 * 1. 接收用户的问题和用户信息
 * 2. 构建一个"规划 Prompt"，告诉 LLM 有哪些工具可用
 * 3. 让 LLM 判断需要哪些工具
 * 4. 解析 LLM 返回的 JSON，得到工具列表
 * 5. 返回工具选择结果
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolPlanningService {

    // LLM 服务，用来调用大模型
    private final LlmService llmService;

    // JSON 解析器，用来把 LLM 返回的字符串转成 Java 对象
    private final ObjectMapper objectMapper;

    /**
     * 系统提示词（System Prompt）
     *
     * 这个 Prompt 非常重要！它告诉 LLM：
     * 1. 有哪些分析工具可以使用
     * 2. 每个工具在什么情况下使用
     * 3. 如何判断是否需要工具分析
     * 4. 必须返回严格的 JSON 格式
     */
    private static final String PLANNING_SYSTEM_PROMPT = """
            你是一个职业决策分析系统的规划专家。你的任务是根据用户的问题，判断是否需要使用分析工具，以及需要使用哪些工具。

            你可以使用以下分析工具：

            1. **salary_analysis**（收入维度分析）
               - 适用场景：用户询问薪资水平、薪资对比、涨幅空间、薪资谈判等
               - 关键词：薪资、工资、收入、待遇、钱、涨薪、降薪、薪水

            2. **growth_analysis**（成长维度分析）
               - 适用场景：用户询问技术成长、职业发展、晋升机会、能力提升等
               - 关键词：成长、进步、学技术、晋升、发展、能力、提升、技术栈

            3. **market_analysis**（市场维度分析）
               - 适用场景：用户询问市场环境、行业趋势、跳槽时机、招聘情况等
               - 关键词：市场、行情、时机、趋势、机会、招聘、公司、行业

            4. **risk_analysis**（风险维度分析）
               - 适用场景：用户询问跳槽风险、试用期、公司稳定性、裸辞风险等
               - 关键词：风险、危险、试用期、稳定、安全、倒闭、裁员、裸辞

            ## 判断规则

            ### 何时需要工具分析（needs_analysis = true）
            - 用户明确询问某个维度的问题
            - 用户的咨询涉及职业决策建议
            - 需要基于数据进行专业分析
            - 需要多维度评估

            ### 何时不需要工具分析（needs_analysis = false）
            - 用户的问候或闲聊（如"你好"、"谢谢"）
            - 已经有明确的决策，只是需要确认
            - 纯粹的信息查询（不涉及分析）
            - 表达感谢或结束对话

            ## 输出格式

            你必须严格按照以下 JSON 格式输出，不要添加任何其他文字：

            {
              "needs_analysis": true 或 false,
              "tools": ["工具名称1", "工具名称2"],
              "reasoning": "简要说明选择这些工具的原因"
            }

            ## 注意事项

            1. tools 数组中的工具名称必须是上面列出的 4 个工具之一
            2. 如果 needs_analysis 为 false，tools 应该为空数组 []
            3. 如果用户的问题不明确，优先选择最相关的工具，而不是全选
            4. reasoning 用中文简洁说明即可，20字以内
            """;

    /**
     * 【核心方法】规划需要使用哪些工具
     *
     * 步骤：
     * 1. 构建规划 Prompt（包含用户问题和用户信息）
     * 2. 调用 LLM，让它判断需要哪些工具
     * 3. 解析 LLM 返回的 JSON
     * 4. 返回工具选择结果
     *
     * @param userMessage 用户的问题（例如："我现在20k，想跳槽能涨多少？"）
     * @param profile 从用户问题中提取的信息（例如：薪资20k，工作年限未知）
     * @return 工具选择结果，包含需要执行的工具列表
     */
    public ToolSelectionResponse planTools(String userMessage, UserProfile profile) {
        log.info("【工具规划】开始分析用户问题，需要哪些工具：{}", userMessage);

        // 步骤 1：构建 Prompt
        String planningPrompt = buildPlanningPrompt(userMessage, profile);
        log.debug("【工具规划】规划 Prompt：{}", planningPrompt);

        // 步骤 2：调用 LLM
        String llmResponse = llmService.ask(PLANNING_SYSTEM_PROMPT, planningPrompt);
        log.debug("【工具规划】LLM 返回结果：{}", llmResponse);

        // 步骤 3：解析 JSON
        ToolSelectionResponse selection = parsePlanningResponse(llmResponse);

        log.info("【工具规划】规划完成 - 是否需要分析：{}，选择工具：{}，原因：{}",
                selection.getNeedsAnalysis(), selection.getTools(), selection.getReasoning());

        return selection;
    }

    /**
     * 构建"规划 Prompt"
     *
     * 这个 Prompt 会告诉 LLM：
     * - 用户问了什么问题
     * - 我们提取到了哪些用户信息
     * - 让 LLM 判断需要哪些工具
     */
    private String buildPlanningPrompt(String userMessage, UserProfile profile) {
        StringBuilder prompt = new StringBuilder();

        // 用户提供的问题
        prompt.append("【用户问题】\n");
        prompt.append(userMessage);
        prompt.append("\n\n");

        // 如果提取到了用户信息，也告诉 LLM
        if (profile != null && !profile.isEmpty()) {
            prompt.append("【用户信息】\n");
            if (profile.getMonthlySalary() != null) {
                prompt.append("• 薪资：").append(profile.getMonthlySalary()).append("k\n");
            }
            if (profile.getWorkYears() != null) {
                prompt.append("• 工作年限：").append(profile.getWorkYears()).append("年\n");
            }
            if (profile.getCurrentPosition() != null) {
                prompt.append("• 职位：").append(profile.getCurrentPosition()).append("\n");
            }
            if (profile.getTargetCompany() != null) {
                prompt.append("• 目标公司：").append(profile.getTargetCompany()).append("\n");
            }
            prompt.append("\n");
        }

        // 让 LLM 判断需要哪些工具
        prompt.append("【任务】\n");
        prompt.append("请根据用户问题，判断是否需要工具分析，以及需要使用哪些工具。\n");
        prompt.append("输出严格的 JSON 格式，不要包含任何其他内容。");

        return prompt.toString();
    }

    /**
     * 解析 LLM 返回的 JSON
     *
     * 处理步骤：
     * 1. 清理 JSON（移除可能的 markdown 代码块标记）
     * 2. 用 Jackson 把 JSON 字符串解析成 Java 对象
     * 3. 验证和修复数据（处理 null 值）
     * 4. 如果解析失败，降级为默认策略（全部分析）
     */
    private ToolSelectionResponse parsePlanningResponse(String llmResponse) {
        try {
            // 步骤 1：清理 JSON 字符串
            // LLM 有时会返回 ```json ... ``` 这种格式，需要去掉
            String cleanedResponse = cleanJsonResponse(llmResponse);

            // 步骤 2：解析 JSON
            ToolSelectionResponse response = objectMapper.readValue(
                    cleanedResponse,
                    ToolSelectionResponse.class
            );

            // 步骤 3：验证和修复数据
            if (response.getNeedsAnalysis() == null) {
                log.warn("【工具规划】needs_analysis 字段为空，默认设为 false");
                response.setNeedsAnalysis(false);
            }

            if (response.getTools() == null) {
                log.warn("【工具规划】tools 数组为空，默认设为空列表");
                response.setTools(List.of());
            }

            return response;

        } catch (JsonProcessingException e) {
            // 步骤 4：解析失败时的容错处理
            log.error("【工具规划】JSON 解析失败！LLM 返回：{}", llmResponse, e);

            // 降级策略：执行所有工具
            log.warn("【工具规划】降级为默认策略：执行所有工具");
            return new ToolSelectionResponse(
                    true,
                    List.of("salary_analysis", "growth_analysis", "market_analysis", "risk_analysis"),
                    "解析失败，使用默认全部分析"
            );
        }
    }

    /**
     * 清理 LLM 返回的 JSON 字符串
     *
     * LLM 有时会用 markdown 代码块格式返回 JSON，例如：
     * ```json
     * {...}
     * ```
     *
     * 这个方法会把 ```json 和 ``` 标记去掉，只保留纯 JSON
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();

        // 去掉开头的 ```json 或 ```
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        // 去掉结尾的 ```
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}
