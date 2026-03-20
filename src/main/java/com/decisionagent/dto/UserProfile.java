package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户画像数据传输对象
 *
 * 【用途说明】
 * 保存从用户消息中提取的结构化信息
 * 用于后续的分析和决策
 *
 * 【数据来源】
 * 由 UserProfileExtractor 服务从用户消息中提取
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@Builder  // Lombok 注解：支持构建者模式
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
public class UserProfile {

    /**
     * 当前月薪（单位：千元）
     *
     * 【示例】
     * - BigDecimal.valueOf(20) 表示月薪 20k
     * - BigDecimal.valueOf(25.5) 表示月薪 25.5k
     *
     * 【为什么用 BigDecimal？】
     * - 精确的十进制计算，避免浮点数精度问题
     * - 适合金融和薪资计算场景
     */
    private BigDecimal monthlySalary;

    /**
     * 工作年限（单位：年）
     *
     * 【示例】
     * - 3 表示工作 3 年
     * - 5 表示工作 5 年
     */
    private Integer workYears;

    /**
     * 当前公司或行业
     *
     * 【示例】
     * - "阿里巴巴"
     * - "互联网行业"
     */
    private String currentCompany;

    /**
     * 目标公司或行业（如果用户提到）
     *
     * 【示例】
     * - "字节跳动"
     * - "想要去大厂"
     */
    private String targetCompany;

    /**
     * 当前职位或角色
     *
     * 【示例】
     * - "Java开发工程师"
     * - "前端工程师"
     * - "产品经理"
     */
    private String currentPosition;

    /**
     * 用户的主要关注点或目标
     *
     * 【可能的值】
     * - "job_change"：跳槽
     * - "salary_increase"：涨薪
     * - "career_growth"：职业成长
     * - "risk_assessment"：风险评估
     * - "general_consultation"：一般咨询
     */
    private String mainConcern;

    /**
     * 额外的上下文信息
     *
     * 【用途】
     * - 保存用户的完整原始消息
     * - 提供给 LLM 作为参考
     *
     * 【示例】
     * - "我现在20k，工作3年，想跳槽能涨多少？"
     */
    private String additionalContext;

    /**
     * 判断用户画像是否为空
     *
     * 【用途】
     * 检查是否成功提取到任何有效信息
     *
     * @return 如果所有字段都为空或 null，返回 true
     */
    public boolean isEmpty() {
        return monthlySalary == null
                && workYears == null
                && (currentCompany == null || currentCompany.isEmpty())
                && (targetCompany == null || targetCompany.isEmpty())
                && (currentPosition == null || currentPosition.isEmpty())
                && (mainConcern == null || mainConcern.isEmpty())
                && (additionalContext == null || additionalContext.isEmpty());
    }
}
