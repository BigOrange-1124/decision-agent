package com.decisionagent.tool.impl;

import com.decisionagent.dto.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Salary Analysis Tool Test
 *
 * Tests the salary analysis functionality.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
class SalaryAnalysisToolTest {

    private SalaryAnalysisTool tool;

    @BeforeEach
    void setUp() {
        tool = new SalaryAnalysisTool();
    }

    @Test
    void testGetName() {
        assertEquals("收入维度分析", tool.getName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(tool.getDescription());
        assertTrue(tool.getDescription().contains("薪资"));
    }

    @Test
    void testGetOrder() {
        assertEquals(1, tool.getOrder());
    }

    @Test
    void testAnalyzeWithNormalSalary() {
        UserProfile profile = UserProfile.builder()
                .monthlySalary(new BigDecimal("20"))
                .workYears(3)
                .build();

        String result = tool.analyze(profile, "我要不要跳槽？");

        assertNotNull(result);
        assertTrue(result.contains("收入维度分析"));
        assertTrue(result.contains("20.0k"));
        assertTrue(result.contains("市场对比"));
    }

    @Test
    void testAnalyzeWithLowSalary() {
        UserProfile profile = UserProfile.builder()
                .monthlySalary(new BigDecimal("10"))
                .workYears(3)
                .build();

        String result = tool.analyze(profile, "我要不要跳槽？");

        assertNotNull(result);
        assertTrue(result.contains("偏低"));
        assertTrue(result.contains("提升空间"));
    }

    @Test
    void testAnalyzeWithHighSalary() {
        UserProfile profile = UserProfile.builder()
                .monthlySalary(new BigDecimal("40"))
                .workYears(3)
                .build();

        String result = tool.analyze(profile, "我要不要跳槽？");

        assertNotNull(result);
        assertTrue(result.contains("高于市场"));
    }

    @Test
    void testAnalyzeWithoutSalary() {
        UserProfile profile = UserProfile.builder()
                .workYears(3)
                .build();

        String result = tool.analyze(profile, "我要不要跳槽？");

        assertNotNull(result);
        assertTrue(result.contains("未获取到薪资信息"));
    }
}
