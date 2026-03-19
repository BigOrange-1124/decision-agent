package com.decisionagent.agent;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.service.UserProfileExtractor;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.YearMonth;

/**
 * 职业决策工具集
 *
 * 【核心特性】
 * 使用 LangChain4j 的 @Tool 注解，让 LLM 可以直接调用这些方法
 *
 * 【工作原理】
 * 1. LangChain4j 自动扫描 @Tool 注解的方法
 * 2. 将工具的描述、参数信息发送给 LLM
 * 3. LLM 根据用户问题自主决定是否调用工具
 * 4. LLM 决定调用哪个工具、传入什么参数
 * 5. LangChain4j 自动执行工具并返回结果给 LLM
 *
 * 【关键优势】
 * - ✅ 真正的 Tool Calling：LLM 直接决定调用哪些工具
 * - ✅ 无需手动解析 JSON
 * - ✅ 无需字符串匹配
 * - ✅ LLM 可以传入参数（例如薪资、工作年限）
 * - ✅ 支持 ReAct 模式（思考 → 行动 → 观察）
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CareerDecisionTools {

    private final UserProfileExtractor profileExtractor;

    // ==================== 工具 1：收入维度分析 ====================

    /**
     * 分析用户的收入情况和跳槽涨幅空间
     *
     * 【LLM 会自动调用这个方法】
     * 当用户询问薪资、涨薪、收入相关问题时，LLM 会决定是否调用此工具
     *
     * @param currentSalary 当前月薪（单位：千元），例如：20 表示 20k
     * @return 收入分析结果
     */
    @Tool("分析用户的收入情况和跳槽涨幅空间。需要提供当前月薪（单位：千元）")
    public String analyzeSalary(Double currentSalary) {
        log.info("【Tool Calling】LLM 调用了 analyzeSalary 工具，参数：currentSalary = {}", currentSalary);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【收入维度分析】\n");

        if (currentSalary != null && currentSalary > 0) {
            // 当前薪资
            analysis.append(String.format("• 当前月薪：%.1fk\n", currentSalary));

            // 市场对比
            BigDecimal marketAvg = getMarketAverage(null);
            BigDecimal ratio = BigDecimal.valueOf(currentSalary).divide(marketAvg, 2, RoundingMode.HALF_UP);
            analysis.append(String.format("• 市场对比：当前薪资约为市场平均水平的%.0f%%\n", ratio.multiply(new BigDecimal("100"))));

            if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
                analysis.append("• 薪资评估：⚠️ 你的薪资偏低，跳槽有较大提升空间（预计20-40%）\n");
            } else if (ratio.compareTo(new BigDecimal("1.2")) > 0) {
                analysis.append("• 薪资评估：✓ 你的薪资高于市场平均水平，跳槽需谨慎\n");
            } else {
                analysis.append("• 薪资评估：◐ 你的薪资处于正常水平，跳槽可争取10-20%涨幅\n");
            }

            // 涨幅建议
            analysis.append("• 跳槽涨幅：一般跳槽涨幅 15-30% 为正常范围\n");
            analysis.append(String.format("• 预期薪资：%.1fk - %.1fk\n",
                currentSalary * 1.15, currentSalary * 1.3));
        } else {
            analysis.append("• 未获取到薪资信息，建议补充当前薪资以便精准分析\n");
        }

        analysis.append("• 市场参考：3年经验约 18k，5年经验约 25k，资深约 35k+\n");

        String result = analysis.toString();
        log.info("【Tool Calling】analyzeSalary 执行完成，返回结果长度：{}", result.length());
        return result;
    }

    // ==================== 工具 2：成长维度分析 ====================

    /**
     * 分析用户的职业成长空间和发展机会
     *
     * @param workYears 工作年限（单位：年），例如：3 表示 3 年经验
     * @return 成长分析结果
     */
    @Tool("分析用户的职业成长空间和发展机会。需要提供工作年限（单位：年）")
    public String analyzeGrowth(Integer workYears) {
        log.info("【Tool Calling】LLM 调用了 analyzeGrowth 工具，参数：workYears = {}", workYears);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【成长维度分析】\n");

        if (workYears != null) {
            // 职业阶段分析
            if (workYears < 2) {
                analysis.append("• 职业阶段：初级阶段 - 重点在积累技术深度\n");
                analysis.append("• 成长建议：建议在当前公司至少待满 2 年，避免简历频繁跳槽\n");
            } else if (workYears < 5) {
                analysis.append("• 职业阶段：成长阶段 - 需要技术广度和项目经验\n");
                analysis.append("• 成长建议：可考虑跳槽到大厂或独角兽，提升技术视野\n");
            } else {
                analysis.append("• 职业阶段：成熟阶段 - 关键在管理/架构能力\n");
                analysis.append("• 成长建议：跳槽应聚焦职位级别（如 Tech Lead/架构师）\n");
            }
        } else {
            analysis.append("• 未提供工作年限，无法分析职业阶段\n");
        }

        // 成长信号判断
        analysis.append("\n【成长信号判断】\n");
        analysis.append("✓ 应该跳槽的信号：\n");
        analysis.append("  - 当前工作已学不到新东西超过 6 个月\n");
        analysis.append("  - 没有 mentor 指导，技术成长停滞\n");
        analysis.append("  - 项目业务价值低，技术栈老旧\n");

        analysis.append("\n✗ 不建议跳槽的信号：\n");
        analysis.append("  - 正在做核心技术项目，半年内可上线\n");
        analysis.append("  - 刚获得晋升或承担更重要角色\n");

        String result = analysis.toString();
        log.info("【Tool Calling】analyzeGrowth 执行完成，返回结果长度：{}", result.length());
        return result;
    }

    // ==================== 工具 3：市场维度分析 ====================

    /**
     * 分析当前招聘市场环境和最佳跳槽时机
     *
     * @return 市场分析结果
     */
    @Tool("分析当前招聘市场环境和最佳跳槽时机。无需额外参数")
    public String analyzeMarket() {
        log.info("【Tool Calling】LLM 调用了 analyzeMarket 工具（无参数）");

        StringBuilder analysis = new StringBuilder();
        analysis.append("【市场维度分析】\n");

        // 季节性招聘模式
        Month currentMonth = YearMonth.now().getMonth();
        analysis.append("• 当前时间：").append(currentMonth.name()).append("\n");

        int monthValue = currentMonth.getValue();
        if (monthValue >= 2 && monthValue <= 4) {
            analysis.append("• 金三银四：当前是跳槽黄金期，机会多，薪资涨幅可达 30%+\n");
        } else if (monthValue >= 9 && monthValue <= 11) {
            analysis.append("• 金九银十：当前是次旺季，企业秋招补录机会多\n");
        } else {
            analysis.append("• 淡季建议：当前市场相对平淡，但竞争少，可提前准备\n");
        }

        // 行业趋势
        analysis.append("\n【行业趋势】\n");
        analysis.append("🔥 热门方向（机会多）：\n");
        analysis.append("  • AI/大模型应用开发 - 人才缺口大，薪资溢价高\n");
        analysis.append("  • 云原生/DevOps - 企业数字化转型刚需\n");
        analysis.append("  • 数据工程/实时计算 - 数据驱动决策趋势\n");

        String result = analysis.toString();
        log.info("【Tool Calling】analyzeMarket 执行完成，返回结果长度：{}", result.length());
        return result;
    }

    // ==================== 工具 4：风险维度分析 ====================

    /**
     * 分析跳槽可能面临的风险
     *
     * @param workYears 工作年限（用于评估职业连续性风险）
     * @return 风险分析结果
     */
    @Tool("分析跳槽可能面临的风险。需要提供工作年限（单位：年）")
    public String analyzeRisk(Integer workYears) {
        log.info("【Tool Calling】LLM 调用了 analyzeRisk 工具，参数：workYears = {}", workYears);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【风险维度分析】\n");

        // 职业连续性风险
        analysis.append("【职业连续性风险】\n");
        if (workYears != null && workYears < 2) {
            analysis.append("⚠️ 高风险：频繁跳槽会被 HR 标记为不稳定\n");
            analysis.append("建议：每份工作至少坚持 1.5-2 年，建立职业稳定性\n");
        } else if (workYears != null && workYears < 5) {
            analysis.append("◐ 中风险：3-5 年是关键期，简历上不宜有超过 3 段短经历\n");
            analysis.append("建议：跳槽前确保有完整的上线项目经验\n");
        } else {
            analysis.append("✓ 低风险：资深阶段更看重能力匹配度\n");
            analysis.append("建议：可适度跳槽，但需考虑职位级别连续性\n");
        }

        // 试用期风险
        analysis.append("\n【试用期风险】\n");
        analysis.append("• 统计数据：约 15-20% 的跳槽者无法通过试用期\n");
        analysis.append("• 主要原因：面试承诺与实际不符、团队文化不适应、能力不匹配\n");

        // 公司稳定性风险
        analysis.append("\n【公司稳定性风险】\n");
        analysis.append("🔴 高风险信号（需谨慎）：\n");
        analysis.append("  • 连续亏损、融资困难的创业公司\n");
        analysis.append("  • 业务模式不清晰，频繁转型\n");
        analysis.append("  • 核心人才大量流失\n");

        String result = analysis.toString();
        log.info("【Tool Calling】analyzeRisk 执行完成，返回结果长度：{}", result.length());
        return result;
    }

    // ==================== 辅助方法 ====================

    private BigDecimal getMarketAverage(Integer workYears) {
        if (workYears == null) {
            return new BigDecimal("18");
        }
        if (workYears < 3) {
            return new BigDecimal("18");
        } else if (workYears < 5) {
            return new BigDecimal("25");
        } else {
            return new BigDecimal("35");
        }
    }
}
