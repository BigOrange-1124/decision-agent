package com.decisionagent.controller;

import com.decisionagent.agent.ReActAgent;
import com.decisionagent.dto.ReActResult;
import com.decisionagent.dto.ReActStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ReAct 流式聊天控制器
 *
 * 【核心特性】
 * 使用 SSE 实时推送 ReAct Agent 的每一轮思考过程
 *
 * 【SSE 事件类型】
 * - round: 开始新一轮
 * - thought: LLM 思考
 * - action: LLM 选择工具
 * - observation: 工具执行结果
 * - final_answer: 最终答案
 * - done: 完成
 * - error: 错误
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/react")
@RequiredArgsConstructor
public class ReActStreamingController {

    /** ReAct Agent */
    private final ReActAgent reactAgent;

    /** 异步执行器 */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 【核心端点】ReAct 流式聊天接口
     *
     * @param message 用户消息
     * @return SSE Emitter
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("【ReAct 流式 Controller】收到请求");
        log.info("消息：{}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 创建 SSE Emitter（超时时间：5分钟）
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步执行
        executorService.submit(() -> {
            try {
                emitEvent(emitter, "start", "开始 ReAct 分析...");

                // 这里需要修改 ReActAgent，让它支持回调
                // 暂时使用同步方式，然后逐步推送
                ReActResult result = reactAgent.process(message);

                // 逐步推送每一轮
                if (result.getSteps() != null) {
                    for (ReActStep step : result.getSteps()) {
                        emitEvent(emitter, "round", "Round " + step.getIteration());

                        if (step.getThought() != null) {
                            emitEvent(emitter, "thought", step.getThought());
                        }

                        if (step.isFinalAnswer()) {
                            emitEvent(emitter, "final_answer", step.getFinalAnswer());
                            break;
                        }

                        if (step.getAction() != null) {
                            emitEvent(emitter, "action", getToolDisplayName(step.getAction()));
                        }

                        if (step.getObservation() != null) {
                            emitEvent(emitter, "observation",
                                    truncate(step.getObservation(), 300));
                        }
                    }
                }

                // 推送最终答案
                if (result.getFinalAnswer() != null) {
                    emitEvent(emitter, "final_answer", result.getFinalAnswer());
                }

                emitEvent(emitter, "done", "分析完成（共 " + result.getTotalIterations() + " 轮）");

                log.info("【ReAct 流式 Controller】处理完成");

            } catch (Exception e) {
                log.error("【ReAct 流式 Controller】处理失败", e);
                emitEvent(emitter, "error", "处理失败：" + e.getMessage());
            } finally {
                emitter.complete();
            }
        });

        // 设置超时和错误处理
        emitter.onTimeout(() -> {
            log.warn("【ReAct 流式 Controller】超时");
            emitter.complete();
        });

        emitter.onError((e) -> {
            log.error("【ReAct 流式 Controller】错误", e);
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
            log.debug("【ReAct SSE】发送事件：{} = {}", event, data);
        } catch (IOException e) {
            log.error("【ReAct SSE】发送事件失败：{} = {}", event, data, e);
        }
    }

    /**
     * 获取工具显示名称
     */
    private String getToolDisplayName(String toolName) {
        return switch (toolName.toLowerCase()) {
            case "salary_analysis" -> "收入维度分析";
            case "growth_analysis" -> "成长维度分析";
            case "market_analysis" -> "市场维度分析";
            case "risk_analysis" -> "风险维度分析";
            default -> toolName;
        };
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
