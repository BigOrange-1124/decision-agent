package com.decisionagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * User Profile Data Transfer Object
 *
 * Contains extracted user information for analysis.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    /**
     * Current monthly salary (in thousands, e.g., 20 means 20k)
     */
    private BigDecimal monthlySalary;

    /**
     * Work experience in years
     */
    private Integer workYears;

    /**
     * Current company/industry
     */
    private String currentCompany;

    /**
     * Target company/industry (if mentioned)
     */
    private String targetCompany;

    /**
     * Current position/role
     */
    private String currentPosition;

    /**
     * User's main concern or goal
     */
    private String mainConcern;

    /**
     * Additional context information
     */
    private String additionalContext;

    /**
     * Check if the profile is empty (no meaningful information extracted)
     *
     * @return true if all fields are null or empty
     */
    public boolean isEmpty() {
        return monthlySalary == null
                && workYears == null
                && (currentCompany == null || currentCompany.isEmpty())
                && (targetCompany == null || targetCompany.isEmpty())
                && (currentPosition == null || currentPosition.isEmpty())
                && (mainConcern == null || mainConcern.isEmpty())
                && (additionalContext == null || additionalContext.isEmpty());
    }
}
