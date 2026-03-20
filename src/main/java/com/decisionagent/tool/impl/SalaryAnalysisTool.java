package com.decisionagent.tool.impl;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.tool.AnalysisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 收入分析工具
 *
 * 【核心职责】
 * 分析用户的收入情况和跳槽后的薪资提升空间
 *
 * 【分析维度】
 * 1. 当前薪资水平
 * 2. 与市场平均水平的对比
 * 3. 跳槽后的薪资涨幅预期
 * 4. 不同经验阶段的市场参考价
 *
 * 【市场数据】
 * - 3年以下：约 18k/月
 * - 3-5年：约 25k/月
 * - 5年以上（资深）：约 35k/月
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Component  // Spring 组件注解，会被自动扫描和管理
public class SalaryAnalysisTool implements AnalysisTool {

    // ==================== 常量定义 ====================

    /**
     * 3年以下经验的平均市场薪资（单位：千元）
     */
    private static final BigDecimal MARKET_AVG_3_YEARS = new BigDecimal("18");

    /**
     * 3-5年经验的平均市场薪资（单位：千元）
     */
    private static final BigDecimal MARKET_AVG_5_YEARS = new BigDecimal("25");

    /**
     * 5年以上经验的平均市场薪资（单位：千元）
     */
    private static final BigDecimal MARKET_AVG_SENIOR = new BigDecimal("35");

    // ==================== 接口实现 ====================

    @Override
    public String getName() {
        return "收入维度分析";
    }

    @Override
    public String getDescription() {
        return "分析当前薪资水平与市场对比，评估跳槽的薪资提升空间";
    }

    @Override
    public int getOrder() {
        return 1;  // 最高优先级，薪资是最核心的考量因素
    }

    // ==================== 核心方法 ====================

    /**
     * 【核心方法】执行收入分析
     *
     * @param profile 用户画像（包含薪资、工作年限等信息）
     * @param userMessage 用户的原始消息
     * @return 格式化的收入分析报告
     */
    @Override
    public String analyze(UserProfile profile, String userMessage) {
        log.info("【收入分析】开始分析用户画像：{}", profile);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【收入维度分析】\n");

        // 提取关键信息
        BigDecimal currentSalary = profile.getMonthlySalary();
        Integer workYears = profile.getWorkYears();

        // 步骤 1：如果有薪资信息，进行详细分析
        if (currentSalary != null && currentSalary.compareTo(BigDecimal.ZERO) > 0) {
            // 1.1 显示当前薪资
            analysis.append(String.format("• 当前月薪：%.1fk\n", currentSalary));

            // 1.2 市场对比分析
            BigDecimal marketAvg = getMarketAverage(workYears);
            if (marketAvg != null) {
                // 计算当前薪资与市场平均的比率
                BigDecimal ratio = currentSalary.divide(marketAvg, 2, RoundingMode.HALF_UP);
                analysis.append(String.format("• 市场对比：当前薪资约为市场平均水平的%.0f%%\n",
                        ratio.multiply(new BigDecimal("100"))));

                // 1.3 根据比率给出评估
                if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
                    // 薪资偏低（低于市场平均 80%）
                    analysis.append("• 薪资评估：⚠️ 你的薪资偏低，跳槽有较大提升空间（预计20-40%）\n");
                } else if (ratio.compareTo(new BigDecimal("1.2")) > 0) {
                    // 薪资偏高（高于市场平均 20%）
                    analysis.append("• 薪资评估：✓ 你的薪资高于市场平均水平，跳槽需谨慎\n");
                } else {
                    // 薪资正常（在市场平均的 80%-120% 之间）
                    analysis.append("• 薪资评估：◐ 你的薪资处于正常水平，跳槽可争取10-20%涨幅\n");
                }
            }

            // 1.4 根据工作年限给出建议
            if (workYears != null && workYears < 3) {
                analysis.append("• 成长建议：3年经验是跳槽黄金期，建议先沉淀技术再考虑\n");
            } else if (workYears != null && workYears >= 3 && workYears <= 5) {
                analysis.append("• 成长建议：当前是最佳跳槽时机，市场认可度较高\n");
            } else {
                analysis.append("• 成长建议：资深阶段跳槽更看重职位和团队，而非薪资涨幅\n");
            }

        } else {
            // 步骤 2：如果没有薪资信息，给出通用建议
            analysis.append("• 未获取到薪资信息，建议补充当前薪资以便精准分析\n");
            analysis.append("• 一般跳槽涨幅：15-30%为正常范围，特殊情况可达50%+\n");
        }

        // 步骤 3：提供市场参考价
        analysis.append("• 市场参考：3年经验约18k，5年经验约25k，资深约35k+\n");

        return analysis.toString();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据工作年限获取市场平均薪资
     *
     * @param workYears 工作年限（年）
     * @return 市场平均薪资（单位：千元）
     */
    private BigDecimal getMarketAverage(Integer workYears) {
        if (workYears == null) {
            // 如果没有工作年限信息，默认使用 3 年标准
            return MARKET_AVG_3_YEARS;
        }

        if (workYears < 3) {
            return MARKET_AVG_3_YEARS;
        } else if (workYears < 5) {
            return MARKET_AVG_5_YEARS;
        } else {
            return MARKET_AVG_SENIOR;
        }
    }
}
