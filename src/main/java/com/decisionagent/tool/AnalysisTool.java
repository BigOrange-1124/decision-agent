package com.decisionagent.tool;

import com.decisionagent.dto.UserProfile;

/**
 * AnalysisTool Interface
 *
 * Abstract interface for all analysis tools in the decision agent system.
 * Each tool should analyze a specific dimension of the user's situation.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
public interface AnalysisTool {

    /**
     * Get the name of this analysis tool
     *
     * @return tool name
     */
    String getName();

    /**
     * Get the description of what this tool analyzes
     *
     * @return tool description
     */
    String getDescription();

    /**
     * Analyze the user's profile from this tool's perspective
     *
     * @param profile user profile information
     * @param userMessage original user message
     * @return analysis result as a formatted string
     */
    String analyze(UserProfile profile, String userMessage);

    /**
     * Get the order priority of this tool (lower number = higher priority)
     *
     * @return priority order
     */
    default int getOrder() {
        return 100;
    }
}
