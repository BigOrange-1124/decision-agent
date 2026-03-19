# 跳槽决策 Agent (Decision Agent)

基于 Spring Boot + LangChain4j 的 AI 职业决策咨询系统。

## 项目简介

这是一个 MVP 版本的智能职业决策助手，用户可以用自然语言咨询跳槽相关问题，系统会从收入、成长、市场、风险四个维度进行专业分析，并给出综合建议。

### 核心特性

- 多维度分析（收入/成长/市场/风险）
- 智能信息提取（薪资、工作年限等）
- Tool 机制设计（可扩展分析维度）
- LLM 综合分析（基于 GPT-4）
- REST API 接口
- 企业级代码规范

---

## 技术栈

- Java 17
- Spring Boot 3.2.0
- LangChain4j 0.34.0
- 智谱AI GLM-4 / OpenAI GPT-4
- Maven
- Lombok

---

## 项目结构

```
DecisionAgent/
├── src/main/java/com/decisionagent/
│   ├── DecisionAgentApplication.java          # 主启动类
│   ├── agent/
│   │   └── DecisionAgent.java                 # 核心决策Agent
│   ├── config/
│   │   ├── CorsConfig.java                    # CORS配置
│   │   └── GlobalExceptionHandler.java        # 全局异常处理
│   ├── controller/
│   │   └── ChatController.java                # REST API控制器
│   ├── dto/
│   │   ├── ChatRequest.java                   # 请求DTO
│   │   ├── ChatResponse.java                  # 响应DTO
│   │   └── UserProfile.java                   # 用户信息DTO
│   ├── service/
│   │   ├── LlmService.java                    # LLM服务
│   │   └── UserProfileExtractor.java          # 信息提取服务
│   └── tool/
│       ├── AnalysisTool.java                  # Tool接口
│       └── impl/
│           ├── SalaryAnalysisTool.java        # 收入分析
│           ├── GrowthAnalysisTool.java        # 成长分析
│           ├── MarketAnalysisTool.java        # 市场分析
│           └── RiskAnalysisTool.java          # 风险分析
├── src/main/resources/
│   └── application.yml                        # 配置文件
└── pom.xml                                    # Maven配置
```

---

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- 智谱AI API Key 或 OpenAI API Key

### 2. 配置 API Key

#### 使用智谱AI（已配置）

项目已默认配置智谱AI，无需额外配置即可使用：

```yaml
llm:
  api-key: c1b1c690e1974f8b84312ae41145ebd2.X7gTmeJNhpKiSSNW
  base-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  model-name: glm-4-flash
```

#### 使用 OpenAI

编辑 `src/main/resources/application.yml`：

```yaml
llm:
  api-key: sk-your-openai-key-here
  base-url:  # 留空，使用默认OpenAI
  model-name: gpt-4o-mini
```

或通过环境变量设置：

```bash
# 使用智谱AI
export ZHIPU_API_KEY=your-zhipu-key

# 或使用OpenAI
export OPENAI_API_KEY=your-openai-key
```

### 3. 编译运行

```bash
# 编译
mvn clean install

# 运行
mvn spring-boot:run
```

### 4. 测试接口

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我要不要跳槽？我现在月薪20k，工作3年"}'
```

访问健康检查：
```bash
curl http://localhost:8080/api/health
```

---

## API 使用说明

### POST /api/chat

提交职业咨询问题，获取多维度分析报告。

**请求示例：**

```json
{
  "message": "我要不要跳槽？我现在月薪20k，工作3年，做Java开发"
}
```

**响应示例：**

```json
{
  "response": "## 📊 职业决策分析报告\n\n【收入维度分析】\n• 当前月薪：20.0k...",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00",
  "hasDetailedAnalysis": true
}
```

### GET /api/health

检查服务健康状态。

### GET /

查看 API 文档和使用说明。

---

## 扩展开发

### 添加新的分析维度

1. 创建新的 Tool 类：

```java
@Component
public class CustomAnalysisTool implements AnalysisTool {

    @Override
    public String getName() {
        return "自定义维度";
    }

    @Override
    public String analyze(UserProfile profile, String userMessage) {
        // 实现你的分析逻辑
        return "分析结果...";
    }
}
```

2. Spring 会自动扫描并注册该 Tool，无需额外配置

### 自定义 Prompt

修改 `DecisionAgent.java` 中的 `SYSTEM_PROMPT` 常量，调整 LLM 的输出风格。

### 替换 LLM 模型

在 `application.yml` 中修改：

```yaml
langchain4j:
  openai:
    model-name: gpt-4o  # 或其他模型
```

---

## 架构设计说明

### Tool 机制

所有分析工具实现 `AnalysisTool` 接口，Agent 会自动扫描并按优先级调用：

- 收入分析（SalaryAnalysisTool）
- 成长分析（GrowthAnalysisTool）
- 市场分析（MarketAnalysisTool）
- 风险分析（RiskAnalysisTool）

### 信息提取

`UserProfileExtractor` 使用正则表达式从自然语言中提取关键信息：

- 薪资：20k, 2万, 月薪20
- 工作年限：3年, 工作3年
- 职位：Java开发, 后端工程师

### LLM 调用流程

1. 用户输入 → 提取结构化信息
2. 依次调用 4 个 Tool 生成各维度分析
3. 汇总所有分析结果
4. 构建综合 Prompt
5. 调用 LLM 生成最终建议
6. 返回完整报告

---

## 常见问题

### 1. 智谱AI API Key 无效

检查 `application.yml` 中的 `llm.api-key` 配置是否正确，或访问 [智谱AI开放平台](https://open.bigmodel.cn/) 获取新的API Key。

### 2. OpenAI API Key 无效

检查 `application.yml` 中的 `llm.api-key` 配置是否正确。

### 2. 端口冲突

修改 `application.yml` 中的 `server.port`。

### 3. 中文乱码

确保系统编码为 UTF-8，IDE 编码设置为 UTF-8。

---

## License

MIT License

---

## 联系方式

如有问题，请提交 Issue 或 PR。
