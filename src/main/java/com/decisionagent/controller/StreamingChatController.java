package com.decisionagent.controller;

import com.decisionagent.agent.DecisionAgent;
import com.decisionagent.dto.AgentExecutionResult;
import com.decisionagent.dto.AgentExecutionTrace;
import com.decisionagent.service.UserProfileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流式聊天控制器（模型驱动版本）
 *
 * 【核心特性】
 * 使用 SSE (Server-Sent Events) 实时推送 Agent 的思考过程
 * 与 ChatController 使用相同的 DecisionAgent，但通过 SSE 逐步推送执行过程
 *
 * 【架构说明】
 * 复用 DecisionAgent 的核心逻辑，通过推送 AgentExecutionTrace 的各个步骤实现流式输出
 *
 * 【Agent 流程】
 * 1. 提取用户信息（UserProfileExtractor）
 * 2. LLM 规划工具（ToolPlanningService）
 * 3. 执行工具（ToolExecutor）
 * 4. LLM 生成建议（LlmService）
 *
 * 【SSE 事件类型】
 * - step: 执行步骤（"提取信息"、"LLM 规划"、"执行分析"等）
 * - thinking: LLM 思考状态
 * - info: 信息提示
 * - tool_selected: LLM 选择的工具
 * - tool_start: 工具开始执行
 * - tool_end: 工具执行结束
 * - tool_result: 工具执行结果
 * - final: 最终建议
 * - done: 完成
 * - error: 错误
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreamingChatController {

    // ==================== 依赖的服务 ====================

    /**
     * 决策 Agent（核心）
     * 与 ChatController 使用同一个 Agent 实例
     * 这样保证了两个 Controller 的逻辑完全一致
     */
    private final DecisionAgent decisionAgent;

    /** 用户信息提取器（用于提前推送提取结果） */
    private final UserProfileExtractor profileExtractor;

    /** 异步执行器（用于在后台线程执行 Agent） */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 【核心端点】流式聊天接口
     *
     * 使用 SSE 实时推送 Agent 的执行过程
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
                // ===================================================================
                // 步骤 1：提前推送用户信息提取结果
                // （因为 DecisionAgent.process() 不会逐步推送）
                // ===================================================================
                emitEvent(emitter, "step", "提取信息");
                emitEvent(emitter, "thinking", "正在分析用户问题...");

                var profile = profileExtractor.extract(message);

                if (profile.getMonthlySalary() != null) {
                    emitEvent(emitter, "info", "✓ 提取到薪资：" + profile.getMonthlySalary() + "k");
                }
                if (profile.getWorkYears() != null) {
                    emitEvent(emitter, "info", "✓ 提取到工作年限：" + profile.getWorkYears() + "年");
                }
                if (profile.getCurrentPosition() != null) {
                    emitEvent(emitter, "info", "✓ 提取到职位：" + profile.getCurrentPosition());
                }

                // ===================================================================
                // 步骤 2：调用 DecisionAgent 处理（与 ChatController 相同）
                // ===================================================================
                emitEvent(emitter, "step", "Agent 执行中");

                AgentExecutionResult result = decisionAgent.process(message);
                AgentExecutionTrace trace = result.getTrace();

                // ===================================================================
                // 步骤 3：根据执行轨迹逐步推送事件
                // ===================================================================

                // 推送 LLM 规划结果
                if (trace.getPlanning() != null) {
                    emitEvent(emitter, "step", "LLM 规划");
                    emitEvent(emitter, "info", "✓ LLM 决策：" + trace.getPlanning().getReasoning());

                    if (trace.getPlanning().getSelectedTools() != null) {
                        emitEvent(emitter, "decision", "AI 决定分析以下维度：");
                        for (String tool : trace.getPlanning().getSelectedTools()) {
                            String displayName = getToolDisplayName(tool);
                            emitEvent(emitter, "tool_selected", displayName);
                        }
                    }
                }

                // 推送工具执行过程
                if (trace.getToolExecutions() != null && !trace.getToolExecutions().isEmpty()) {
                    emitEvent(emitter, "step", "执行分析");

                    for (var step : trace.getToolExecutions()) {
                        emitEvent(emitter, "tool_start", step.getDisplayName());
                        emitEvent(emitter, "thinking", "正在执行 " + step.getDisplayName() + "...");

                        if ("success".equals(step.getStatus())) {
                            emitEvent(emitter, "tool_result", step.getResult());
                            emitEvent(emitter, "tool_end",
                                    step.getDisplayName() + " (耗时：" + step.getDurationMs() + "ms)");
                        } else {
                            emitEvent(emitter, "error",
                                    step.getDisplayName() + " 失败：" + step.getErrorMessage());
                        }
                    }
                }

                // 推送最终建议
                emitEvent(emitter, "step", "LLM 生成建议");
                emitEvent(emitter, "final", trace.getLlmAdvice());
                emitEvent(emitter, "done", "分析完成");

                log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                log.info("【流式 Agent】处理完成，耗时：{}ms", trace.getTotalDurationMs());
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
     * 获取工具显示名称
     */
    private String getToolDisplayName(String toolName) {
        return switch (toolName) {
            case "salary_analysis" -> "收入维度";
            case "growth_analysis" -> "成长维度";
            case "market_analysis" -> "市场维度";
            case "risk_analysis" -> "风险维度";
            default -> toolName;
        };
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
}
