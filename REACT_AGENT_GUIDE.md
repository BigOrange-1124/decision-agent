# 🔄 ReAct Agent 使用指南

## 🎯 什么是 ReAct Agent？

**ReAct** = **Re**asoning（推理）+ **Act**ing（行动）

与传统的"一次性规划"Agent不同，ReAct Agent 会：

| 特性 | 一次性规划 Agent | ReAct Agent |
|------|------------------|-------------|
| **规划方式** | 一次性选择所有工具 | 逐步思考，每轮选一个工具 |
| **执行顺序** | 按固定顺序执行 | 根据结果动态调整 |
| **思考过程** | 隐藏 | 完全可见 |
| **适应能力** | 无 | 可根据观察结果调整策略 |

---

## 🧠 ReAct 循环模式

```
┌─────────────────────────────────────────────────┐
│               ReAct 循环                         │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  1. Thought（思考）                        │ │
│  │     "我需要做什么？"                      │ │
│  └──────────────┬────────────────────────────┘ │
│                 ↓                                │
│  ┌───────────────────────────────────────────┐ │
│  │  2. Action（行动）                        │ │
│  │     "选择一个工具"                        │ │
│  └──────────────┬────────────────────────────┘ │
│                 ↓                                │
│  ┌───────────────────────────────────────────┐ │
│  │  3. Observation（观察）                   │ │
│  │     "获取工具结果"                        │ │
│  └──────────────┬────────────────────────────┘ │
│                 ↓                                │
│  ┌───────────────────────────────────────────┐ │
│  │  4. 决策                                  │ │
│  │     • 继续下一轮？                         │ │
│  │     • 或给出 Final Answer？              │ │
│  └──────────────┬────────────────────────────┘ │
│                 ↓                                │
│          回到 Step 1                           │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 📝 执行示例

### 示例 1：简单问题（单轮）

**用户输入**：
```
你好
```

**Round 1**：
```
Thought: 用户在打招呼，不需要调用工具，直接回复
Final Answer: 你好！我是职业决策助手，有什么可以帮你的吗？
```

---

### 示例 2：薪资问题（2轮）

**用户输入**：
```
我现在20k，想跳槽能涨多少？
```

**Round 1**：
```
Thought: 用户询问薪资涨幅，我需要先分析当前收入水平
Action: salary_analysis
Action Input: N/A

Observation: 【收入维度分析】
• 当前薪资：20k
• 市场对比：当前薪资约为市场平均水平的80%
• 薪资评估：⚠️ 你的薪资偏低，跳槽有较大提升空间（预计20-40%）
• 预期薪资：23k - 26k
```

**Round 2**：
```
Thought: 已了解到薪资偏低，结合当前金三银四期间，可以给出建议
Final Answer: 根据分析，建议跳槽。你的当前薪资20k低于市场平均25k，加上当前是跳槽黄金期，预计涨幅可达20-30%，目标薪资23k-26k。
```

---

### 示例 3：复杂问题（3轮）

**用户输入**：
```
我要不要跳槽？我现在20k，工作3年
```

**Round 1**：
```
Thought: 用户询问跳槽建议，我需要先分析收入情况
Action: salary_analysis
Action Input: N/A

Observation: 当前薪资20k，低于市场平均...
```

**Round 2**：
```
Thought: 了解到薪资偏低，现在需要分析市场时机
Action: market_analysis
Action Input: N/A

Observation: 当前是金三银四期间，机会较多...
```

**Round 3**：
```
Thought: 已收集到收入和市场信息，3年经验是跳槽黄金期，可以给出综合建议
Final Answer: 根据分析，建议跳槽。理由：
1. 当前薪资20k低于市场平均25k
2. 3年经验是跳槽黄金期
3. 当前处于金三银四期间
预计涨幅20-30%，目标薪资23k-26k。
```

---

## 🚀 API 使用

### 1. 普通接口（一次性返回）

**接口**：`POST /api/react/chat`

**请求**：
```json
{
  "message": "我现在20k，想跳槽能涨多少？"
}
```

**响应**：
```json
{
  "finalAnswer": "根据分析，建议跳槽...",
  "steps": [
    {
      "iteration": 1,
      "thought": "用户询问薪资涨幅...",
      "action": "salary_analysis",
      "actionInput": "N/A",
      "observation": "【收入维度分析】..."
    },
    {
      "iteration": 2,
      "thought": "已了解到薪资偏低...",
      "finalAnswer": "根据分析，建议跳槽..."
    }
  ],
  "totalIterations": 2,
  "success": true,
  "maxIterationsReached": false,
  "durationMs": 1234,
  "report": "## 🤖 ReAct 职业决策分析报告..."
}
```

### 2. 流式接口（实时推送）

**接口**：`GET /api/react/chat/stream?message=xxx`

**SSE 事件**：
```
event: start
data: 开始 ReAct 分析...

event: round
data: Round 1

event: thought
data: 用户询问薪资涨幅，我需要先分析当前收入水平

event: action
data: 收入维度分析

event: observation
data: 【收入维度分析】• 当前薪资：20k...

event: round
data: Round 2

event: thought
data: 已了解到薪资偏低，可以给出建议

event: final_answer
data: 根据分析，建议跳槽...

event: done
data: 分析完成（共 2 轮）
```

---

## 💻 前端集成示例

### JavaScript（SSE 流式）

```javascript
const eventSource = new EventSource(
  '/api/react/chat/stream?message=我现在20k，想跳槽能涨多少？'
);

eventSource.addEventListener('round', (e) => {
  console.log('开始第 ' + e.data + ' 轮');
  displayRound(e.data);
});

eventSource.addEventListener('thought', (e) => {
  console.log('💭 思考：' + e.data);
  displayThought(e.data);
});

eventSource.addEventListener('action', (e) => {
  console.log('🎬 行动：' + e.data);
  displayAction(e.data);
});

eventSource.addEventListener('observation', (e) => {
  console.log('👁️ 观察：' + e.data);
  displayObservation(e.data);
});

eventSource.addEventListener('final_answer', (e) => {
  console.log('✅ 最终答案：' + e.data);
  displayFinalAnswer(e.data);
});

eventSource.addEventListener('done', (e) => {
  console.log('✓ 完成：' + e.data);
  eventSource.close();
});

eventSource.addEventListener('error', (e) => {
  console.error('错误：' + e.data);
  eventSource.close();
});
```

---

## 🎨 前端展示建议

### 方式 1：时间线展示

```
┌──────────────────────────────────────────────┐
│  Round 1                                     │
│  ┌────────────────────────────────────────┐  │
│  │ 💭 思考：用户询问薪资涨幅...          │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ 🎬 行动：收入维度分析                 │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ 👁️ 观察：【收入维度分析】...         │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
         ↓
┌──────────────────────────────────────────────┐
│  Round 2                                     │
│  ┌────────────────────────────────────────┐  │
│  │ 💭 思考：已了解到薪资偏低...          │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ ✅ 最终答案：根据分析，建议跳槽...   │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### 方式 2：思维链展示

```
用户：我现在20k，想跳槽能涨多少？
         ↓
[Round 1] 💭 思考：用户询问薪资涨幅，我需要先分析当前收入水平
         ↓
         🎬 行动：收入维度分析
         ↓
         👁️ 观察：当前薪资20k，低于市场平均...
         ↓
[Round 2] 💭 思考：已了解到薪资偏低，可以给出建议
         ↓
         ✅ 最终答案：根据分析，建议跳槽，预计涨幅20-30%
```

---

## ⚙️ 配置说明

### 最大循环次数

在 `ReActAgent.java` 中修改：

```java
private static final int MAX_ITERATIONS = 5;  // 默认 5 轮
```

**建议值**：
- 简单问题：2-3 轮
- 复杂问题：3-5 轮
- 不要超过 10 轮（会降低用户体验）

### LLM 模型选择

在 `application.yml` 中：

```yaml
llm:
  # 推荐：更强的模型
  model-name: glm-4-plus  # 或 gpt-4

  # 或：更快的模型
  model-name: glm-4-flash  # 或 gpt-4o-mini
```

---

## 🔍 调试技巧

### 1. 查看日志

```bash
# 启动时查看详细日志
tail -f logs/application.log | grep ReAct
```

### 2. 检查 Prompt

日志中会输出每一轮的 Prompt：

```
【ReAct Round 1】Prompt:
=== 用户问题 ===
我现在20k，想跳槽能涨多少？

=== 用户信息 ===
• 薪资：20k

=== 请继续 ===
基于以上信息，请给出下一步的 Thought、Action 或 Final Answer。
```

### 3. 查看 LLM 原始输出

```
【ReAct Round 1】LLM 输出:
Thought: 用户询问薪资涨幅，我需要先分析当前收入水平
Action: salary_analysis
Action Input: N/A
```

---

## 📊 性能对比

| Agent 类型 | 平均响应时间 | LLM 调用次数 | 准确性 |
|-----------|-------------|-------------|--------|
| 一次性规划 Agent | 2-3 秒 | 2 次 | ⭐⭐⭐ |
| ReAct Agent | 5-8 秒 | 2-6 次 | ⭐⭐⭐⭐⭐ |

**说明**：
- ReAct Agent 会进行多轮思考，响应时间更长
- 但准确性更高，因为每一步都基于观察结果调整
- 用户体验更好，可以看到完整的思考过程

---

## 🎯 最佳实践

### 1. Prompt 优化

在 `REACT_SYSTEM_PROMPT` 中：
- ✅ 提供清晰的示例
- ✅ 严格定义输出格式
- ✅ 强调只能调用一个工具
- ✅ 提醒何时给出 Final Answer

### 2. 工具选择

- ✅ 工具名称要清晰（salary_analysis）
- ✅ 工具描述要准确
- ✅ 工具执行要稳定

### 3. 上下文管理

- ✅ 每轮都要传入历史步骤
- ✅ Observation 要截断（避免 token 过多）
- ✅ 最终答案要完整（不截断）

---

## 🚀 下一步

### 进阶功能

1. **多轮对话**：支持上下文记忆
2. **工具参数化**：让 LLM 传递参数给工具
3. **自我反思**：每轮后评估结果质量
4. **规划能力**：提前规划后续步骤

### 扩展方向

1. **更多工具**：添加更多分析维度
2. **知识库**：接入外部知识库
3. **代码执行**：支持执行代码进行数据分析
4. **多 Agent 协作**：多个 Agent 协同工作

---

**让 AI 像人类一样思考和行动！** 🤖✨
