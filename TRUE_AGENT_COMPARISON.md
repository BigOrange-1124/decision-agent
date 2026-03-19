# 真 Agent vs 伪 Agent - 完整对比

## 🎯 核心区别

### ❌ 伪 Agent（之前的实现）

```
用户输入 → 提取信息
    ↓
我们构建规划 Prompt："用户询问薪资...需要哪些工具？"
    ↓
LLM 返回 JSON：
{
  "needs_analysis": true,
  "tools": ["salary_analysis"],
  "reasoning": "用户询问薪资涨幅"
}
    ↓
我们手动解析 JSON
    ↓
我们手动通过字符串匹配执行工具
    ↓
收集工具结果
    ↓
发送给 LLM 生成建议
    ↓
返回结果
```

**问题：**
- 我们需要构建复杂的 Prompt
- 我们需要解析 JSON
- 我们需要手动匹配工具
- LLM 没有真正的决策权
- 不是真正的 Agent

---

### ✅ 真 Agent（现在的实现）

```
用户输入 → 提取信息
    ↓
发送给 LLM："我现在20k，工作3年，想跳槽"
    ↓
LLM 分析：用户询问薪资和成长，需要调用工具
    ↓
LLM 自动调用：
  analyzeSalary(20.0)  ← LLM 传入参数 20.0
  analyzeGrowth(3)     ← LLM 传入参数 3
    ↓
工具执行，返回结果
    ↓
LLM 基于工具结果生成建议
    ↓
返回结果
```

**优势：**
- LLM 完全自主决策
- 自动处理参数传递
- 自动调用工具
- 真正的 ReAct 模式
- 代码极其简洁

---

## 📊 详细对比

| 维度 | 伪 Agent | 真 Agent |
|------|---------|---------|
| **工具注册** | 我们手动注册 | LangChain4j 自动扫描 @Tool |
| **Prompt 构建** | 我们构建规划 Prompt | 只需定义 System Message |
| **LLM 决策** | LLM 返回 JSON 列表 | LLM 直接调用工具方法 |
| **参数传递** | 无参数传递 | LLM 传入参数（如 20.0, 3） |
| **工具调用** | 我们手动解析、匹配 | LangChain4j 自动调用 |
| **结果返回** | 我们手动收集 | 自动返回给 LLM |
| **代码复杂度** | 高（需要很多自定义代码） | 低（框架自动处理） |
| **可扩展性** | 低（需要修改核心代码） | 高（只需添加 @Tool 方法） |
| **Agent 特征** | ❌ 规则系统 | ✅ 真正的 Agent |

---

## 💡 代码对比

### 伪 Agent（需要 500+ 行代码）

```java
// 1. 定义 DTO
class ToolSelectionResponse {
    private Boolean needsAnalysis;
    private List<String> tools;
    private String reasoning;
}

// 2. 创建规划服务
class ToolPlanningService {
    public ToolSelectionResponse planTools(...) {
        String prompt = "构建复杂的规划 Prompt...";
        String response = llmService.ask(prompt);
        return parseJson(response);  // 手动解析
    }
}

// 3. 创建执行服务
class ToolExecutor {
    public Map<String, String> executeTools(...) {
        // 手动匹配工具
        AnalysisTool tool = findToolByName(toolName);
        String result = tool.analyze(...);
        return results;
    }
}

// 4. Agent 协调
class DecisionAgent {
    public String process(...) {
        // 1. 提取信息
        // 2. 调用规划服务
        ToolSelectionResponse selection = planningService.planTools(...);
        // 3. 调用执行服务
        Map<String, String> results = executor.executeTools(...);
        // 4. 生成最终建议
        String advice = llmService.ask(...);
        return advice;
    }
}
```

**总计：~500 行代码，复杂的自定义逻辑**

---

### 真 Agent（只需 50 行代码）

```java
// 1. 定义工具（@Tool 注解）
@Component
class CareerDecisionTools {
    @Tool("分析收入情况")
    public String analyzeSalary(Double currentSalary) {
        return "分析结果...";
    }

    @Tool("分析成长空间")
    public String analyzeGrowth(Integer workYears) {
        return "分析结果...";
    }
}

// 2. 定义 AiService 接口
@AiService
interface CareerAdvisorAssistant {
    @SystemMessage("你是职业规划顾问...")
    String chat(@UserMessage String userMessage);
}

// 3. 配置
@Configuration
class Config {
    @Bean
    CareerAdvisorAssistant assistant(ChatModel model, CareerDecisionTools tools) {
        return AiServices.builder(CareerAdvisorAssistant.class)
                .chatLanguageModel(model)
                .tools(tools)
                .build();
    }
}

// 4. 使用
@Component
class Agent {
    private final CareerAdvisorAssistant assistant;

    public String process(String message) {
        return assistant.chat(message);  // 就这么简单！
    }
}
```

**总计：~50 行代码，框架自动处理所有逻辑**

---

## 🔧 技术实现

### 伪 Agent 技术栈

```java
// 需要自己实现
- ToolPlanningService  // 规划服务
- ToolExecutor         // 执行服务
- ToolSelectionResponse  // DTO
- UserProfileExtractor  // 信息提取
- DecisionAgent       // 协调器
```

### 真 Agent 技术栈

```java
// LangChain4j 自动处理
- @Tool              // 定义工具
- @AiService         // 定义服务接口
- AiServices.builder() // 构建服务
```

---

## 📈 执行流程对比

### 场景：用户问"我现在20k，工作3年，想跳槽能涨多少？"

#### 伪 Agent 执行流程

```
1. 提取信息：{ salary: 20, years: 3 }
2. 构建规划 Prompt：
   "用户询问薪资...需要哪些工具？"
3. LLM 返回 JSON：
   { "tools": ["salary_analysis", "growth_analysis"] }
4. 解析 JSON，得到工具列表
5. 通过字符串匹配找到 SalaryAnalysisTool
6. 调用 tool.analyze(profile, message)
   ← 注意：这里没有参数，需要从 profile 中读取
7. 通过字符串匹配找到 GrowthAnalysisTool
8. 调用 tool.analyze(profile, message)
9. 收集结果
10. 发送给 LLM 生成建议
```

**问题：**
- 工具方法签名是 `analyze(UserProfile, String)`
- 不能直接接收参数
- 需要从 UserProfile 中提取信息

---

#### 真 Agent 执行流程

```
1. 提取信息：{ salary: 20, years: 3 }
2. 构建用户消息：
   "我现在20k，工作3年，想跳槽能涨多少？

    【用户信息】
    - 当前月薪：20k
    - 工作年限：3年"
3. LLM 分析消息
4. LLM 决定：需要分析薪资和成长
5. LLM 自动调用：
   analyzeSalary(20.0)  ← 直接传入参数！
   analyzeGrowth(3)     ← 直接传入参数！
6. 工具执行，返回结果
7. LLM 基于工具结果生成建议
8. 返回最终回答
```

**优势：**
- 工具方法签名是 `analyzeSalary(Double currentSalary)`
- 可以直接接收参数
- LLM 传入具体的数值

---

## 🎨 前端展示

### 伪 Agent 的执行轨迹

```json
{
  "planning": {
    "needsAnalysis": true,
    "selectedTools": ["salary_analysis", "growth_analysis"],
    "reasoning": "用户询问薪资涨幅"
  },
  "toolExecutions": [
    {
      "toolName": "salary_analysis",
      "result": "【收入维度分析】..."
    }
  ]
}
```

### 真 Agent 的执行轨迹

```json
{
  "toolExecutions": [
    {
      "toolName": "analyzeSalary",
      "displayName": "收入维度分析",
      "status": "success",
      "result": "【收入维度分析】\n• 当前薪资：20.0k..."
    },
    {
      "toolName": "analyzeGrowth",
      "displayName": "成长维度分析",
      "status": "success",
      "result": "【成长维度分析】\n• 职业阶段：成长阶段..."
    }
  ]
}
```

**区别：**
- 真 Agent 没有 `planning` 字段
- 因为 LLM 直接调用工具，不需要"规划"步骤
- 更加直接、高效

---

## 🚀 使用示例

### 伪 Agent 使用

```java
// 复杂的调用
AgentExecutionResult result = agent.process(message);
String response = result.getResponse();
AgentExecutionTrace trace = result.getTrace();

// 查看 LLM 规划
trace.getPlanning().getSelectedTools();  // ["salary_analysis"]
trace.getPlanning().getReasoning();       // "用户询问薪资涨幅"
```

### 真 Agent 使用

```java
// 极其简单的调用
String response = assistant.chat(message);

// 就这么简单！
// LangChain4j 自动处理所有工具调用
```

---

## 📚 代码结构

### 伪 Agent 文件结构

```
src/main/java/com/decisionagent/
├── agent/
│   ├── DecisionAgent.java              # 200+ 行
│   └── CareerDecisionTools.java        # 未使用
├── service/
│   ├── ToolPlanningService.java        # 150+ 行
│   ├── ToolExecutor.java               # 100+ 行
│   └── UserProfileExtractor.java       # 50+ 行
└── dto/
    ├── ToolSelectionResponse.java      # 50+ 行
    ├── AgentExecutionTrace.java        # 100+ 行
    └── UserProfile.java               # 50+ 行

总计：~700 行代码
```

### 真 Agent 文件结构

```
src/main/java/com/decisionagent/
├── agent/
│   ├── CareerDecisionTools.java        # 100 行（@Tool 方法）
│   ├── CareerAdvisorAssistant.java     # 20 行（@AiService 接口）
│   └── DecisionAgentWithAiServices.java # 80 行（调用逻辑）
├── config/
│   ├── AiServiceConfig.java            # 30 行（配置）
│   └── LangChain4jAgentConfig.java     # 50 行（配置）
└── service/
    └── UserProfileExtractor.java       # 50+ 行（保留）

总计：~330 行代码
```

**代码量减少了一半！**

---

## 🎯 结论

### 伪 Agent 的问题

1. ❌ 我们手动构建规划 Prompt
2. ❌ 我们手动解析 LLM 返回的 JSON
3. ❌ 我们手动通过字符串匹配执行工具
4. ❌ LLM 没有真正的决策权
5. ❌ 工具方法不能直接接收参数
6. ❌ 代码复杂，难以维护

### 真 Agent 的优势

1. ✅ LLM 完全自主决策
2. ✅ 自动处理工具调用
3. ✅ 自动传递参数
4. ✅ 真正的 ReAct 模式
5. ✅ 代码极其简洁
6. ✅ 易于扩展

---

## 📖 推荐使用

**使用真 Agent（DecisionAgentWithAiServices）**

```java
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final DecisionAgentWithAiServices agent;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        AgentExecutionResult result = agent.process(request.getMessage());
        return ResponseEntity.ok(
            ChatResponse.success(
                result.getResponse(),
                result.getTrace(),
                result.getTrace().getConversationId()
            )
        );
    }
}
```

**就这么简单！**
