package com.decisionagent.service;

import com.decisionagent.dto.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User Profile Extractor Service
 *
 * Extracts structured user information from natural language messages.
 * Uses regex patterns to identify salary, work years, and other key information.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class UserProfileExtractor {

    // Pattern for salary extraction (e.g., "20k", "20K", "20", "2万")
    private static final Pattern SALARY_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)[kKwW万千]|月薪?(\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for work years extraction (e.g., "3年", "三年")
    private static final Pattern WORK_YEARS_PATTERN = Pattern.compile(
            "(\\d+)\\s*年|工作(\\d+)\\s*年|经验(\\d+)\\s*年",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern for position/role extraction
    private static final Pattern POSITION_PATTERN = Pattern.compile(
            "(?:做|担任|职位是|岗位是|作为)\\s*([^，。！？\\s]{2,10})",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extract user profile from message
     *
     * @param message user's natural language message
     * @return extracted UserProfile
     */
    public UserProfile extract(String message) {
        log.debug("Extracting user profile from message: {}", message);

        UserProfile.UserProfileBuilder builder = UserProfile.builder();

        // Extract salary
        BigDecimal salary = extractSalary(message);
        builder.monthlySalary(salary);

        // Extract work years
        Integer workYears = extractWorkYears(message);
        builder.workYears(workYears);

        // Extract position
        String position = extractPosition(message);
        builder.currentPosition(position);

        // Determine main concern
        String mainConcern = determineMainConcern(message);
        builder.mainConcern(mainConcern);

        // Store full message as context
        builder.additionalContext(message);

        UserProfile profile = builder.build();
        log.info("Extracted user profile: {}", profile);

        return profile;
    }

    private BigDecimal extractSalary(String message) {
        Matcher matcher = SALARY_PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                // Try to match "20k" or "20K" format
                if (matcher.group(1) != null) {
                    return new BigDecimal(matcher.group(1));
                }
                // Try to match "2万" format
                if (matcher.group(1) != null) {
                    String value = matcher.group(1).toLowerCase();
                    if (value.contains("w") || value.contains("万")) {
                        return new BigDecimal(value.replaceAll("[w万千]", ""));
                    }
                }
                // Try to match second group for plain number
                if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                    return new BigDecimal(matcher.group(2));
                }
            } catch (Exception e) {
                log.warn("Failed to parse salary from match: {}", matcher.group(), e);
            }
        }

        return null;
    }

    private Integer extractWorkYears(String message) {
        Matcher matcher = WORK_YEARS_PATTERN.matcher(message);

        if (matcher.find()) {
            try {
                // Check all groups for a match
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        return Integer.parseInt(matcher.group(i));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse work years from match: {}", matcher.group(), e);
            }
        }

        return null;
    }

    private String extractPosition(String message) {
        Matcher matcher = POSITION_PATTERN.matcher(message);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String determineMainConcern(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("跳槽") || lowerMessage.contains("换工作")) {
            return "job_change";
        } else if (lowerMessage.contains("薪资") || lowerMessage.contains("工资") || lowerMessage.contains("涨薪")) {
            return "salary_increase";
        } else if (lowerMessage.contains("成长") || lowerMessage.contains("学习") || lowerMessage.contains("提升")) {
            return "career_growth";
        } else if (lowerMessage.contains("风险") || lowerMessage.contains("稳定")) {
            return "risk_assessment";
        } else {
            return "general_consultation";
        }
    }
}
