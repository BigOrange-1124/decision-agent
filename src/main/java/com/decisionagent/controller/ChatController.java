package com.decisionagent.controller;

import com.decisionagent.agent.DecisionAgent;
import com.decisionagent.dto.AgentExecutionResult;
import com.decisionagent.dto.ChatRequest;
import com.decisionagent.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 *
 * 处理职业决策咨询的 REST API
 *
 * 【API 端点】
 * - POST /api/chat - 提交问题，获取分析报告和执行轨迹
 * - GET /api/health - 健康检查
 * - GET / - API 使用说明
 *
 * @author DecisionAgent Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    /** 决策 Agent */
    private final DecisionAgent decisionAgent;

    /**
     * 【核心端点】聊天接口
     *
     * 接收用户的职业决策问题，返回分析报告和完整的执行轨迹
     *
     * 【请求示例】
     * ```json
     * {
     *   "message": "我现在20k，想跳槽能涨多少？"
     * }
     * ```
     *
     * 【响应示例】
     * ```json
     * {
     *   "response": "## 📊 职业决策分析报告\n\n...",
     *   "trace": {
     *     "conversationId": "xxx",
     *     "userMessage": "我现在20k，想跳槽能涨多少？",
     *     "extractedProfile": { "monthlySalary": 20, ... },
     *     "planning": {
     *       "needsAnalysis": true,
     *       "selectedTools": ["salary_analysis"],
     *       "reasoning": "用户询问薪资涨幅"
     *     },
     *     "toolExecutions": [
     *       {
     *         "toolName": "salary_analysis",
     *         "displayName": "收入维度分析",
     *         "status": "success",
     *         "result": "【收入维度分析】\n• 当前薪资：20k...",
     *         "durationMs": 15
     *       }
     *     ],
     *     "llmAdvice": "根据你的薪资情况...",
     *     "totalDurationMs": 1234
     *   },
     *   "conversationId": "xxx",
     *   "timestamp": "2024-...,...",
     *   "success": true
     * }
     * ```
     *
     * @param request 聊天请求
     * @return 聊天响应（包含分析报告和执行轨迹）
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("【Controller】收到聊天请求 - 消息：'{}'，会话ID：'{}'",
                request.getMessage(), request.getConversationId());

        try {
            // 处理消息，获取执行结果（包含响应和轨迹）
            long startTime = System.currentTimeMillis();
            AgentExecutionResult result = decisionAgent.process(request.getMessage());
            long duration = System.currentTimeMillis() - startTime;

            log.info("【Controller】Agent 处理完成，耗时：{}ms", duration);

            // 构建响应
            String conversationId = result.getTrace() != null
                    ? result.getTrace().getConversationId()
                    : (request.getConversationId() != null
                        ? request.getConversationId()
                        : java.util.UUID.randomUUID().toString());

            // 返回成功响应（包含完整的执行轨迹）
            ChatResponse response = ChatResponse.success(
                    result.getResponse(),
                    result.getTrace(),
                    conversationId
            );

            log.debug("【Controller】返回响应，会话ID：{}", conversationId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("【Controller】处理请求失败", e);

            // 返回错误响应
            ChatResponse errorResponse = ChatResponse.error(
                    "抱歉，处理您的请求时出现错误：" + e.getMessage()
            );

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 健康检查端点
     *
     * GET /api/health
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Decision Agent is running! " + java.time.LocalDateTime.now());
    }

    /**
     * 根路径端点（API 使用说明）
     *
     * GET /
     *
     * @return 欢迎信息和 API 文档
     */
    @GetMapping("/")
    public ResponseEntity<String> root() {
        String info = """
                # Decision Agent API（模型驱动版本）

                Welcome to the AI-powered Career Decision Agent!

                这是一个**模型驱动的 Agent**，LLM 会根据用户问题智能选择需要的分析工具。

                ## 特性

                - ✅ **LLM 决策**：根据问题自动选择分析维度
                - ✅ **执行轨迹**：返回完整的思考过程
                - ✅ **动态工具调用**：只调用必要的分析工具
                - ✅ **智能降级**：简单问题直接回答

                ## API 端点

                ### POST /api/chat
                提交职业决策问题，获取分析报告和执行轨迹。

                **请求示例：**
                ```json
                {
                  "message": "我要不要跳槽？我现在月薪20k，工作3年，Java开发"
                }
                ```

                **响应示例：**
                ```json
                {
                  "response": "## 📊 职业决策分析报告\\n\\n━━━━━━━━━━━━━━━━...",
                  "trace": {
                    "conversationId": "a1b2c3d4",
                    "userMessage": "我要不要跳槽？...",
                    "extractedProfile": {
                      "monthlySalary": 20,
                      "workYears": 3,
                      "currentPosition": "Java开发"
                    },
                    "planning": {
                      "needsAnalysis": true,
                      "selectedTools": ["salary_analysis", "growth_analysis", "market_analysis"],
                      "reasoning": "用户询问薪资、成长和市场时机"
                    },
                    "toolExecutions": [
                      {
                        "toolName": "salary_analysis",
                        "displayName": "收入维度分析",
                        "status": "success",
                        "result": "【收入维度分析】\\n• 当前薪资：20k...",
                        "durationMs": 15
                      },
                      {
                        "toolName": "growth_analysis",
                        "displayName": "成长维度分析",
                        "status": "success",
                        "result": "【成长维度分析】\\n• 职业阶段：成长阶段...",
                        "durationMs": 12
                      },
                      {
                        "toolName": "market_analysis",
                        "displayName": "市场维度分析",
                        "status": "success",
                        "result": "【市场维度分析】\\n• 当前时间：MARCH...",
                        "durationMs": 10
                      }
                    ],
                    "skippedAnalysis": false,
                    "llmAdvice": "根据你的情况，建议跳槽...",
                    "finalResponse": "完整报告...",
                    "startTime": "2024-03-19T10:30:00",
                    "endTime": "2024-03-19T10:30:02",
                    "totalDurationMs": 2345
                  },
                  "conversationId": "a1b2c3d4",
                  "timestamp": "2024-03-19T10:30:02",
                  "success": true
                }
                ```

                ### GET /api/health
                检查服务健康状态。

                ## 使用示例

                ### cURL
                ```bash
                curl -X POST http://localhost:8080/api/chat \\
                  -H "Content-Type: application/json" \\
                  -d '{"message": "我要不要跳槽？我现在月薪20k，工作3年"}'
                ```

                ### JavaScript
                ```javascript
                fetch('http://localhost:8080/api/chat', {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify({
                    message: '我要不要跳槽？我现在月薪20k，工作3年'
                  })
                })
                .then(res => res.json())
                .then(data => {
                  console.log('回复：', data.response);
                  console.log('执行轨迹：', data.trace);
                });
                ```

                ## 前端展示建议

                ### 1. 展示 LLM 规划结果
                ```javascript
                // 显示 LLM 选择了哪些工具
                trace.planning.selectedTools.forEach(tool => {
                  console.log(`• ${getToolDisplayName(tool)}`);
                });
                console.log(`原因：${trace.planning.reasoning}`);
                ```

                ### 2. 展示工具执行过程
                ```javascript
                // 显示每个工具的执行结果
                trace.toolExecutions.forEach(step => {
                  console.log(`${step.displayName} (${step.durationMs}ms)`);
                  console.log(step.result);
                });
                ```

                ### 3. 展示完整思考过程
                ```javascript
                // 用户输入 → 提取信息 → LLM 规划 → 执行工具 → 生成建议
                console.log('1️⃣ 用户输入：', trace.userMessage);
                console.log('2️⃣ 提取信息：', trace.extractedProfile);
                console.log('3️⃣ LLM 规划：', trace.planning);
                console.log('4️⃣ 执行工具：', trace.toolExecutions);
                console.log('5️⃣ LLM 建议：', trace.llmAdvice);
                ```

                ## 配置

                记得在 application.yml 中设置 LLM API Key：
                ```yaml
                llm:
                  api-key: your-api-key-here
                  base-url: https://open.bigmodel.cn/api/paas/v4
                  model-name: glm-4-flash
                ```
                """;

        return ResponseEntity.ok(info);
    }
}
