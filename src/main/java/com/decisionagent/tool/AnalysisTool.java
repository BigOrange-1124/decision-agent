package com.decisionagent.tool;

import com.decisionagent.dto.UserProfile;

/**
 * 分析工具接口
 *
 * 【接口说明】
 * 这是所有分析工具的抽象接口
 * 每个具体的分析工具都需要实现这个接口
 *
 * 【设计模式】
 * 策略模式（Strategy Pattern）：
 * - 定义了分析工具的统一行为
 * - 每个工具实现相同的接口，但有不同的分析逻辑
 * - 便于扩展新的分析维度
 *
 * 【现有实现】
 * - SalaryAnalysisTool：收入维度分析
 * - GrowthAnalysisTool：成长维度分析
 * - MarketAnalysisTool：市场维度分析
 * - RiskAnalysisTool：风险维度分析
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
public interface AnalysisTool {

    /**
     * 获取工具名称
     *
     * 【用途】
     * - 用于日志记录
     * - 用于前端展示
     *
     * @return 工具名称（中文）
     *         例如："收入维度分析"、"成长维度分析"
     */
    String getName();

    /**
     * 获取工具描述
     *
     * 【用途】
     * - 说明这个工具分析什么维度
     * - 用于生成 API 文档
     *
     * @return 工具描述（中文）
     *         例如："分析当前薪资水平与市场对比"
     */
    String getDescription();

    /**
     * 【核心方法】执行分析
     *
     * 【工作流程】
     * 1. 接收用户画像和原始消息
     * 2. 根据工具自身的分析逻辑进行处理
     * 3. 返回格式化的分析结果
     *
     * @param profile 从用户消息中提取的结构化信息
     *                包含：薪资、工作年限、职位等
     * @param userMessage 用户的原始消息（完整文本）
     * @return 分析结果（格式化的字符串，可直接展示给用户）
     */
    String analyze(UserProfile profile, String userMessage);

    /**
     * 获取工具的执行优先级
     *
     * 【用途】
     * - 控制多个工具的执行顺序
     * - 数字越小，优先级越高
     *
     * 【默认值】
     * 100（较低优先级）
     *
     * 【示例】
     * - 收入分析：order = 1（最优先）
     * - 成长分析：order = 2
     * - 市场分析：order = 3
     * - 风险分析：order = 4
     *
     * @return 优先级数值（越小越优先执行）
     */
    default int getOrder() {
        return 100;
    }
}
