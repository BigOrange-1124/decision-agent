package com.decisionagent.tool.impl;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.tool.AnalysisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 风险分析工具
 *
 * 【核心职责】
 * 分析跳槽可能面临的各种风险
 *
 * 【风险维度】
 * 1. 职业连续性风险（频繁跳槽对简历的影响）
 * 2. 试用期风险（无法通过试用的可能性）
 * 3. 公司稳定性风险（公司倒闭、裁员等）
 * 4. 薪资风险（高薪陷阱、期权风险等）
 *
 * 【数据参考】
 * - 约 15-20% 的跳槽者无法通过试用期
 * - 每份工作至少坚持 1.5-2 年
 * - 期权占比不宜超过总包的 30%
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RiskAnalysisTool implements AnalysisTool {

    @Override
    public String getName() {
        return "风险维度分析";
    }

    @Override
    public String getDescription() {
        return "评估跳槽可能面临的风险，包括试用期风险、公司稳定性、职业连续性等";
    }

    @Override
    public int getOrder() {
        return 4;  // 最低优先级
    }

    @Override
    public String analyze(UserProfile profile, String userMessage) {
        log.info("【风险分析】开始分析用户画像：{}", profile);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【风险维度分析】\n");

        Integer workYears = profile.getWorkYears();

        // 步骤 1：职业连续性风险分析
        analysis.append("【职业连续性风险】\n");
        if (workYears != null && workYears < 2) {
            // 初级阶段（高风险）
            analysis.append("⚠️ 高风险：频繁跳槽会被HR标记为不稳定\n");
            analysis.append("建议：每份工作至少坚持1.5-2年，建立职业稳定性\n");
        } else if (workYears != null && workYears < 5) {
            // 成长阶段（中风险）
            analysis.append("◐ 中风险：3-5年是关键期，简历上不宜有超过3段短经历\n");
            analysis.append("建议：跳槽前确保有完整的上线项目经验\n");
        } else {
            // 成熟阶段（低风险）
            analysis.append("✓ 低风险：资深阶段更看重能力匹配度\n");
            analysis.append("建议：可适度跳槽，但需考虑职位级别连续性\n");
        }

        // 步骤 2：试用期风险分析
        analysis.append("\n【试用期风险】\n");
        analysis.append("• 统计数据：约15-20%的跳槽者无法通过试用期\n");
        analysis.append("• 主要原因：\n");
        analysis.append("  1. 面试时承诺与实际工作内容不符\n");
        analysis.append("  2. 团队文化不适应，人际关系紧张\n");
        analysis.append("  3. 技术能力无法满足实际要求\n");
        analysis.append("  4. 公司业务调整，hc冻结或裁员\n");

        analysis.append("\n• 规避建议：\n");
        analysis.append("  ✓ 面试时详细了解具体工作内容和团队现状\n");
        analysis.append("  ✓ 尽可能与未来直属leader沟通\n");
        analysis.append("  ✓ 通过脉脉/LinkedIn了解公司真实情况\n");
        analysis.append("  ✓ 背调时联系前员工了解内部情况\n");

        // 步骤 3：公司稳定性风险分析
        analysis.append("\n【公司稳定性风险】\n");
        analysis.append("🔴 高风险信号（需谨慎）：\n");
        analysis.append("  • 连续亏损、融资困难的创业公司\n");
        analysis.append("  • 业务模式不清晰，频繁转型\n");
        analysis.append("  • 核心人才大量流失\n");
        analysis.append("  • 薪资远高于市场水平（可能是陷阱）\n");

        analysis.append("\n🟢 低风险信号（相对安全）：\n");
        analysis.append("  • 上市大厂或成熟独角兽\n");
        analysis.append("  • 盈利稳定的传统行业数字化部门\n");
        analysis.append("  • 有明确退出路径的创业公司\n");

        // 步骤 4：薪资风险分析
        analysis.append("\n【薪资风险】\n");
        analysis.append("• 高薪陷阱：部分公司用高薪掩盖工时强度\n");
        analysis.append("  建议打听实际工作时长（如996情况）\n");
        analysis.append("• 期权风险：未上市公司的期权可能归零\n");
        analysis.append("  建议：期权占比不超过总包的30%\n");
        analysis.append("• 涨幅风险：跳槽涨幅过大，可能期望值过高\n");
        analysis.append("  建议：涨幅控制在30%以内，除非是稀缺技术\n");

        // 步骤 5：决策建议
        analysis.append("\n【决策建议】\n");
        analysis.append("• 低风险跳槽：已拿到书面offer后再提离职\n");
        analysis.append("• 中等风险：有明确意向但未发offer时谨慎裸辞\n");
        analysis.append("• 高风险行为：仅凭口头offer就提离职（严禁）\n");

        return analysis.toString();
    }
}
