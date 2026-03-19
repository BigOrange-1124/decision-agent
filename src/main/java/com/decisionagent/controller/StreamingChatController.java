package com.decisionagent.controller;

import com.decisionagent.dto.UserProfile;
import com.decisionagent.service.UserProfileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流式聊天控制器
 *
 * 【核心特性】
 * 使用 SSE (Server-Sent Events) 实时推送 Agent 的思考过程
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreamingChatController {

    private final UserProfileExtractor profileExtractor;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 【核心端点】流式聊天接口
     *
     * 使用 SSE 实时推送 Agent 的思考过程：
     * 1. 提取用户信息
     * 2. LLM 思考 (Thinking)
     * 3. LLM 决定调用工具 (Action)
     * 4. 工具执行 (Observation)
     * 5. LLM 基于结果生成建议 (Final)
     *
     * @param message 用户消息
     * @return SSE Emitter
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("【流式 Agent】开始处理");
        log.info("用户消息：{}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 创建 SSE Emitter（超时时间：5分钟）
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步执行 Agent
        executorService.submit(() -> {
            try {
                // 步骤 1：提取用户信息
                emitEvent(emitter, "step", "提取信息");
                emitEvent(emitter, "thinking", "正在分析用户问题...");

                UserProfile profile = profileExtractor.extract(message);

                if (profile.getMonthlySalary() != null) {
                    emitEvent(emitter, "info", "提取到薪资：" + profile.getMonthlySalary() + "k");
                }
                if (profile.getWorkYears() != null) {
                    emitEvent(emitter, "info", "提取到工作年限：" + profile.getWorkYears() + "年");
                }

                // 步骤 2：LLM 思考
                emitEvent(emitter, "step", "AI 思考");
                emitEvent(emitter, "thinking", "AI 正在分析问题，决定需要哪些分析维度...");

                Thread.sleep(500);  // 模拟思考时间

                // 步骤 3：LLM 决定调用工具
                emitEvent(emitter, "step", "AI 决策");

                // 根据用户问题智能决定调用哪些工具
                boolean needSalary = message.contains("薪资") || message.contains("钱") ||
                                     message.contains("工资") || message.contains("k") ||
                                     profile.getMonthlySalary() != null;
                boolean needGrowth = message.contains("成长") || message.contains("发展") ||
                                     profile.getWorkYears() != null;
                boolean needMarket = message.contains("市场") || message.contains("时机");
                boolean needRisk = message.contains("风险") || message.contains("危险");

                if (!needSalary && !needGrowth && !needMarket && !needRisk) {
                    // 默认全部分析
                    needSalary = true;
                    needGrowth = true;
                }

                emitEvent(emitter, "decision", "AI 决定分析以下维度：");

                if (needSalary) {
                    emitEvent(emitter, "tool_selected", "收入维度");
                }
                if (needGrowth) {
                    emitEvent(emitter, "tool_selected", "成长维度");
                }
                if (needMarket) {
                    emitEvent(emitter, "tool_selected", "市场维度");
                }
                if (needRisk) {
                    emitEvent(emitter, "tool_selected", "风险维度");
                }

                Thread.sleep(500);

                // 步骤 4：执行工具
                emitEvent(emitter, "step", "执行分析");
                StringBuilder allAnalysis = new StringBuilder();

                if (needSalary) {
                    emitEvent(emitter, "tool_start", "收入维度分析");
                    String result = analyzeSalary(profile.getMonthlySalary(), emitter);
                    allAnalysis.append(result).append("\n\n");
                    emitEvent(emitter, "tool_end", "收入维度分析");
                }

                if (needGrowth) {
                    emitEvent(emitter, "tool_start", "成长维度分析");
                    String result = analyzeGrowth(profile.getWorkYears(), emitter);
                    allAnalysis.append(result).append("\n\n");
                    emitEvent(emitter, "tool_end", "成长维度分析");
                }

                if (needMarket) {
                    emitEvent(emitter, "tool_start", "市场维度分析");
                    String result = analyzeMarket(emitter);
                    allAnalysis.append(result).append("\n\n");
                    emitEvent(emitter, "tool_end", "市场维度分析");
                }

                if (needRisk) {
                    emitEvent(emitter, "tool_start", "风险维度分析");
                    String result = analyzeRisk(profile.getWorkYears(), emitter);
                    allAnalysis.append(result).append("\n\n");
                    emitEvent(emitter, "tool_end", "风险维度分析");
                }

                // 步骤 5：LLM 生成最终建议
                emitEvent(emitter, "step", "AI 生成建议");
                emitEvent(emitter, "thinking", "AI 正在基于分析结果生成综合建议...");

                Thread.sleep(1000);  // 模拟 LLM 生成时间

                String llmAdvice = generateLlmAdvice(message, profile, allAnalysis.toString());
                emitEvent(emitter, "step", "完成");
                emitEvent(emitter, "final", llmAdvice);

                // 完成
                emitEvent(emitter, "done", "分析完成");

                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log.info("【流式 Agent】处理完成");
                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            } catch (Exception e) {
                log.error("【流式 Agent】处理失败", e);
                emitEvent(emitter, "error", "处理失败：" + e.getMessage());
            } finally {
                emitter.complete();
            }
        });

        // 设置超时和错误处理
        emitter.onTimeout(() -> {
            log.warn("【流式 Agent】超时");
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("【流式 Agent】错误", e);
            emitter.completeWithError(e);
        });

        return emitter;
    }

    /**
     * 发送 SSE 事件
     */
    private void emitEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data)
                    .comment(""));
            log.debug("【SSE】发送事件：{} = {}", event, data);
        } catch (IOException e) {
            log.error("【SSE】发送事件失败：{} = {}", event, data, e);
        }
    }

    /**
     * 分析收入（带实时推送）
     */
    private String analyzeSalary(BigDecimal salary, SseEmitter emitter) throws IOException {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【收入维度分析】\n");

        if (salary != null) {
            analysis.append(String.format("• 当前薪资：%.1fk\n", salary));
            emitEvent(emitter, "progress", "当前薪资：" + salary + "k");

            BigDecimal marketAvg = getMarketAverage(null);
            BigDecimal ratio = salary
                    .divide(marketAvg, 2, RoundingMode.HALF_UP);

            analysis.append(String.format("• 市场对比：%.0f%%\n", ratio.multiply(new BigDecimal("100"))));
            emitEvent(emitter, "progress", "市场对比：" + ratio.multiply(new BigDecimal("100")) + "%");

            if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
                analysis.append("• 薪资评估：⚠️ 薪资偏低，有提升空间\n");
                emitEvent(emitter, "progress", "评估：薪资偏低");
            } else {
                analysis.append("• 薪资评估：✓ 薪资正常\n");
                emitEvent(emitter, "progress", "评估：薪资正常");
            }

            analysis.append(String.format("• 预期薪资：%.1fk - %.1fk\n",
                    salary .multiply(BigDecimal.valueOf(1.15)), salary.multiply(BigDecimal.valueOf(1.3))));
            emitEvent(emitter, "progress", "预期涨幅：15-30%");
        } else {
            analysis.append("• 未获取到薪资信息\n");
            emitEvent(emitter, "progress", "未获取薪资信息");
        }

        return analysis.toString();
    }

    /**
     * 分析成长（带实时推送）
     */
    private String analyzeGrowth(Integer workYears, SseEmitter emitter) throws IOException {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【成长维度分析】\n");

        if (workYears != null) {
            if (workYears < 2) {
                analysis.append("• 职业阶段：初级阶段\n");
                emitEvent(emitter, "progress", "职业阶段：初级");
            } else if (workYears < 5) {
                analysis.append("• 职业阶段：成长阶段\n");
                emitEvent(emitter, "progress", "职业阶段：成长");
            } else {
                analysis.append("• 职业阶段：成熟阶段\n");
                emitEvent(emitter, "progress", "职业阶段：成熟");
            }
        }

        analysis.append("• 建议：持续学习技术，积累项目经验\n");
        return analysis.toString();
    }

    /**
     * 分析市场（带实时推送）
     */
    private String analyzeMarket(SseEmitter emitter) throws IOException {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【市场维度分析】\n");

        Month month = Month.from(java.time.LocalDate.now());
        analysis.append("• 当前时间：").append(month.name()).append("\n");

        int monthValue = month.getValue();
        if (monthValue >= 2 && monthValue <= 4) {
            analysis.append("• 金三银四：跳槽黄金期\n");
            emitEvent(emitter, "progress", "当前是跳槽黄金期");
        } else {
            analysis.append("• 当前是招聘平季\n");
            emitEvent(emitter, "progress", "当前是招聘平季");
        }

        analysis.append("• 建议：根据市场情况选择合适的时机\n");
        return analysis.toString();
    }

    /**
     * 分析风险（带实时推送）
     */
    private String analyzeRisk(Integer workYears, SseEmitter emitter) throws IOException {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【风险维度分析】\n");

        if (workYears != null && workYears < 2) {
            analysis.append("• 风险等级：高（频繁跳槽风险）\n");
            emitEvent(emitter, "progress", "风险等级：高");
        } else {
            analysis.append("• 风险等级：中\n");
            emitEvent(emitter, "progress", "风险等级：中");
        }

        analysis.append("• 建议：注意职业连续性，避免频繁跳槽\n");
        return analysis.toString();
    }

    /**
     * 生成 LLM 建议
     */
    private String generateLlmAdvice(String message, UserProfile profile, String analysis) {
        // 简化版：基于规则生成建议
        StringBuilder advice = new StringBuilder();
        advice.append("根据以上分析，给出以下建议：\n\n");

        if (analysis.contains("薪资偏低")) {
            advice.append("1. 薪资方面：你的薪资偏低，跳槽有较大提升空间\n");
        } else {
            advice.append("1. 薪资方面：你的薪资处于正常水平\n");
        }

        if (profile.getWorkYears() != null && profile.getWorkYears() < 3) {
            advice.append("2. 成长方面：建议先沉淀技术，积累项目经验\n");
        } else {
            advice.append("2. 成长方面：可以考虑跳槽，提升技术视野\n");
        }

        advice.append("3. 时机方面：");
        if (analysis.contains("跳槽黄金期")) {
            advice.append("当前是跳槽黄金期，机会较多\n");
        } else {
            advice.append("当前是招聘平季，可以提前准备\n");
        }

        advice.append("4. 综合建议：根据你的情况，");
        if (analysis.contains("薪资偏低")) {
            advice.append("建议跳槽，可以争取 20-30% 的涨幅\n");
        } else {
            advice.append("可以考虑跳槽，但需要谨慎选择\n");
        }

        return advice.toString();
    }

    private BigDecimal getMarketAverage(Integer workYears) {
        if (workYears == null || workYears < 3) {
            return new BigDecimal("18");
        } else if (workYears < 5) {
            return new BigDecimal("25");
        } else {
            return new BigDecimal("35");
        }
    }
}
