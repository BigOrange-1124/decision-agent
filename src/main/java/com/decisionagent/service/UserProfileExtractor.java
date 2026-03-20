package com.decisionagent.service;

import com.decisionagent.dto.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户信息提取服务
 *
 * 【核心职责】
 * 从用户的自然语言输入中提取结构化信息
 * 将非结构化的文本转换为可用的数据字段
 *
 * 【提取的信息】
 * - 月薪（monthlySalary）：例如 "20k" → 20
 * - 工作年限（workYears）：例如 "3年" → 3
 * - 职位（currentPosition）：例如 "Java开发"
 * - 主要关注点（mainConcern）：例如 "跳槽"、"涨薪"
 *
 * 【技术实现】
 * 使用正则表达式（Regex）匹配模式
 * - 支持多种薪资格式：20k、20K、2万、月薪2万
 * - 支持多种年限格式：3年、工作3年、经验3年
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class UserProfileExtractor {

    // ==================== 正则表达式模式定义 ====================

    /**
     * 薪资提取模式
     * 匹配示例：
     * - "20k" 或 "20K" → 20
     * - "20w" 或 "20万" → 20（月薪2万）
     * - "月薪20" → 20
     * - "20千" → 20
     */
    private static final Pattern SALARY_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)[kKwW万千]|月薪?(\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 工作年限提取模式
     * 匹配示例：
     * - "3年" → 3
     * - "工作3年" → 3
     * - "经验3年" → 3
     */
    private static final Pattern WORK_YEARS_PATTERN = Pattern.compile(
            "(\\d+)\\s*年|工作(\\d+)\\s*年|经验(\\d+)\\s*年",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 职位提取模式
     * 匹配示例：
     * - "做Java开发" → "Java开发"
     * - "担任前端工程师" → "前端工程师"
     * - "职位是产品经理" → "产品经理"
     */
    private static final Pattern POSITION_PATTERN = Pattern.compile(
            "(?:做|担任|职位是|岗位是|作为)\\s*([^，。！？\\s]{2,10})",
            Pattern.CASE_INSENSITIVE
    );

    // ==================== 核心方法 ====================

    /**
     * 【核心方法】从用户消息中提取用户画像信息
     *
     * 【工作流程】
     * 1. 提取薪资信息
     * 2. 提取工作年限
     * 3. 提取职位信息
     * 4. 判断用户的主要关注点
     * 5. 保存完整的原始消息作为上下文
     *
     * @param message 用户的自然语言输入
     *                例如："我现在20k，工作3年，想跳槽能涨多少？"
     * @return 提取到的用户画像对象
     */
    public UserProfile extract(String message) {
        log.debug("【用户信息提取】开始从消息中提取信息：{}", message);

        // 使用 Builder 模式构建 UserProfile 对象
        UserProfile.UserProfileBuilder builder = UserProfile.builder();

        // 步骤 1：提取薪资
        BigDecimal salary = extractSalary(message);
        builder.monthlySalary(salary);

        // 步骤 2：提取工作年限
        Integer workYears = extractWorkYears(message);
        builder.workYears(workYears);

        // 步骤 3：提取职位
        String position = extractPosition(message);
        builder.currentPosition(position);

        // 步骤 4：判断主要关注点
        String mainConcern = determineMainConcern(message);
        builder.mainConcern(mainConcern);

        // 步骤 5：保存完整消息作为上下文
        builder.additionalContext(message);

        // 构建并返回 UserProfile 对象
        UserProfile profile = builder.build();
        log.info("【用户信息提取】提取完成 - 薪资：{}k，年限：{}年，职位：{}，关注点：{}",
                profile.getMonthlySalary(), profile.getWorkYears(),
                profile.getCurrentPosition(), profile.getMainConcern());

        return profile;
    }

    // ==================== 私有方法 ====================

    /**
     * 从消息中提取薪资信息
     *
     * @param message 用户消息
     * @return 提取到的薪资（单位：千元），如果未提取到返回 null
     */
    private BigDecimal extractSalary(String message) {
        Matcher matcher = SALARY_PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                // 尝试匹配 "20k" 或 "20K" 格式
                if (matcher.group(1) != null) {
                    return new BigDecimal(matcher.group(1));
                }
                // 尝试匹配 "2万" 格式
                if (matcher.group(1) != null) {
                    String value = matcher.group(1).toLowerCase();
                    if (value.contains("w") || value.contains("万")) {
                        return new BigDecimal(value.replaceAll("[w万千]", ""));
                    }
                }
                // 尝试匹配第二个分组（纯数字）
                if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                    return new BigDecimal(matcher.group(2));
                }
            } catch (Exception e) {
                log.warn("【用户信息提取】解析薪资失败：{}", matcher.group(), e);
            }
        }

        return null;
    }

    /**
     * 从消息中提取工作年限
     *
     * @param message 用户消息
     * @return 提取到的年限（年），如果未提取到返回 null
     */
    private Integer extractWorkYears(String message) {
        Matcher matcher = WORK_YEARS_PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                // 检查所有分组，找到第一个非空的匹配
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        return Integer.parseInt(matcher.group(i));
                    }
                }
            } catch (Exception e) {
                log.warn("【用户信息提取】解析工作年限失败：{}", matcher.group(), e);
            }
        }

        return null;
    }

    /**
     * 从消息中提取职位信息
     *
     * @param message 用户消息
     * @return 提取到的职位名称，如果未提取到返回 null
     */
    private String extractPosition(String message) {
        Matcher matcher = POSITION_PATTERN.matcher(message);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * 判断用户的主要关注点
     *
     * 【分类规则】
     * - job_change：跳槽/换工作
     * - salary_increase：薪资/涨薪
     * - career_growth：成长/学习/提升
     * - risk_assessment：风险/稳定
     * - general_consultation：一般咨询（默认）
     *
     * @param message 用户消息
     * @return 关注点类型代码
     */
    private String determineMainConcern(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("跳槽") || lowerMessage.contains("换工作")) {
            return "job_change";
        } else if (lowerMessage.contains("薪资") || lowerMessage.contains("工资") || lowerMessage.contains("涨薪")) {
            return "salary_increase";
        } else if (lowerMessage.contains("成长") || lowerMessage.contains("学习") || lowerMessage.contains("提升")) {
            return "career_growth";
        } else if (lowerMessage.contains("风险") || lowerMessage.contains("稳定")) {
            return "risk_assessment";
        } else {
            return "general_consultation";
        }
    }
}
