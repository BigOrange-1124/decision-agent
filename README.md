# 🤖 决策 Agent - AI 职业决策助手（支持 ReAct 模式）

> 一个基于大语言模型的智能职业决策系统，支持"一次性规划"和"ReAct 循环思考"两种模式

---

## 🎯 项目简介

决策 Agent 是一个**模型驱动的 AI Agent 系统**，通过大语言模型（LLM）智能分析用户的职业情况，从收入、成长、市场、风险四个维度提供跳槽决策建议。

### ✨ 核心特性

- ✅ **真正的模型驱动**：LLM 根据用户问题智能选择分析维度
- ✅ **双模式支持**：
  - 📋 **一次性规划模式**：快速高效，适合简单问题
  - 🔄 **ReAct 循环模式**：逐步思考，适合复杂问题
- ✅ **完整的执行轨迹**：返回 Agent 的完整思考过程
- ✅ **流式输出支持**：通过 SSE 实时推送思考过程
- ✅ **多模型支持**：支持 OpenAI、智谱AI 等兼容 OpenAI API 的服务

---

## 🆚 两种 Agent 模式对比

| 特性 | 一次性规划 Agent | ReAct 循环 Agent |
|------|------------------|------------------|
| **规划方式** | 一次性选择所有工具 | 逐步思考，每轮选一个工具 |
| **执行顺序** | 按固定顺序执行 | 根据结果动态调整 |
| **思考过程** | 只在开始时规划一次 | 每轮都重新思考 |
| **适应能力** | 无 | 可根据观察结果调整策略 |
| **响应速度** | 快（2-3秒） | 慢（5-8秒） |
| **准确性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **适用场景** | 简单问题、快速响应 | 复杂问题、深度分析 |

### 选择建议

```
简单问题（问候、单一维度）
└─ 一次性规划 Agent
   └─ POST /api/chat

复杂问题（需要多维度分析）
└─ ReAct 循环 Agent
   └─ POST /api/react/chat
```

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────┐
│                  前端应用                        │
└────┬───────────────────────────┬────────────────┘
     │                           │
     ↓                           ↓
┌─────────────────┐     ┌─────────────────┐
│ 一次性规划 Agent │     │ ReAct 循环 Agent │
│ DecisionAgent   │     │   ReActAgent    │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │ 共享                  │ 共享
         ↓                       ↓
┌──────────────────────────────────────────┐
│           核心服务层                       │
│  • LlmService（LLM 调用）                 │
│  • UserProfileExtractor（信息提取）       │
│  • ToolPlanningService（工具规划）        │
│  • ToolExecutor（工具执行）               │
└──────────────────┬───────────────────────┘
                   ↓
┌──────────────────────────────────────────┐
│           工具层（四维度分析）             │
│  • SalaryAnalysisTool（收入）            │
│  • GrowthAnalysisTool（成长）            │
│  • MarketAnalysisTool（市场）            │
│  • RiskAnalysisTool（风险）             │
└──────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- 大语言模型 API Key（OpenAI 或智谱AI）

### 配置步骤

1. **配置 LLM API**

编辑 `src/main/resources/application.yml`：

```yaml
llm:
  # 使用智谱AI
  api-key: your-zhipu-api-key
  base-url: https://open.bigmodel.cn/api/paas/v4
  model-name: glm-4-flash

  # 或使用 OpenAI
  # api-key: your-openai-api-key
  # model-name: gpt-4o-mini
```

2. **启动项目**
```bash
mvn spring-boot:run
```

3. **访问服务**
```
http://localhost:8199
```

---

## 📡 API 接口

### 1️⃣ 一次性规划 Agent

**接口**：`POST /api/chat`

**请求**：
```json
{
  "message": "我现在20k，想跳槽能涨多少？"
}
```

**响应**：
```json
{
  "response": "## 📊 职业决策分析报告...",
  "trace": {
    "planning": {
      "needsAnalysis": true,
      "selectedTools": ["salary_analysis"],
      "reasoning": "用户询问薪资涨幅"
    },
    "toolExecutions": [...]
  }
}
```

---

### 2️⃣ ReAct 循环 Agent（推荐用于复杂问题）

**接口**：`POST /api/react/chat`

**请求**：
```json
{
  "message": "我要不要跳槽？我现在20k，工作3年"
}
```

**响应**：
```json
{
  "finalAnswer": "根据分析，建议跳槽...",
  "steps": [
    {
      "iteration": 1,
      "thought": "用户询问跳槽建议，我需要先分析收入情况",
      "action": "salary_analysis",
      "observation": "【收入维度分析】..."
    },
    {
      "iteration": 2,
      "thought": "已了解到薪资偏低，现在需要分析市场时机",
      "action": "market_analysis",
      "observation": "【市场维度分析】..."
    },
    {
      "iteration": 3,
      "thought": "已收集到足够信息，可以给出综合建议",
      "finalAnswer": "根据分析，建议跳槽..."
    }
  ],
  "totalIterations": 3,
  "success": true
}
```

---

### 3️⃣ ReAct 流式接口（实时推送）

**接口**：`GET /api/react/chat/stream?message=xxx`

**SSE 事件**：
```
event: round
data: Round 1

event: thought
data: 用户询问跳槽建议，我需要先分析收入情况

event: action
data: 收入维度分析

event: observation
data: 【收入维度分析】...

event: round
data: Round 2

event: final_answer
data: 根据分析，建议跳槽...

event: done
data: 分析完成（共 2 轮）
```

---

## 📊 ReAgent 执行示例

### 用户输入
```
我要不要跳槽？我现在20k，工作3年
```

### ReAct 循环过程

#### Round 1
```
💭 思考 (Thought)：
用户询问跳槽建议，我需要先分析收入情况

🎬 行动 (Action)：
salary_analysis

👁️ 观察 (Observation)：
【收入维度分析】
• 当前薪资：20k
• 市场对比：当前薪资约为市场平均水平的80%
• 薪资评估：⚠️ 你的薪资偏低，跳槽有较大提升空间（预计20-40%）
• 预期薪资：23k - 26k
```

#### Round 2
```
💭 思考 (Thought)：
已了解到薪资偏低，现在需要分析市场时机

🎬 行动 (Action)：
market_analysis

👁️ 观察 (Observation)：
【市场维度分析】
• 当前时间：MARCH
• 金三银四：当前是跳槽黄金期，机会多，薪资涨幅可达30%+
• 建议根据市场情况选择合适的时机
```

#### Round 3
```
💭 思考 (Thought)：
已收集到收入和市场信息，3年经验是跳槽黄金期，可以给出综合建议

✅ 最终答案 (Final Answer)：
根据分析，建议跳槽。理由：
1. 当前薪资20k低于市场平均25k
2. 3年经验是跳槽黄金期
3. 当前处于金三银四期间
预计涨幅20-30%，目标薪资23k-26k。
```

---

## 🧠 ReAgent 模式深度解析

### 什么是 ReAct？

**ReAct** = **Re**asoning（推理）+ **Act**ing（行动）

ReAct 是一种让 AI Agent 模拟人类思考过程的模式：

1. **Thought（思考）**：我现在需要做什么？
2. **Action（行动）**：选择一个工具执行
3. **Observation（观察）**：查看工具返回结果
4. **决策**：继续下一轮 OR 给出最终答案

### ReAgent 循环图

```
┌─────────────────────────────────────────┐
│         用户问题                         │
└──────────────┬──────────────────────────┘
               ↓
        ┌──────────────┐
        │ Round 1      │
        └──────┬───────┘
               ↓
    ┌─────────────────────┐
    │ 💭 Thought: 思考    │
    │ "我需要做什么？"    │
    └──────────┬──────────┘
               ↓
    ┌─────────────────────┐
    │ 🎬 Action: 行动     │
    │ "选择工具 A"        │
    └──────────┬──────────┘
               ↓
    ┌─────────────────────┐
    │ 👁️ Observation: 观察│
    │ "工具返回结果..."   │
    └──────────┬──────────┘
               ↓
         有足够信息吗？
              ↙  ↘
           否      是
           ↓       ↓
    ┌──────────┐  ┌──────────────┐
    │ Round 2  │  │ Final Answer │
    └──────────┘  └──────────────┘
```

### 为什么选择 ReAgent？

| 场景 | 一次性规划 | ReAgent |
|------|-----------|---------|
| 简单问候 | ✅ 推荐 | ⚠️ 过度 |
| 单一维度（只问薪资） | ✅ 可用 | ✅ 更准确 |
| 多维度分析 | ⚠️ 可能冗余 | ✅ 推荐 |
| 需要推理 | ❌ 不支持 | ✅ 推荐 |
| 展示思考过程 | ❌ 不可见 | ✅ 完全可见 |

---

## 💻 前端集成示例

### 方式 1：一次性规划 Agent

```javascript
const response = await fetch('/api/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message: '我现在20k...' })
});

const data = await response.json();
document.getElementById('result').innerHTML = data.response;
```

### 方式 2：ReAgent（一次性返回）

```javascript
const response = await fetch('/api/react/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message: '我要不要跳槽？' })
});

const data = await response.json();

// 展示思考过程
data.steps.forEach(step => {
  console.log(`Round ${step.iteration}:`);
  console.log(`  思考：${step.thought}`);
  if (step.action) {
    console.log(`  行动：${step.action}`);
  }
});

// 展示最终答案
document.getElementById('answer').innerHTML = data.finalAnswer;
```

### 方式 3：ReAgent（SSE 流式）

```javascript
const eventSource = new EventSource(
  '/api/react/chat/stream?message=我要不要跳槽？'
);

eventSource.addEventListener('round', (e) => {
  console.log('开始第 ' + e.data + ' 轮');
});

eventSource.addEventListener('thought', (e) => {
  console.log('💭 ' + e.data);
});

eventSource.addEventListener('action', (e) => {
  console.log('🎬 ' + e.data);
});

eventSource.addEventListener('observation', (e) => {
  console.log('👁️ ' + e.data);
});

eventSource.addEventListener('final_answer', (e) => {
  console.log('✅ ' + e.data);
});

eventSource.addEventListener('done', (e) => {
  eventSource.close();
});
```

---

## 📁 项目结构

```
decision-agent/
├── src/main/java/com/decisionagent/
│   ├── DecisionAgentApplication.java      # 应用主入口
│   │
│   ├── agent/
│   │   ├── DecisionAgent.java            # 一次性规划 Agent
│   │   └── ReActAgent.java               # ReAct 循环 Agent ⭐NEW
│   │
│   ├── controller/
│   │   ├── ChatController.java           # 一次性规划接口
│   │   ├── StreamingChatController.java  # 流式接口
│   │   ├── ReActChatController.java      # ReAct 接口 ⭐NEW
│   │   └── ReActStreamingController.java # ReAct 流式接口 ⭐NEW
│   │
│   ├── service/
│   │   ├── LlmService.java               # LLM 服务
│   │   ├── UserProfileExtractor.java    # 用户信息提取
│   │   ├── ToolPlanningService.java      # 工具规划服务
│   │   └── ToolExecutor.java             # 工具执行器
│   │
│   ├── tool/
│   │   ├── AnalysisTool.java             # 分析工具接口
│   │   └── impl/
│   │       ├── SalaryAnalysisTool.java   # 收入分析
│   │       ├── GrowthAnalysisTool.java   # 成长分析
│   │       ├── MarketAnalysisTool.java   # 市场分析
│   │       └── RiskAnalysisTool.java     # 风险分析
│   │
│   ├── dto/
│   │   ├── AgentExecutionTrace.java     # Agent 执行轨迹
│   │   ├── ReActResult.java             # ReAct 结果 ⭐NEW
│   │   └── ReActStep.java               # ReAct 步骤 ⭐NEW
│   │
│   └── config/
│       ├── CorsConfig.java               # 跨域配置
│       ├── GlobalExceptionHandler.java   # 异常处理
│       └── ReActAgentConfig.java        # ReAct 配置 ⭐NEW
│
├── src/main/resources/
│   └── application.yml                   # 应用配置
│
├── README.md                             # 项目文档
├── REACT_AGENT_GUIDE.md                  # ReAct 使用指南 ⭐NEW
└── pom.xml                               # Maven 配置
```

---

## 🔧 技术栈

| 类别 | 技术 | 版本 |
|-----|------|------|
| **后端框架** | Spring Boot | 3.2.0 |
| **LLM 集成** | LangChain4j | 0.34.0 |
| **大模型** | OpenAI / 智谱AI | GPT-4 / GLM-4 |
| **日志** | SLF4J | - |
| **构建工具** | Maven | 3.6+ |

---

## 📚 文档索引

- 📘 [README.md](README.md) - 项目总览（本文档）
- 📗 [REACT_AGENT_GUIDE.md](REACT_AGENT_GUIDE.md) - ReAgent 使用指南
- 📙 [项目结构说明](PROJECT_STRUCTURE.md) - 代码组织说明
- 📕 [快速开始指南](QUICKSTART.md) - 新手入门

---

## 🎯 路线图

### v2.0 - 一次性规划 Agent ✅
- [x] LLM 工具规划
- [x] 动态工具执行
- [x] 执行轨迹记录
- [x] 流式输出支持

### v3.0 - ReAct 循环 Agent ✅（当前版本）
- [x] ReAct 循环模式
- [x] Thought → Action → Observation
- [x] 逐步思考能力
- [x] 动态策略调整
- [x] 流式推送思考过程

### v4.0 - 进阶特性（规划中）
- [ ] 多轮对话支持
- [ ] 工具参数化
- [ ] 自我反思能力
- [ ] 多 Agent 协作

---

## 🤝 贡献指南

欢迎贡献代码、提出建议或报告问题！

---

## 📄 许可证

MIT License

---

## 👥 作者

DecisionAgent Team

---

**让 AI 像人类一样思考和行动！** 🤖✨
