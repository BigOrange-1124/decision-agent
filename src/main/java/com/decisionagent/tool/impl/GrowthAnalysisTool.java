package com.decisionagent.tool.impl;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.tool.AnalysisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Growth Analysis Tool
 *
 * Analyzes the user's career growth potential, including skill development,
 * promotion opportunities, and long-term career trajectory.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class GrowthAnalysisTool implements AnalysisTool {

    @Override
    public String getName() {
        return "成长维度分析";
    }

    @Override
    public String getDescription() {
        return "评估当前工作的成长空间、技能提升机会和职业发展路径";
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public String analyze(UserProfile profile, String userMessage) {
        log.info("Analyzing growth dimension for profile: {}", profile);

        StringBuilder analysis = new StringBuilder();
        analysis.append("【成长维度分析】\n");

        Integer workYears = profile.getWorkYears();
        String position = profile.getCurrentPosition();

        // Career stage analysis
        if (workYears != null) {
            if (workYears < 2) {
                analysis.append("• 职业阶段：初级阶段 - 重点在积累技术深度\n");
                analysis.append("• 成长建议：建议在当前公司至少待满2年，避免简历频繁跳槽\n");
                analysis.append("• 能力聚焦：深度掌握当前技术栈，打造核心竞争力\n");
            } else if (workYears < 5) {
                analysis.append("• 职业阶段：成长阶段 - 需要技术广度和项目经验\n");
                analysis.append("• 成长建议：可考虑跳槽到大厂或独角兽，提升技术视野\n");
                analysis.append("• 能力聚焦：从单一技术向全栈/架构方向拓展\n");
            } else {
                analysis.append("• 职业阶段：成熟阶段 - 关键在管理/架构能力\n");
                analysis.append("• 成长建议：跳槽应聚焦职位级别（如Tech Lead/架构师）\n");
                analysis.append("• 能力聚焦：培养团队管理能力和系统设计能力\n");
            }
        }

        // Technology growth indicators
        analysis.append("\n【成长信号判断】\n");
        analysis.append("✓ 应该跳槽的信号：\n");
        analysis.append("  - 当前工作已学不到新东西超过6个月\n");
        analysis.append("  - 没有mentor指导，技术成长停滞\n");
        analysis.append("  - 项目业务价值低，技术栈老旧\n");
        analysis.append("  - 公司没有技术分享氛围\n");

        analysis.append("\n✗ 不建议跳槽的信号：\n");
        analysis.append("  - 正在做核心技术项目，半年内可上线\n");
        analysis.append("  - 刚获得晋升或承担更重要角色\n");
        analysis.append("  - 公司有清晰的职业发展路径\n");

        // Skill growth advice
        analysis.append("\n【成长建议】\n");
        analysis.append("• 技术深度：选择能接触高并发/大流量的场景\n");
        analysis.append("• 技术广度：选择技术栈多元化的团队\n");
        analysis.append("• 软技能：选择有技术大牛的团队，可以学习思维模式\n");

        return analysis.toString();
    }
}
