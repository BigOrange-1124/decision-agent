# 跳槽决策 Agent 升级文档

## 改造概述

将 Agent 从「**代码控制流程**」升级为「**LLM 模型驱动的 Tool Choosing Agent**」

---

## 改造前后对比

### 改造前（固定流程）

```
用户输入
    ↓
提取用户信息
    ↓
执行所有 4 个 Tool（固定顺序）
    ├─ SalaryAnalysisTool
    ├─ GrowthAnalysisTool
    ├─ MarketAnalysisTool
    └─ RiskAnalysisTool
    ↓
LLM 生成综合建议
    ↓
返回结果
```

**问题：**
- 每次都调用所有 Tool，即使某些维度用户不关心
- 无法根据问题灵活调整分析维度
- 浪费计算资源和时间

### 改造后（模型驱动）

```
用户输入
    ↓
提取用户信息
    ↓
🤖 LLM 决策：需要哪些分析维度？
    ├─ 判断是否需要工具
    ├─ 选择相关工具
    └─ 返回 JSON 格式的工具列表
    ↓
解析 LLM 返回的 JSON
    ↓
动态执行选中的 Tool
    ↓
LLM 基于工具结果生成最终建议
    ↓
返回结果（显示使用的分析维度）
```

**优势：**
- ✅ 智能选择相关分析维度
- ✅ 简单问题直接回答，无需工具
- ✅ 更像真正的 Agent 行为
- ✅ 节省计算资源

---

## 新增/修改的文件

### 1. 新增文件

#### `dto/ToolSelectionResponse.java`
**作用**：LLM 返回的工具选择结果 DTO

**字段说明**：
```java
{
  "needs_analysis": true,        // 是否需要工具分析
  "tools": ["salary_analysis"],  // 需要执行的工具列表
  "reasoning": "用户询问薪资"     // LLM 的选择原因
}
```

#### `service/ToolPlanningService.java`
**作用**：让 LLM 决定需要调用哪些工具

**核心方法**：
```java
public ToolSelectionResponse planTools(String userMessage, UserProfile profile)
```

**关键功能**：
- 构建规划 Prompt
- 解析 LLM 返回的 JSON
- 处理 JSON 清理（移除 markdown 代码块）
- 容错处理（解析失败时使用默认策略）

#### `service/ToolExecutor.java`
**作用**：动态执行选中的工具

**核心方法**：
```java
public Map<String, String> executeTools(
    ToolSelectionResponse selection,
    UserProfile profile,
    String userMessage
)
```

**关键功能**：
- 根据工具名称查找对应的 Tool 实现
- 执行工具并收集结果
- 格式化工具执行结果

### 2. 修改文件

#### `dto/UserProfile.java`
**新增方法**：
```java
public boolean isEmpty()
```
判断是否提取到有效信息

#### `agent/DecisionAgent.java`（完全重写）
**新流程**：
1. 提取用户信息
2. 调用 ToolPlanningService 让 LLM 决策
3. 判断是否需要工具分析
4. 如果不需要，生成直接回答
5. 如果需要，执行选中的工具
6. LLM 综合工具结果生成建议
7. 组合完整响应

---

## Prompt 设计

### Tool Selection Prompt

```java
private static final String PLANNING_SYSTEM_PROMPT = """
    你是一个职业决策分析系统的规划专家。你的任务是根据用户的问题，
    判断是否需要使用分析工具，以及需要使用哪些工具。

    你可以使用以下分析工具：

    1. **salary_analysis**（收入维度分析）
       - 适用场景：用户询问薪资水平、薪资对比、涨幅空间、薪资谈判等
       - 关键词：薪资、工资、收入、待遇、钱、涨薪、降薪、薪水

    2. **growth_analysis**（成长维度分析）
       - 适用场景：用户询问技术成长、职业发展、晋升机会、能力提升等
       - 关键词：成长、进步、学技术、晋升、发展、能力、提升、技术栈

    3. **market_analysis**（市场维度分析）
       - 适用场景：用户询问市场环境、行业趋势、跳槽时机、招聘情况等
       - 关键词：市场、行情、时机、趋势、机会、招聘、公司、行业

    4. **risk_analysis**（风险维度分析）
       - 适用场景：用户询问跳槽风险、试用期、公司稳定性、裸辞风险等
       - 关键词：风险、危险、试用期、稳定、安全、倒闭、裁员、裸辞

    ## 输出格式

    你必须严格按照以下 JSON 格式输出，不要添加任何其他文字：

    {
      "needs_analysis": true 或 false,
      "tools": ["工具名称1", "工具名称2"],
      "reasoning": "简要说明选择这些工具的原因"
    }
    """;
```

**设计要点**：
1. ✅ 明确每个工具的适用场景和关键词
2. ✅ 定义清晰的判断规则
3. ✅ 强制要求严格的 JSON 输出格式
4. ✅ 包含 reasoning 字段便于调试

---

## JSON 解析实现

### 关键代码

```java
private ToolSelectionResponse parsePlanningResponse(String llmResponse) {
    try {
        // 1. 清理响应（移除 markdown 代码块）
        String cleanedResponse = cleanJsonResponse(llmResponse);

        // 2. 解析 JSON
        ToolSelectionResponse response = objectMapper.readValue(
            cleanedResponse,
            ToolSelectionResponse.class
        );

        // 3. 验证和修复
        if (response.getNeedsAnalysis() == null) {
            response.setNeedsAnalysis(false);
        }
        if (response.getTools() == null) {
            response.setTools(List.of());
        }

        return response;

    } catch (JsonProcessingException e) {
        // 4. 容错处理：解析失败时使用默认策略
        return new ToolSelectionResponse(
            true,
            List.of("salary_analysis", "growth_analysis",
                    "market_analysis", "risk_analysis"),
            "解析失败，使用默认全部分析"
        );
    }
}

// 清理 JSON 响应
private String cleanJsonResponse(String response) {
    String cleaned = response.trim();

    // 移除 ```json ... ```
    if (cleaned.startsWith("```json")) {
        cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.substring(3);
    }

    if (cleaned.endsWith("```")) {
        cleaned = cleaned.substring(0, cleaned.length() - 3);
    }

    return cleaned.trim();
}
```

**容错处理**：
- ✅ 处理 LLM 返回的 markdown 代码块格式
- ✅ 验证必需字段，提供默认值
- ✅ 解析失败时降级为全部分析（保证可用性）

---

## 使用示例

### 场景 1：用户只关心薪资

**用户输入**：
```
我现在月薪 15k，想跳槽，能涨多少？
```

**LLM 决策**：
```json
{
  "needs_analysis": true,
  "tools": ["salary_analysis"],
  "reasoning": "用户询问薪资涨幅"
}
```

**系统行为**：
- 只执行 SalaryAnalysisTool
- 不执行其他 3 个工具
- 返回结果中显示"分析维度：收入维度"

### 场景 2：问候语

**用户输入**：
```
你好
```

**LLM 决策**：
```json
{
  "needs_analysis": false,
  "tools": [],
  "reasoning": "用户打招呼，无需分析"
}
```

**系统行为**：
- 不执行任何工具
- 直接生成友好的问候回复

### 场景 3：综合咨询

**用户输入**：
```
我有 3 年经验，现在是 Java 开发，月薪 20k，想跳槽，
有什么建议吗？
```

**LLM 决策**：
```json
{
  "needs_analysis": true,
  "tools": ["salary_analysis", "growth_analysis", "market_analysis"],
  "reasoning": "用户涉及薪资、成长和市场时机"
}
```

**系统行为**：
- 执行 3 个工具（不包含风险分析）
- 返回结果中显示"分析维度：收入维度、成长维度、市场维度"

---

## 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **LangChain4j 0.34.0**
- **Jackson**（JSON 处理）
- **Lombok**（代码简化）

---

## 关键特性

### 1. 真正的模型驱动
- LLM 决定是否使用工具
- LLM 决定使用哪些工具
- 代码只负责执行 LLM 的决策

### 2. 智能降级
- JSON 解析失败时，自动降级为全部分析
- 单个工具执行失败不影响其他工具
- 保证系统可用性

### 3. 结构清晰
- ToolPlanningService：负责规划
- ToolExecutor：负责执行
- DecisionAgent：负责协调
- 职责分离，易于维护

### 4. 易于扩展
- 新增工具只需：
  1. 实现 AnalysisTool 接口
  2. 在 ToolPlanningService 中添加描述
  3. 在 ToolExecutor 中添加映射

---

## 代码结构

```
src/main/java/com/decisionagent/
├── agent/
│   └── DecisionAgent.java          ✏️ 重写（新流程）
├── dto/
│   ├── UserProfile.java            ✏️ 新增 isEmpty()
│   └── ToolSelectionResponse.java   🆕 新增
├── service/
│   ├── LlmService.java              ✓ 保持不变
│   ├── ToolPlanningService.java     🆕 新增
│   ├── ToolExecutor.java            🆕 新增
│   └── UserProfileExtractor.java    ✓ 保持不变
└── tool/
    ├── AnalysisTool.java            ✓ 保持不变
    └── impl/
        ├── SalaryAnalysisTool.java  ✓ 保持不变
        ├── GrowthAnalysisTool.java  ✓ 保持不变
        ├── MarketAnalysisTool.java  ✓ 保持不变
        └── RiskAnalysisTool.java    ✓ 保持不变
```

---

## 测试建议

### 测试场景

1. **单一维度测试**
   ```
   我现在 20k，跳槽能涨多少？
   ```

2. **多维度测试**
   ```
   我有 3 年经验，月薪 15k，想跳槽，有什么风险？
   ```

3. **无需工具测试**
   ```
   你好
   谢谢
   ```

4. **模糊问题测试**
   ```
   我该不该跳槽？
   ```

### 验证点

- ✅ LLM 正确选择工具
- ✅ JSON 解析成功
- ✅ 只执行选中的工具
- ✅ 简单问题直接回答
- ✅ 日志清晰可读

---

## 后续优化方向

### 1. Prompt 优化
- 根据实际使用情况调整关键词
- 添加更多示例
- 优化 JSON 格式要求

### 2. 多轮对话支持
- 记住之前选择的工具
- 支持追问场景

### 3. 工具执行结果缓存
- 相似问题复用结果
- 减少 LLM 调用次数

### 4. 监控和统计
- 统计各工具使用频率
- 分析 LLM 决策准确率

---

## 总结

这次改造实现了从「固定流程」到「模型驱动」的转变：

| 维度 | 改造前 | 改造后 |
|------|--------|--------|
| 流程控制 | 代码控制 | LLM 决策 |
| 工具调用 | 固定全部 | 动态选择 |
| 简单问题 | 仍调用工具 | 直接回答 |
| 资源消耗 | 固定 4 次调用 | 1-4 次不等 |
| 扩展性 | 需要修改代码 | 只需配置 |
| Agent 特征 | 规则系统 | 真正的 Agent |

现在系统具备了真正的 Agent 行为特征：**感知、规划、行动、反馈**。
