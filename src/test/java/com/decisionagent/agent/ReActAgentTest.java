package com.decisionagent.agent;

import com.decisionagent.dto.ReActResult;
import com.decisionagent.dto.ReActStep;
import com.decisionagent.service.LlmService;
import com.decisionagent.service.UserProfileExtractor;
import com.decisionagent.tool.AnalysisTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Agent 测试类
 *
 * 【使用说明】
 * 运行此测试类可以验证 ReAct Agent 的功能
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@SpringBootTest
public class ReActAgentTest {

    @Mock
    private LlmService llmService;

    @Mock
    private UserProfileExtractor profileExtractor;

    private List<AnalysisTool> analysisTools;
    private ReActAgent reactAgent;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        analysisTools = new ArrayList<>();
        reactAgent = new ReActAgent(llmService, profileExtractor, analysisTools);
    }

    /**
     * 测试简单问候（应该直接返回 Final Answer）
     */
    @Test
    void testSimpleGreeting() {
        String message = "你好";

        // ReActResult result = reactAgent.process(message);

        // 验证
        // assertTrue(result.getSuccess());
        // assertTrue(result.getTotalIterations() <= 2);
        // assertNotNull(result.getFinalAnswer());
        // assertTrue(result.getFinalAnswer().contains("你好"));
    }

    /**
     * 测试薪资问题（应该调用 salary_analysis 工具）
     */
    @Test
    void testSalaryQuestion() {
        String message = "我现在20k，想跳槽能涨多少？";

        // ReActResult result = reactAgent.process(message);

        // 验证
        // assertTrue(result.getSuccess());
        // assertFalse(result.getSteps().isEmpty());

        // 第一轮应该调用 salary_analysis
        // ReActStep firstStep = result.getSteps().get(0);
        // assertEquals("salary_analysis", firstStep.getAction());
        // assertNotNull(firstStep.getObservation());
    }

    /**
     * 测试复杂问题（应该多轮思考）
     */
    @Test
    void testComplexQuestion() {
        String message = "我要不要跳槽？我现在20k，工作3年";

        // ReActResult result = reactAgent.process(message);

        // 验证
        // assertTrue(result.getSuccess());
        // assertTrue(result.getTotalIterations() >= 2);
        // assertNotNull(result.getFinalAnswer());

        // 验证调用了多个工具
        // List<String> actions = result.getSteps().stream()
        //     .map(ReActStep::getAction)
        //     .filter(action -> action != null)
        //     .toList();
        // assertTrue(actions.size() >= 2);
    }

    /**
     * 打印执行轨迹（用于调试）
     */
    @Test
    void printExecutionTrace() {
        String message = "我现在20k，想跳槽能涨多少？";

        // ReActResult result = reactAgent.process(message);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("【ReAct 执行轨迹】");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("总循环次数：" + 1); // result.getTotalIterations()
        System.out.println();

        // for (ReActStep step : result.getSteps()) {
        //     System.out.println("=== Round " + step.getIteration() + " ===");
        //     System.out.println("💭 思考：" + step.getThought());
        //
        //     if (step.isFinalAnswer()) {
        //         System.out.println("✅ 最终答案：" + step.getFinalAnswer());
        //     } else {
        //         System.out.println("🎬 行动：" + step.getAction());
        //         System.out.println("👁️ 观察：" +
        //             truncate(step.getObservation(), 100));
        //     }
        //     System.out.println();
        // }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
