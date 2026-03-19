# 模型驱动 Agent 架构设计

## 系统架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         用户请求 (User Input)                        │
│                    "我现在20k，想跳槽，能涨多少？"                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    UserProfileExtractor                             │
│                      提取用户信息                                     │
│  { salary: 20k, years: null, position: null }                       │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ToolPlanningService                              │
│                  🤖 LLM 决策：需要哪些工具？                           │
│                                                                       │
│  Prompt: "用户询问薪资...需要哪些工具？"                              │
│                       ↓                                              │
│  LLM Response:                                                       │
│  {                                                                   │
│    "needs_analysis": true,                                          │
│    "tools": ["salary_analysis"],                                    │
│    "reasoning": "用户询问薪资涨幅"                                    │
│  }                                                                   │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ToolExecutor                                   │
│                    动态执行选中的工具                                  │
│                                                                       │
│  Tool Name Mapping:                                                 │
│  ┌─────────────────┬──────────────────────────┐                    │
│  │ "salary_analysis" │ → SalaryAnalysisTool    │                    │
│  │ "growth_analysis" │ → GrowthAnalysisTool    │                    │
│  │ "market_analysis" │ → MarketAnalysisTool    │                    │
│  │ "risk_analysis"   │ → RiskAnalysisTool      │                    │
│  └─────────────────┴──────────────────────────┘                    │
│                        ↓                                             │
│  Executing: SalaryAnalysisTool.analyze()                            │
│  Result: "【收入维度分析】\n• 当前薪资：20k..."                      │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DecisionAgent                                   │
│                  综合分析结果生成建议                                  │
│                                                                       │
│  Prompt:                                                             │
│  "用户咨询：我现在20k，想跳槽，能涨多少？                             │
│   已执行维度：收入维度                                               │
│   分析结果：[SalaryAnalysisTool 的输出]                              │
│                                                                       │
│   请基于以上分析，给出建议..."                                        │
│                       ↓                                              │
│  LLM Response:                                                       │
│  "根据你的薪资情况...建议跳槽，涨幅预计20-30%..."                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        最终响应                                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━                         │
│  **分析维度**：收入维度                                               │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━                         │
│  【收入维度分析】                                                     │
│  • 当前薪资：20k                                                     │
│  • 市场对比：约为市场平均水平的111%                                   │
│  • 薪资评估：◐ 你的薪资处于正常水平...                                │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━                         │
│  ## 💡 综合建议                                                      │
│  根据你的薪资情况...建议跳槽，涨幅预计20-30%...                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 核心组件详解

### 1. ToolPlanningService（规划服务）

**职责**：LLM 决策中枢

```java
@Service
public class ToolPlanningService {
    private final LlmService llmService;

    public ToolSelectionResponse planTools(String userMessage, UserProfile profile) {
        // 1. 构建 Prompt
        String prompt = buildPlanningPrompt(userMessage, profile);

        // 2. 调用 LLM
        String response = llmService.ask(PLANNING_SYSTEM_PROMPT, prompt);

        // 3. 解析 JSON
        return parsePlanningResponse(response);
    }
}
```

**关键 Prompt 设计**：
- 明确每个工具的适用场景
- 定义判断规则（何时需要/不需要工具）
- 强制 JSON 输出格式
- 包含容错处理

---

### 2. ToolExecutor（执行服务）

**职责**：动态工具执行器

```java
@Service
public class ToolExecutor {
    private final List<AnalysisTool> analysisTools;

    public Map<String, String> executeTools(
        ToolSelectionResponse selection,
        UserProfile profile,
        String userMessage
    ) {
        Map<String, String> results = new LinkedHashMap<>();

        for (String toolName : selection.getTools()) {
            AnalysisTool tool = findToolByName(toolName);
            String result = tool.analyze(profile, userMessage);
            results.put(toolName, result);
        }

        return results;
    }
}
```

**工具名称映射**：
```java
private boolean isToolMatch(AnalysisTool tool, String toolName) {
    String className = tool.getClass().getSimpleName();
    return switch (toolName) {
        case "salary_analysis" -> className.contains("Salary");
        case "growth_analysis" -> className.contains("Growth");
        case "market_analysis" -> className.contains("Market");
        case "risk_analysis" -> className.contains("Risk");
        default -> false;
    };
}
```

---

### 3. DecisionAgent（协调器）

**职责**：流程编排

```java
@Component
public class DecisionAgent {
    private final ToolPlanningService planningService;
    private final ToolExecutor toolExecutor;
    private final LlmService llmService;

    public String process(String userMessage) {
        // 1. 提取用户信息
        UserProfile profile = profileExtractor.extract(userMessage);

        // 2. LLM 规划工具
        ToolSelectionResponse selection = planningService.planTools(userMessage, profile);

        // 3. 判断是否需要工具
        if (!selection.getNeedsAnalysis()) {
            return generateDirectResponse(userMessage, profile);
        }

        // 4. 执行工具
        Map<String, String> results = toolExecutor.executeTools(selection, profile, userMessage);

        // 5. 生成建议
        String summary = llmService.ask(buildSynthesisPrompt(...));

        // 6. 组合响应
        return buildFullResponse(results, summary, selection);
    }
}
```

---

## 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                        数据流                                    │
└─────────────────────────────────────────────────────────────────┘

用户输入 (String)
    ↓
UserProfile (DTO)
    { salary, years, position, ... }
    ↓
ToolSelectionResponse (DTO)
    { needsAnalysis, tools, reasoning }
    ↓
Map<String, String> (工具执行结果)
    {
      "salary_analysis": "【收入维度分析】...",
      "growth_analysis": "【成长维度分析】..."
    }
    ↓
Final Response (String)
    "## 📊 职业决策分析报告\n\n..."
```

---

## 时序图

```
User          DecisionAgent    ToolPlanningService    LLM      ToolExecutor    Tool
  │                 │                 │               │            │           │
  │─── 用户消息 ────>│                 │               │            │           │
  │                 │─── 提取信息 ────>│               │            │           │
  │                 │                 │─── 规划请求 ──>│            │           │
  │                 │                 │<─ JSON决策 ───│            │           │
  │                 │<─ 工具选择 ─────│               │            │           │
  │                 │─── 执行工具 ──────────────────────────────────>│           │
  │                 │                 │               │            │           │
  │                 │                 │               │            │<─ 分析结果─│
  │                 │<─ 工具结果 ────────────────────│            │           │
  │                 │─── 生成建议 ────>               │            │           │
  │                 │                 │<─ 综合建议 ───│            │           │
  │                 │<─ 最终响应 ─────│               │            │           │
  │<── 完整报告 ────│                 │               │            │           │
```

---

## 关键设计决策

### 1. 为什么使用 String 而不是 Enum 作为工具名称？

**选择 String**：
- ✅ LLM 可以直接返回工具名称
- ✅ 易于扩展（无需修改代码）
- ✅ 灵活的映射机制

**如果用 Enum**：
- ❌ LLM 返回的 JSON 需要额外转换
- ❌ 新增工具需要修改代码
- ❌ 不够灵活

### 2. 为什么需要 ToolSelectionResponse DTO？

**单一职责**：
- ✅ 清晰的数据结构
- ✅ 易于验证和测试
- ✅ 支持未来扩展（如添加优先级、参数等）

### 3. 为什么分离 ToolPlanningService 和 ToolExecutor？

**职责分离**：
- **ToolPlanningService**：专注于 LLM 交互和决策
- **ToolExecutor**：专注于工具查找和执行
- **DecisionAgent**：专注于流程编排

**好处**：
- ✅ 易于测试
- ✅ 易于维护
- ✅ 可复用

---

## 扩展性设计

### 新增工具步骤

假设要新增一个 `work_life_balance_analysis` 工具：

**Step 1**: 实现 Tool
```java
@Component
public class WorkLifeBalanceAnalysisTool implements AnalysisTool {
    @Override
    public String getName() {
        return "工作生活平衡分析";
    }

    @Override
    public String analyze(UserProfile profile, String userMessage) {
        // 实现分析逻辑
    }
}
```

**Step 2**: 更新 ToolPlanningService 的 Prompt
```java
5. **work_life_balance_analysis**（工作生活平衡分析）
   - 适用场景：用户询问加班、工作强度、远程办公等
   - 关键词：加班、996、远程、弹性、平衡
```

**Step 3**: 更新 ToolExecutor 的映射
```java
case "work_life_balance_analysis" -> className.contains("Balance");
```

**Step 4**: 更新 DecisionAgent 的维度名称映射
```java
case "work_life_balance_analysis" -> "工作生活平衡维度";
```

**完成！** 无需修改核心逻辑。

---

## 性能考虑

### LLM 调用次数

| 场景 | 改造前 | 改造后 |
|------|--------|--------|
| 简单问候 | 1 次 | 2 次（规划+直接回答） |
| 单一维度 | 1 次 | 2 次（规划+执行后综合） |
| 全部分析 | 1 次 | 2 次（规划+执行后综合） |

**优化方向**：
- 可以缓存常见的工具选择决策
- 可以使用更快的模型进行规划
- 可以批量处理相似问题

---

## 错误处理

### 1. JSON 解析失败
```java
catch (JsonProcessingException e) {
    // 降级策略：全部分析
    return new ToolSelectionResponse(
        true,
        List.of("salary_analysis", "growth_analysis",
                "market_analysis", "risk_analysis"),
        "解析失败，使用默认全部分析"
    );
}
```

### 2. 工具执行失败
```java
try {
    String result = tool.analyze(profile, userMessage);
    results.put(toolName, result);
} catch (Exception e) {
    // 单个工具失败不影响其他工具
    results.put(toolName, buildErrorMessage(tool, e));
}
```

### 3. LLM 调用失败
```java
try {
    String response = chatModel.generate(prompt);
    return response;
} catch (Exception e) {
    return "抱歉，AI分析服务暂时不可用。";
}
```

---

## 监控和日志

### 关键日志点

```java
// 1. 用户请求
log.info("Processing user message: {}", userMessage);

// 2. 工具规划
log.info("Tool selection: needsAnalysis={}, tools={}, reasoning={}",
    selection.getNeedsAnalysis(), selection.getTools(), selection.getReasoning());

// 3. 工具执行
log.info("Executing {} tools based on selection", selection.getToolCount());

// 4. 完成
log.info("Successfully processed request for conversation: {}", conversationId);
```

### 可观察性指标

- 工具使用频率分布
- LLM 决策准确率
- 平均响应时间
- JSON 解析失败率
