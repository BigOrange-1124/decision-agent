package com.decisionagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat Request Data Transfer Object
 *
 * Request payload for the chat endpoint.
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * User's message/question
     */
    @NotBlank(message = "Message cannot be empty")
    private String message;

    /**
     * Optional conversation ID for context tracking
     */
    private String conversationId;
}
