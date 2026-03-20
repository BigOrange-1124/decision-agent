/**
 * ReActAgentChat.jsx
 *
 * ReAct Agent 聊天组件 - React 版本
 *
 * 【功能特性】
 * - 使用 SSE (Server-Sent Events) 实时接收 AI 思考过程
 * - 展示完整的 ReAct 循环（Thought → Action → Observation）
 * - 美观的 UI 设计，带有动画效果
 * - 支持示例问题快速输入
 *
 * 【使用方法】
 * 1. 安装依赖：npm install
 * 2. 导入组件：import ReActAgentChat from './ReActAgentChat';
 * 3. 使用组件：<ReActAgentChat apiUrl="http://localhost:8199" />
 *
 * @author DecisionAgent Team
 * @version 3.0.0
 */

import React, { useState, useEffect, useRef } from 'react';
import './ReActAgentChat.css';

// ============================================================================
// 配置
// ============================================================================

const DEFAULT_CONFIG = {
  apiUrl: 'http://localhost:8199',
  reconnectAttempts: 3,
  reconnectDelay: 1000,
};

// ============================================================================
// 主组件
// ============================================================================

const ReActAgentChat = ({ apiUrl = DEFAULT_CONFIG.apiUrl }) => {
  // ==================== 状态管理 ====================

  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      type: 'ai',
      content: '你好！我是职业决策助手 🤖\n\n我会像人类一样逐步思考，帮助你分析职业决策。',
      isWelcome: true,
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [currentRound, setCurrentRound] = useState(null);

  // ==================== Ref ====================

  const eventSourceRef = useRef(null);
  const chatMessagesRef = useRef(null);
  const currentAIMessageRef = useRef(null);

  // ==================== 示例问题 ====================

  const exampleQuestions = [
    { icon: '💰', text: '我现在20k，想跳槽能涨多少？' },
    { icon: '🤔', text: '我要不要跳槽？我现在20k，工作3年' },
    { icon: '📊', text: '现在适合跳槽吗？' },
    { icon: '⚠️', text: '跳槽有风险吗？' },
  ];

  // ==================== 核心方法 ====================

  /**
   * 发送消息
   */
  const sendMessage = async (message = inputValue) => {
    if (!message.trim() || isProcessing) {
      return;
    }

    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: message,
    };
    setMessages((prev) => [...prev, userMessage]);

    // 清空输入框
    setInputValue('');
    setIsProcessing(true);

    // 创建 AI 消息容器
    const aiMessageId = Date.now() + 1;
    const aiMessage = {
      id: aiMessageId,
      type: 'ai',
      rounds: [],
      status: 'thinking',
    };
    setMessages((prev) => [...prev, aiMessage]);
    currentAIMessageRef.current = aiMessageId;

    // 连接 SSE
    connectToReActAgent(message, aiMessageId);
  };

  /**
   * 连接到 ReAct Agent SSE 接口
   */
  const connectToReActAgent = (message, messageId) => {
    const url = `${apiUrl}/api/react/chat/stream?message=${encodeURIComponent(message)}`;
    const eventSource = new EventSource(url);

    eventSourceRef.current = eventSource;

    let currentRoundData = null;

    // 开始事件
    eventSource.addEventListener('start', (e) => {
      updateMessage(messageId, { status: 'thinking' });
    });

    // 轮次事件
  eventSource.addEventListener('round', (e) => {
      currentRoundData = {
        number: parseInt(e.data.replace('Round ', '')),
        thought: null,
        action: null,
        observation: null,
      };

      addRound(messageId, currentRoundData);
      setCurrentRound(currentRoundData.number);
    });

    // 思考事件
    eventSource.addEventListener('thought', (e) => {
      if (currentRoundData) {
        currentRoundData.thought = e.data;
        updateRound(messageId, currentRoundData);
      }
    });

    // 行动事件
    eventSource.addEventListener('action', (e) => {
      if (currentRoundData) {
        currentRoundData.action = e.data;
        updateRound(messageId, currentRoundData);
      }
    });

    // 观察事件
    eventSource.addEventListener('observation', (e) => {
      if (currentRoundData) {
        currentRoundData.observation = e.data;
        updateRound(messageId, currentRoundData);
        setCurrentRound(null); // 完成当前轮次
      }
    });

    // 最终答案事件
    eventSource.addEventListener('final_answer', (e) => {
      updateMessage(messageId, {
        finalAnswer: e.data,
        status: 'done',
      });
    });

    // 完成事件
    eventSource.addEventListener('done', (e) => {
      setTimeout(() => {
        finishProcessing();
      }, 500);
    });

    // 错误事件
    eventSource.addEventListener('error', (e) => {
      updateMessage(messageId, {
        error: e.data,
        status: 'error',
      });
      finishProcessing();
    });

    // 连接关闭
    eventSource.onerror = () => {
      finishProcessing();
    };
  };

  /**
   * 添加思考轮次
   */
  const addRound = (messageId, roundData) => {
    setMessages((prev) =>
      prev.map((msg) => {
        if (msg.id === messageId) {
          return {
            ...msg,
            rounds: [...msg.rounds, roundData],
          };
        }
        return msg;
      })
    );
  };

  /**
   * 更新思考轮次
   */
  const updateRound = (messageId, roundData) => {
    setMessages((prev) =>
      prev.map((msg) => {
        if (msg.id === messageId) {
          return {
            ...msg,
            rounds: msg.rounds.map((round) =>
              round.number === roundData.number ? roundData : round
            ),
          };
        }
        return msg;
      })
    );
  };

  /**
   * 更新消息
   */
  const updateMessage = (messageId, updates) => {
    setMessages((prev) =>
      prev.map((msg) => (msg.id === messageId ? { ...msg, ...updates } : msg))
    );
  };

  /**
   * 完成处理
   */
  const finishProcessing = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    setIsProcessing(false);
    setCurrentRound(null);
  };

  // ==================== 辅助方法 ====================

  /**
   * 使用示例问题
   */
  const useExample = (question) => {
    setInputValue(question);
    sendMessage(question);
  };

  /**
   * 处理回车键
   */
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  /**
   * 滚动到底部
   */
  useEffect(() => {
    if (chatMessagesRef.current) {
      chatMessagesRef.current.scrollTop = chatMessagesRef.current.scrollHeight;
    }
  }, [messages]);

  // ==================== 渲染 ====================

  return (
    <div className="react-agent-chat">
      <div className="chat-header">
        <h1>🤖 ReAct Agent</h1>
        <p>AI 职业决策助手 - 会思考、会行动、会观察</p>
      </div>

      <div className="chat-container">
        <div className="chat-messages" ref={chatMessagesRef}>
          {messages.map((message) => (
            <MessageBubble key={message.id} message={message} />
          ))}
        </div>

        <div className="input-area">
          <div className="input-wrapper">
            <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="输入你的问题..."
              disabled={isProcessing}
            />
            <button
              onClick={() => sendMessage()}
              disabled={isProcessing || !inputValue.trim()}
            >
              {isProcessing ? '思考中...' : '发送'}
            </button>
          </div>
        </div>
      </div>

      <div className="examples-section">
        <h3>💡 试试这些问题</h3>
        <div className="examples-grid">
          {exampleQuestions.map((example, index) => (
            <div
              key={index}
              className="example-item"
              onClick={() => useExample(example.text)}
            >
              <span className="example-icon">{example.icon}</span>
              <span className="example-text">{example.text}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// ============================================================================
// 消息气泡组件
// ============================================================================

const MessageBubble = ({ message }) => {
  if (message.type === 'user') {
    return (
      <div className="message user-message">
        <div className="bubble">{message.content}</div>
      </div>
    );
  }

  return (
    <div className="message ai-message">
      <div className="bubble">
        {message.isWelcome ? (
          <WelcomeMessage />
        ) : (
          <AIMessage message={message} />
        )}
      </div>
    </div>
  );
};

// ============================================================================
// AI 消息组件
// ============================================================================

const AIMessage = ({ message }) => {
  const { status, rounds, finalAnswer, error } = message;

  if (error) {
    return <div className="error-message">❌ 错误：{error}</div>;
  }

  if (status === 'thinking') {
    return (
      <>
        <div className="status-badge thinking">🤔 AI 正在思考...</div>
        {rounds.map((round) => (
          <ThinkingRound key={round.number} round={round} />
        ))}
        <div className="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </>
    );
  }

  if (finalAnswer) {
    return (
      <>
        {rounds.map((round) => (
          <ThinkingRound key={round.number} round={round} />
        ))}
        <div className="final-answer">
          <h3>✅ 最终答案</h3>
          <div className="answer-content">{formatAnswer(finalAnswer)}</div>
        </div>
      </>
    );
  }

  return null;
};

// ============================================================================
// 欢迎消息组件
// ============================================================================

const WelcomeMessage = () => (
  <>
    <strong>你好！</strong> 我是职业决策助手 🤖<br /><br />
    我会像人类一样逐步思考，帮助你分析职业决策。<br /><br />
    你可以问我：<br />
    • 💰 "我现在20k，想跳槽能涨多少？"<br />
    • 📈 "我要不要跳槽？"<br />
    • 📊 "现在适合跳槽吗？"
  </>
);

// ============================================================================
// 思考轮次组件
// ============================================================================

const ThinkingRound = ({ round }) => {
  const { number, thought, action, observation } = round;

  return (
    <div className="thinking-round">
      <div className="round-header">
        <span className="round-icon">💭</span>
        <span className="round-number">Round {number}</span>
        {!observation && (
          <div className="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        )}
      </div>

      {thought && (
        <div className="round-content">
          <strong>💭 思考：</strong>
          <span>{thought}</span>
        </div>
      )}

      {action && (
        <div className="round-content">
          <strong>🎬 行动：</strong>
          <span>{action}</span>
        </div>
      )}

      {observation && (
        <div className="round-content">
          <strong>👁️ 观察：</strong>
          <span>{observation}</span>
        </div>
      )}
    </div>
  );
};

// ============================================================================
// 工具函数
// ============================================================================

/**
 * 格式化答案（Markdown → HTML）
 */
const formatAnswer = (text) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/• /g, '• ');
};

// ============================================================================
// 导出
// ============================================================================

export default ReActAgentChat;
