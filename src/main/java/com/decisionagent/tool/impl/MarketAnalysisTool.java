package com.decisionagent.tool.impl;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.tool.AnalysisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.time.YearMonth;

/**
 * 市场分析工具
 *
 * 【核心职责】
 * 分析当前招聘市场环境和最佳跳槽时机
 *
 * 【分析维度】
 * 1. 季节性招聘模式（金三银四、金九银十）
 * 2. 行业趋势和热门方向
 * 3. 不同经验阶段的择业建议
 * 4. 市场活跃度信号
 *
 * 【季节性规律】
 * - 金三银四（2-4月）：跳槽黄金期
 * - 金九银十（9-11月）：次旺季
 * - 其他月份：淡季，但竞争少
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class MarketAnalysisTool implements AnalysisTool {

    @Override
    public String getName() {
        return "市场维度分析";
    }

    @Override
    public String getDescription() {
        return "分析当前招聘市场环境、行业趋势和最佳跳槽时机";
    }

    @Override
    public int getOrder() {
        return 3;  // 第三优先级
    }

    @Override
    public String analyze(UserProfile profile, String userMessage) {
        log.info("【市场分析】开始分析用户画像：{}", profile);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【市场维度分析】\n");

        // 步骤 1：季节性招聘模式分析
        Month currentMonth = YearMonth.now().getMonth();
        analysis.append("• 当前时间：").append(currentMonth.name()).append("\n");

        int monthValue = currentMonth.getValue();
        if (monthValue >= 2 && monthValue <= 4) {
            // 金三银四（春季）
            analysis.append("• 金三银四：当前是跳槽黄金期，机会多，薪资涨幅可达30%+\n");
        } else if (monthValue >= 9 && monthValue <= 11) {
            // 金九银十（秋季）
            analysis.append("• 金九银十：当前是次旺季，企业秋招补录机会多\n");
        } else {
            // 淡季
            analysis.append("• 淡季建议：当前市场相对平淡，但竞争少，可提前准备\n");
        }

        // 步骤 2：行业趋势分析
        analysis.append("\n【行业趋势】\n");
        analysis.append("🔥 热门方向（机会多）：\n");
        analysis.append("  • AI/大模型应用开发 - 人才缺口大，薪资溢价高\n");
        analysis.append("  • 云原生/DevOps - 企业数字化转型刚需\n");
        analysis.append("  • 数据工程/实时计算 - 数据驱动决策趋势\n");

        analysis.append("\n📊 稳定方向（风险低）：\n");
        analysis.append("  • 金融科技 - 监管趋严，但需求稳定\n");
        analysis.append("  • 企业服务 - ToB业务现金流好\n");
        analysis.append("  • 传统行业数字化 - 转型需求持续\n");

        // 步骤 3：根据工作年限推荐公司类型
        analysis.append("\n【公司类型建议】\n");
        Integer workYears = profile.getWorkYears();
        if (workYears != null && workYears < 3) {
            analysis.append("• 建议优先：中型互联网公司 > 大厂 > 创业公司\n");
            analysis.append("  理由：中型公司能获得完整项目经验，而非螺丝钉\n");
        } else if (workYears != null && workYears < 5) {
            analysis.append("• 建议优先：大厂核心部门 > 独角兽 > 成熟中型公司\n");
            analysis.append("  理由：提升技术视野和背书价值\n");
        } else {
            analysis.append("• 建议优先：有期权/股票的成熟公司 > 上市大厂 > 高薪创业公司\n");
            analysis.append("  理由：职业后期更看重稳定性和长期收益\n");
        }

        // 步骤 4：市场活跃度信号
        analysis.append("\n【市场信号】\n");
        analysis.append("✓ 市场活跃的信号：\n");
        analysis.append("  - 猎头主动联系频率增加\n");
        analysis.append("  - 招聘网站职位数量明显增多\n");
        analysis.append("  - 朋友/同事讨论跳槽话题增多\n");

        return analysis.toString();
    }
}
