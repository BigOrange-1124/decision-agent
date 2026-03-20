package com.decisionagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求数据传输对象
 *
 * 【用途说明】
 * 这是 POST /api/chat 接口的请求体结构
 * 前端需要按照这个格式发送数据
 *
 * 【请求示例】
 * {
 *   "message": "我现在20k，想跳槽能涨多少？",
 *   "conversationId": "abc-123"  // 可选
 * }
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Data  // Lombok 注解：自动生成 getter/setter
@Builder  // Lombok 注解：支持构建者模式
@NoArgsConstructor  // Lombok 注解：生成无参构造函数
@AllArgsConstructor  // Lombok 注解：生成全参构造函数
public class ChatRequest {

    /**
     * 用户的消息内容
     *
     * 【验证规则】
     * - @NotBlank：不能为 null、空字符串或纯空格
     * - message：验证失败时的提示信息
     *
     * 【示例】
     * - "我要不要跳槽？"
     * - "现在20k，能涨多少？"
     */
    @NotBlank(message = "消息不能为空")
    private String message;

    /**
     * 会话 ID（可选）
     *
     * 【用途】
     * - 用于追踪同一个会话的多次对话
     * - 前端可以生成 UUID 作为会话 ID
     * - 后端会返回这个 ID，便于前端关联
     *
     * 【示例】
     * - "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
     * - 如果不提供，后端会自动生成
     */
    private String conversationId;
}
