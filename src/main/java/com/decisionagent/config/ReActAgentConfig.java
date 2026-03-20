package com.decisionagent.config;

import com.decisionagent.agent.ReActAgent;
import com.decisionagent.service.LlmService;
import com.decisionagent.service.UserProfileExtractor;
import com.decisionagent.tool.AnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ReAct Agent 配置类
 *
 * 【职责】
 * 确保 ReActAgent 正确初始化并注入所有依赖
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReActAgentConfig {

    private final LlmService llmService;
    private final UserProfileExtractor profileExtractor;
    private final List<AnalysisTool> analysisTools;

    /**
     * 创建 ReActAgent Bean
     *
     * 【说明】
     * 虽然ReActAgent已经有@Component注解，
     * 但通过@Bean方式可以显式地控制初始化顺序
     *
     * @return ReActAgent 实例
     */
    @Bean
    public ReActAgent reactAgent() {
        log.info("【ReAct 配置】初始化 ReActAgent");
        log.info("【ReAct 配置】可用工具数量：{}", analysisTools.size());

        for (AnalysisTool tool : analysisTools) {
            log.info("【ReAct 配置】  - {}", tool.getName());
        }

        return new ReActAgent(llmService, profileExtractor, analysisTools);
    }
}
