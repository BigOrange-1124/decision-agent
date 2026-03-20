# 🆚 两种 Agent 模式对比

## 一图看懂区别

```
┌─────────────────────────────────────────────────────────────┐
│          一次性规划 Agent vs ReAct 循环 Agent               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  一次性规划 Agent（DecisionAgent）                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 1. 用户输入                                           │  │
│  │    ↓                                                  │  │
│  │ 2. LLM 一次性规划所有工具                              │  │
│  │    "我需要 salary_analysis, growth_analysis..."       │  │
│  │    ↓                                                  │  │
│  │ 3. 按顺序执行所有工具                                  │  │
│  │    salary → growth → market → risk                   │  │
│  │    ↓                                                  │  │
│  │ 4. LLM 总结所有结果                                    │  │
│  │    "根据以上分析..."                                   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ReAct 循环 Agent（ReActAgent）                            │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Round 1:                                              │  │
│  │   💭 Thought: "用户询问薪资，我需要先分析收入"         │  │
│  │   🎬 Action: salary_analysis                          │  │
│  │   👁️ Observation: "当前薪资20k，低于市场平均..."        │  │
│  │    ↓                                                  │  │
│  │ Round 2:                                              │  │
│  │   💭 Thought: "已了解薪资偏低，需要分析市场时机"       │  │
│  │   🎬 Action: market_analysis                          │  │
│  │   👁️ Observation: "当前是金三银四..."                  │  │
│  │    ↓                                                  │  │
│  │ Round 3:                                              │  │
│  │   💭 Thought: "已有足够信息，可以给出建议"             │  │
│  │   ✅ Final Answer: "根据分析，建议跳槽..."            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 详细对比表

| 维度 | 一次性规划 Agent | ReAct 循环 Agent |
|------|------------------|-----------------|
| **核心模式** | Plan（规划）→ Execute（执行）→ Summarize（总结） | Thought（思考）→ Action（行动）→ Observation（观察）→ 循环 |
| **LLM 调用时机** | 开始时规划一次，结束后总结一次 | 每轮都调用 LLM |
| **工具选择方式** | 一次性选择所有需要的工具 | 每轮选择一个工具 |
| **是否适应结果** | ❌ 不适应，按固定顺序执行 | ✅ 根据观察结果动态调整 |
| **思考过程可见性** | ❌ 只显示规划，不显示思考过程 | ✅ 每轮思考都可见 |
| **执行顺序** | 固定顺序（按规划执行） | 动态顺序（根据思考决定） |
| **平均响应时间** | 2-3 秒 | 5-8 秒 |
| **LLM 调用次数** | 2 次（规划 + 总结） | 2-6 次（每轮一次） |
| **Token 消耗** | 少 | 多（因为每轮都传历史） |
| **准确性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **适用场景** | 简单问题、快速响应 | 复杂问题、深度分析 |

---

## 🎯 使用场景建议

### 一次性规划 Agent 适用于：

```
✅ 简单问候
   "你好"、"谢谢"

✅ 单一维度问题
   "20k在市场上是什么水平？"
   "现在是不是跳槽的好时机？"

✅ 快速响应场景
   需要在 3 秒内给出答案

✅ 明确的问题
   用户问题目标明确，不需要推理
```

### ReAct 循环 Agent 适用于：

```
✅ 复杂决策问题
   "我要不要跳槽？"
   "我现在20k工作3年，想去大厂，有机会吗？"

✅ 需要多维度分析
   涉及收入、成长、市场、风险等多个维度

✅ 需要推理
   需要根据分析结果进行逻辑推理

✅ 需要展示思考过程
   用户希望看到 AI 的完整思考链

✅ 不确定的问题
   用户问题模糊，需要逐步澄清
```

---

## 💡 实际案例对比

### 案例 1：简单问题

**用户输入**：`"你好"`

#### 一次性规划 Agent
```
[规划] LLM 判断无需工具
[总结] 直接返回："你好！我是职业决策助手..."
耗时：1.5 秒
```

#### ReAct 循环 Agent
```
[Round 1]
  Thought: 用户在打招呼，不需要调用工具
  Final Answer: "你好！我是职业决策助手..."
耗时：2.5 秒
```

**结论**：简单问题用一次性规划 Agent 更快 ✅

---

### 案例 2：中等问题

**用户输入**：`"我现在20k，想跳槽能涨多少？"`

#### 一次性规划 Agent
```
[规划]
  LLM 判断：需要 salary_analysis
  思考："用户询问薪资涨幅"

[执行]
  salary_analysis → 返回结果

[总结]
  LLM 生成："根据分析，建议跳槽..."
耗时：2.5 秒
```

#### ReAct 循环 Agent
```
[Round 1]
  Thought: "用户询问薪资涨幅，我需要先分析收入水平"
  Action: salary_analysis
  Observation: "当前薪资20k，低于市场平均..."

[Round 2]
  Thought: "已了解到薪资偏低，可以给出建议"
  Final Answer: "根据分析，建议跳槽..."
耗时：5 秒
```

**结论**：中等问题两者都可以，ReAct 更准确但更慢 ⚖️

---

### 案例 3：复杂问题

**用户输入**：`"我要不要跳槽？我现在20k，工作3年，Java开发，想去字节跳动"`

#### 一次性规划 Agent
```
[规划]
  LLM 判断：需要全部分析
  思考："用户需要综合分析"

[执行]
  salary_analysis → 返回结果
  growth_analysis → 返回结果
  market_analysis → 返回结果
  risk_analysis → 返回结果

[总结]
  LLM 生成："根据以上分析..."
耗时：3.5 秒

问题：
❌ 执行了风险分析（但用户没问）
❌ 不能根据中间结果调整策略
❌ 浪费了 token 和时间
```

#### ReAct 循环 Agent
```
[Round 1]
  Thought: "用户询问跳槽建议，我需要先分析收入情况"
  Action: salary_analysis
  Observation: "当前薪资20k，低于市场平均..."

[Round 2]
  Thought: "已了解薪资偏低，现在分析市场时机"
  Action: market_analysis
  Observation: "当前是金三银四期间..."

[Round 3]
  Thought: "已收集到收入和市场信息，3年经验是黄金期，想去字节跳动需要分析成长空间"
  Action: growth_analysis
  Observation: "3年经验正处于成长阶段..."

[Round 4]
  Thought: "已收集到足够信息，可以给出建议"
  Final Answer: "根据分析，建议跳槽。理由：
    1. 当前薪资20k低于市场平均25k
    2. 3年经验是跳槽黄金期
    3. 当前处于金三银四期间
    4. 字节跳动对3年经验需求较大
    预计涨幅25-35%，目标薪资25k-27k。"
耗时：7 秒

优势：
✅ 只执行了必要的工具
✅ 根据中间结果动态调整
✅ 最终答案更精准
```

**结论**：复杂问题用 ReAct Agent 效果更好 ✅

---

## 🔄 代码对比

### 一次性规划 Agent

```java
public AgentExecutionResult process(String userMessage) {
    // 1. 提取用户信息
    UserProfile profile = profileExtractor.extract(userMessage);

    // 2. LLM 一次性规划所有工具
    ToolSelectionResponse selection = planningService.planTools(userMessage, profile);

    // 3. 执行所有工具
    Map<String, String> toolResults = toolExecutor.executeTools(selection, profile, userMessage);

    // 4. LLM 总结
    String finalPrompt = buildSynthesisPrompt(...);
    String llmResponse = llmService.ask(finalPrompt);

    return result;
}
```

### ReAct 循环 Agent

```java
public ReActResult process(String userMessage) {
    List<ReActStep> steps = new ArrayList<>();
    StringBuilder context = new StringBuilder();

    // ReAct 循环
    for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
        // 1. 调用 LLM 生成 Thought + Action
        String llmOutput = llmService.ask(buildPrompt(context));

        // 2. 解析输出
        ReActStep step = parseLlmOutput(llmOutput);
        steps.add(step);

        // 3. 判断是否为 Final Answer
        if (step.isFinalAnswer()) {
            return ReActResult.builder()
                .steps(steps)
                .finalAnswer(step.getFinalAnswer())
                .build();
        }

        // 4. 执行工具
        String observation = executeTool(step.getAction(), profile);
        step.setObservation(observation);

        // 5. 更新上下文
        context.append("Thought: ").append(step.getThought()).append("\n");
        context.append("Observation: ").append(observation).append("\n");
    }

    // 达到最大循环次数
    return ReActResult.builder().steps(steps).build();
}
```

---

## 🎨 前端展示对比

### 一次性规划 Agent 展示

```
┌────────────────────────────────────────┐
│  📊 职业决策分析报告                   │
├────────────────────────────────────────┤
│  分析维度：收入、成长、市场            │
│                                        │
│  【收入维度分析】                      │
│  • 当前薪资：20k                       │
│  • 市场对比：80%                        │
│                                        │
│  【成长维度分析】                      │
│  • 职业阶段：成长阶段                  │
│                                        │
│  【市场维度分析】                      │
│  • 当前时机：金三银四                  │
│                                        │
│  【综合建议】                          │
│  根据分析，建议跳槽...                 │
└────────────────────────────────────────┘
```

### ReAct 循环 Agent 展示

```
┌────────────────────────────────────────┐
│  🤖 AI 思考过程                        │
├────────────────────────────────────────┤
│  ┌──────────────────────────────────┐  │
│  │ Round 1                          │  │
│  │ 💭 用户询问薪资涨幅，我需要...   │  │
│  │ 🎬 执行：收入维度分析            │  │
│  │ 👁️ 当前薪资20k，低于市场平均...  │  │
│  └──────────────────────────────────┘  │
│           ↓                            │
│  ┌──────────────────────────────────┐  │
│  │ Round 2                          │  │
│  │ 💭 已了解薪资偏低，现在需要...   │  │
│  │ 🎬 执行：市场维度分析            │  │
│  │ 👁️ 当前是金三银四期间...         │  │
│  └──────────────────────────────────┘  │
│           ↓                            │
│  ┌──────────────────────────────────┐  │
│  │ Round 3                          │  │
│  │ 💭 已收集到足够信息，可以给出... │  │
│  │ ✅ 最终答案：根据分析，建议跳槽  │  │
│  └──────────────────────────────────┘  │
└────────────────────────────────────────┘
```

---

## 🚀 如何选择？

### 决策树

```
开始
  ↓
问题复杂吗？
  ├─ 简单（问候、单一维度）
  │   └─ 一次性规划 Agent
  │       API: POST /api/chat
  │
  └─ 复杂（多维度、需要推理）
      ↓
  需要展示思考过程吗？
      ├─ 不需要
      │   └─ 一次性规划 Agent
      │       API: POST /api/chat
      │
      └─ 需要
          └─ ReAct 循环 Agent
              API: POST /api/react/chat
              或：GET /api/react/chat/stream
```

---

## 💡 最佳实践

### 1. 默认使用一次性规划 Agent

```
适用场景：
• 用户首次使用
• 问题简单明确
• 需要快速响应

优点：
• 响应快
• 成本低
• 体验好
```

### 2. 复杂问题自动切换到 ReAct Agent

```javascript
// 前端示例
function askAI(message) {
  // 简单问题 → 一次性规划
  if (isSimpleQuestion(message)) {
    return fetch('/api/chat', {
      method: 'POST',
      body: JSON.stringify({ message })
    });
  }

  // 复杂问题 → ReAct
  return fetch('/api/react/chat', {
    method: 'POST',
    body: JSON.stringify({ message })
  });
}
```

### 3. 提供 Agent 切换选项

```javascript
// 让用户选择
function askAI(message, agentType) {
  const endpoint = agentType === 'react'
    ? '/api/react/chat'
    : '/api/chat';

  return fetch(endpoint, {
    method: 'POST',
    body: JSON.stringify({ message })
  });
}
```

---

## 📚 更多资源

- 📘 [README.md](README.md) - 项目总览
- 📗 [REACT_AGENT_GUIDE.md](REACT_AGENT_GUIDE.md) - ReAgent 详细指南
- 📙 [项目结构说明](PROJECT_STRUCTURE.md) - 代码组织

---

**选择合适的 Agent，获得最佳体验！** 🚀
