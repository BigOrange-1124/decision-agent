package com.decisionagent.controller;

import com.decisionagent.agent.ReActAgent;
import com.decisionagent.dto.ReActResult;
import com.decisionagent.dto.ReActStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ReAct 聊天控制器
 *
 * 【核心特性】
 * 使用 ReAct 循环 Agent 处理用户问题
 * 返回完整的思考过程和最终答案
 *
 * 【与 ChatController 的区别】
 * - ChatController：一次性规划所有工具
 * - ReActChatController：逐步思考，每轮选择一个工具
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/react")
@RequiredArgsConstructor
public class ReActChatController {

    /** ReAct Agent */
    private final ReActAgent reactAgent;

    /**
     * 【核心端点】ReAct 聊天接口
     *
     * @param request 聊天请求
     * @return ReAct 执行结果
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("【ReAct Controller】收到请求");
        log.info("消息：{}", message);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // 调用 ReAct Agent
            long startTime = System.currentTimeMillis();
            ReActResult result = reactAgent.process(message);
            long duration = System.currentTimeMillis() - startTime;

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("finalAnswer", result.getFinalAnswer());
            response.put("steps", result.getSteps());
            response.put("totalIterations", result.getTotalIterations());
            response.put("success", result.getSuccess());
            response.put("maxIterationsReached", result.getMaxIterationsReached());
            response.put("durationMs", duration);

            // 构建详细报告
            String detailedReport = buildDetailedReport(result);
            response.put("report", detailedReport);

            log.info("【ReAct Controller】处理完成，耗时：{}ms", duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("【ReAct Controller】处理失败", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "处理失败：" + e.getMessage());
            errorResponse.put("success", false);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 构建详细报告
     */
    private String buildDetailedReport(ReActResult result) {
        StringBuilder report = new StringBuilder();

        report.append("## 🤖 ReAct 职业决策分析报告\n\n");
        report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        report.append("**总循环次数**：").append(result.getTotalIterations()).append(" 轮\n\n");

        // 逐步展示思考过程
        if (result.getSteps() != null && !result.getSteps().isEmpty()) {
            report.append("**思考过程**：\n\n");

            for (ReActStep step : result.getSteps()) {
                report.append("### Round ").append(step.getIteration()).append("\n\n");

                if (step.getThought() != null) {
                    report.append("**💭 思考 (Thought)**：\n");
                    report.append(step.getThought()).append("\n\n");
                }

                if (step.getAction() != null) {
                    report.append("**🎬 行动 (Action)**：\n");
                    report.append("执行工具：").append(getToolDisplayName(step.getAction())).append("\n\n");
                }

                if (step.getObservation() != null) {
                    report.append("**👁️ 观察 (Observation)**：\n");
                    report.append(truncate(step.getObservation(), 200)).append("\n\n");
                }

                report.append("---\n\n");
            }
        }

        report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        report.append("## 💡 最终答案\n\n");
        report.append(result.getFinalAnswer());

        return report.toString();
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

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ReAct Agent is running!");
    }
}
