package com.decisionagent.service;

import com.decisionagent.dto.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Profile Extractor Test
 *
 * Tests the profile extraction functionality.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
class UserProfileExtractorTest {

    private UserProfileExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new UserProfileExtractor();
    }

    @Test
    void testExtractSalaryWithK() {
        String message = "我现在月薪20k，工作3年";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertEquals(new BigDecimal("20"), profile.getMonthlySalary());
        assertEquals(3, profile.getWorkYears());
    }

    @Test
    void testExtractSalaryWithWan() {
        String message = "我现在月薪2万，工作3年";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertEquals(new BigDecimal("2"), profile.getMonthlySalary());
        assertEquals(3, profile.getWorkYears());
    }

    @Test
    void testExtractWorkYears() {
        String message = "我有5年工作经验";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertEquals(5, profile.getWorkYears());
    }

    @Test
    void testExtractPosition() {
        String message = "我做Java开发，月薪20k";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertEquals("Java开发", profile.getCurrentPosition());
        assertEquals(new BigDecimal("20"), profile.getMonthlySalary());
    }

    @Test
    void testComplexMessage() {
        String message = "我要不要跳槽？我现在月薪20k，工作3年，做后端开发";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertEquals(new BigDecimal("20"), profile.getMonthlySalary());
        assertEquals(3, profile.getWorkYears());
        assertEquals("后端开发", profile.getCurrentPosition());
        assertEquals("job_change", profile.getMainConcern());
    }

    @Test
    void testMessageWithoutNumbers() {
        String message = "我想跳槽，不知道该怎么选择";
        UserProfile profile = extractor.extract(message);

        assertNotNull(profile);
        assertNull(profile.getMonthlySalary());
        assertNull(profile.getWorkYears());
        assertEquals("job_change", profile.getMainConcern());
    }
}
