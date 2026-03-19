# 真正的 Agent - 使用指南

## 🎯 核心特性

使用 **LangChain4j 的 @AiService**，实现真正的模型驱动 Agent。

### ✅ 关键优势

- ✅ **LLM 完全自主决策**：LLM 自动决定调用哪些工具
- ✅ **自动参数传递**：LLM 传入具体参数（如 `analyzeSalary(20.0)`）
- ✅ **真正的 ReAct 模式**：思考 → 行动 → 观察
- ✅ **代码极其简洁**：只需定义 `@Tool` 方法和 `@AiService` 接口
- ✅ **框架自动处理**：LangChain4j 自动处理所有工具调用逻辑

---

## 📁 新增文件

| 文件 | 作用 |
|------|------|
| **`agent/CareerDecisionTools.java`** | 使用 `@Tool` 注解定义工具方法 |
| **`agent/CareerAdvisorAssistant.java`** | 使用 `@AiService` 定义服务接口 |
| **`agent/DecisionAgentWithAiServices.java`** | Agent 调用逻辑 |
| **`config/AiServiceConfig.java`** | LangChain4j 配置 |
| **`controller/ChatControllerV2.java`** | REST API 控制器 |
| **`TRUE_AGENT_COMPARISON.md`** | 真 Agent vs 伪 Agent 对比 |

---

## 🔄 执行流程

### 用户输入："我现在20k，工作3年，想跳槽能涨多少？"

```
┌─────────────────────────────────────────────────────────────┐
│ 用户：我现在20k，工作3年，想跳槽能涨多少？                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 1: 提取用户信息                                         │
│ UserProfileExtractor.extract()                              │
│ 结果：{ salary: 20, years: 3 }                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 2: 构建用户消息                                         │
│ "我现在20k，工作3年，想跳槽能涨多少？                         │
│                                                              │
│ 【用户信息】                                                  │
│ - 当前月薪：20k                                               │
│ - 工作年限：3年                                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ Step 3: 调用 LangChain4j AiServices                         │
│ assistant.chat(userMessage)                                 │
│                                                              │
│ LangChain4j 自动处理：                                       │
│ 1. 发送消息给 LLM                                            │
│ 2. LLM 分析：用户询问薪资和成长，需要调用工具                  │
│ 3. LLM 自动调用工具：                                        │
│    - analyzeSalary(20.0)  ← LLM 传入参数 20.0                │
│    - analyzeGrowth(3)     ← LLM 传入参数 3                   │
│ 4. 工具执行，返回结果                                        │
│ 5. LLM 基于工具结果生成建议                                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 返回结果                                                      │
│ "## 📊 职业决策分析报告                                      │
│                                                              │
│ 【收入维度分析】                                              │
│ • 当前薪资：20.0k                                            │
│ • 市场对比：约为市场平均水平的111%                            │
│ ...                                                          │
│                                                              │
│ 【成长维度分析】                                              │
│ • 职业阶段：成长阶段                                          │
│ ...                                                          │
│                                                              │
│ ## 💡 综合建议                                                │
│ 根据你的薪资情况，建议跳槽..."                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 使用方法

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 调用 API

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我现在20k，工作3年，想跳槽能涨多少？"}'
```

### 3. 响应示例

```json
{
  "response": "## 📊 职业决策分析报告...",
  "trace": {
    "conversationId": "xxx",
    "userMessage": "我现在20k，工作3年，想跳槽能涨多少？",
    "extractedProfile": {
      "monthlySalary": 20,
      "workYears": 3
    },
    "toolExecutions": [
      {
        "toolName": "analyzeSalary",
        "displayName": "收入维度分析",
        "status": "success",
        "result": "【收入维度分析】\n• 当前薪资：20.0k...",
        "durationMs": 50
      },
      {
        "toolName": "analyzeGrowth",
        "displayName": "成长维度分析",
        "status": "success",
        "result": "【成长维度分析】\n• 职业阶段：成长阶段...",
        "durationMs": 30
      }
    ],
    "totalDurationMs": 2345
  },
  "conversationId": "xxx",
  "timestamp": "2024-03-19T10:30:00",
  "success": true
}
```

---

## 🔧 配置

### application.yml

```yaml
llm:
  api-key: your-api-key-here
  base-url: https://open.bigmodel.cn/api/paas/v4
  model-name: glm-4-flash
  temperature: 0.7
  timeout: 120
```

---

## 📊 可用工具

### 1. analyzeSalary(Double currentSalary)

**功能**：分析收入情况

**参数**：
- `currentSalary`：当前月薪（单位：千元），例如 `20.0` 表示 20k

**示例**：
```java
@Tool("分析用户的收入情况和跳槽涨幅空间")
public String analyzeSalary(Double currentSalary) {
    return String.format("• 当前薪资：%.1fk\n", currentSalary);
}
```

**LLM 调用示例**：
```
LLM：我需要分析用户的收入情况
→ 调用：analyzeSalary(20.0)
→ 返回："• 当前薪资：20.0k..."
```

---

### 2. analyzeGrowth(Integer workYears)

**功能**：分析成长空间

**参数**：
- `workYears`：工作年限（单位：年），例如 `3` 表示 3 年经验

**示例**：
```java
@Tool("分析用户的职业成长空间和发展机会")
public String analyzeGrowth(Integer workYears) {
    return String.format("• 工作年限：%d年\n", workYears);
}
```

---

### 3. analyzeMarket()

**功能**：分析市场环境

**参数**：无

**示例**：
```java
@Tool("分析当前招聘市场环境和最佳跳槽时机")
public String analyzeMarket() {
    return "• 金三银四：当前是跳槽黄金期...";
}
```

---

### 4. analyzeRisk(Integer workYears)

**功能**：分析跳槽风险

**参数**：
- `workYears`：工作年限（单位：年）

**示例**：
```java
@Tool("分析跳槽可能面临的风险")
public String analyzeRisk(Integer workYears) {
    return String.format("• 职业连续性风险（%d年）...\n", workYears);
}
```

---

## 🎨 添加新工具

### 步骤 1：定义工具方法

在 `CareerDecisionTools.java` 中添加：

```java
@Tool("分析公司文化和团队氛围")
public String analyzeCompanyCulture(String companyName) {
    return String.format("【公司文化分析】\n公司：%s\n...", companyName);
}
```

### 步骤 2：完成

**就这么简单！**

LangChain4j 会自动：
1. 扫描 `@Tool` 注解
2. 将工具注册到 LLM
3. LLM 可以自动调用新工具

---

## 📝 核心代码

### 1. 定义工具（使用 @Tool）

```java
@Component
@RequiredArgsConstructor
public class CareerDecisionTools {

    @Tool("分析收入情况")
    public String analyzeSalary(Double currentSalary) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【收入维度分析】\n");
        analysis.append(String.format("• 当前薪资：%.1fk\n", currentSalary));
        // ... 更多分析逻辑
        return analysis.toString();
    }

    @Tool("分析成长空间")
    public String analyzeGrowth(Integer workYears) {
        // ...
    }
}
```

### 2. 定义 AiService 接口

```java
@AiService
public interface CareerAdvisorAssistant {

    @SystemMessage("""
            你是一位资深的职业规划顾问。
            你可以使用以下工具：
            - analyzeSalary(currentSalary)
            - analyzeGrowth(workYears)
            - analyzeMarket()
            - analyzeRisk(workYears)
            """)
    String chat(@UserMessage String userMessage);
}
```

### 3. 配置

```java
@Configuration
@RequiredArgsConstructor
public class AiServiceConfig {

    private final ChatLanguageModel chatLanguageModel;
    private final CareerDecisionTools tools;

    @Bean
    public CareerAdvisorAssistant careerAdvisorAssistant() {
        return AiServices.builder(CareerAdvisorAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(tools)
                .build();
    }
}
```

### 4. 使用

```java
@Component
@RequiredArgsConstructor
public class DecisionAgentWithAiServices {

    private final CareerAdvisorAssistant assistant;

    public AgentExecutionResult process(String userMessage) {
        String response = assistant.chat(userMessage);
        return AgentExecutionResult.builder()
                .response(response)
                .trace(buildTrace(response))
                .build();
    }
}
```

**就这么简单！**

---

## 🎯 与之前的区别

### ❌ 之前的伪 Agent

```java
// 需要构建规划 Prompt
String planningPrompt = "用户询问薪资...需要哪些工具？";

// LLM 返回 JSON
String llmResponse = llmService.ask(planningPrompt);
// {"tools": ["salary_analysis"]}

// 手动解析 JSON
ToolSelectionResponse selection = parseJson(llmResponse);

// 手动匹配工具
AnalysisTool tool = findToolByName("salary_analysis");

// 手动调用
String result = tool.analyze(profile, message);
```

### ✅ 现在的真 Agent

```java
// 直接调用
String response = assistant.chat(userMessage);

// LangChain4j 自动处理：
// - LLM 决定调用哪些工具
// - LLM 传入参数
// - 自动执行工具
// - 返回结果
```

**代码量减少 90%！**

---

## 📊 执行轨迹

### 前端展示

```javascript
// 显示 LLM 调用了哪些工具
trace.toolExecutions.forEach(step => {
    console.log(`${step.displayName} (${step.durationMs}ms)`);
    console.log(step.result);
});

// 输出：
// 收入维度分析 (50ms)
// 【收入维度分析】
// • 当前薪资：20.0k
// • 市场对比：约为市场平均水平的111%
// ...
//
// 成长维度分析 (30ms)
// 【成长维度分析】
// • 职业阶段：成长阶段
// ...
```

---

## 🚀 总结

### 使用真 Agent的优势

1. **真正的 LLM 决策**：LLM 自主决定调用哪些工具
2. **自动参数传递**：LLM 传入具体参数
3. **代码极其简洁**：只需定义 `@Tool` 和 `@AiService`
4. **易于扩展**：添加新工具只需一个 `@Tool` 方法
5. **完全由框架控制**：LangChain4j 自动处理所有逻辑

### 与伪 Agent 的对比

| 维度 | 伪 Agent | 真 Agent |
|------|---------|---------|
| 代码量 | ~700 行 | ~330 行 |
| 复杂度 | 高 | 低 |
| LLM 决策权 | ❌ | ✅ |
| 参数传递 | ❌ | ✅ |
| 可扩展性 | 低 | 高 |

---

## 📖 参考文档

- **[TRUE_AGENT_COMPARISON.md](TRUE_AGENT_COMPARISON.md)** - 详细对比
- **[FRONTEND_DISPLAY_GUIDE.md](FRONTEND_DISPLAY_GUIDE.md)** - 前端展示指南
- **[LangChain4j 文档](https://docs.langchain4j.dev/)** - 官方文档
