# 快速启动指南

## 方法一：使用 Maven 启动（推荐）

### 1. 配置 API Key

项目已默认配置智谱AI，可以直接启动：

```bash
mvn spring-boot:run
```

如果需要使用自己的API Key：

**使用智谱AI：**

**Windows (PowerShell):**
```powershell
$env:ZHIPU_API_KEY="your-zhipu-key"
```

**Linux/Mac:**
```bash
export ZHIPU_API_KEY="your-zhipu-key"
```

**使用 OpenAI：**

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-your-openai-key"
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-your-openai-key"
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 测试接口

**Windows (PowerShell):**
```powershell
.\test-api.ps1
```

**Linux/Mac:**
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 方法二：使用 IDE 启动

### 1. 导入项目

- IntelliJ IDEA: File → Open → 选择 pom.xml
- Eclipse: File → Import → Maven → Existing Maven Projects

### 2. 配置环境变量

在 IDE 中设置环境变量 `OPENAI_API_KEY`

### 3. 运行主类

运行 `DecisionAgentApplication.java`

---

## 测试示例

### cURL 命令

```bash
# 基本咨询
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我要不要跳槽？我现在月薪20k，工作3年"}'

# 复杂咨询
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我有两个offer：A公司30k但加班多，B公司25k但团队好，怎么选？"}'

# 成长咨询
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我现在工作1年，感觉学不到东西，要不要跳槽？"}'
```

### Postman

1. 创建新请求
2. Method: POST
3. URL: `http://localhost:8080/api/chat`
4. Headers: `Content-Type: application/json`
5. Body (raw JSON):
```json
{
  "message": "我要不要跳槽？我现在月薪20k，工作3年"
}
```

---

## 预期输出

系统会返回结构化的分析报告，包含：

```
## 📊 职业决策分析报告

【收入维度分析】
• 当前月薪：20.0k
• 市场对比：当前薪资约为市场平均水平的111%
• 薪资评估：◐ 你的薪资处于正常水平，跳槽可争取10-20%涨幅
...

【成长维度分析】
...

【市场维度分析】
...

【风险维度分析】
...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

## 💡 综合建议
（LLM 生成的个性化建议）
```

---

## 常见问题

### Q: 提示 API Key 无效
A: 检查环境变量是否正确设置，或直接修改 application.yml。智谱AI用户请访问 https://open.bigmodel.cn/ 获取API Key。

### Q: 端口 8080 被占用
A: 修改 application.yml 中的 server.port

### Q: Maven 依赖下载慢
A: 配置国内镜像源（如阿里云）

### Q: 中文乱码
A: 确保 IDE 和终端编码为 UTF-8

---

## 下一步

- 查看完整文档：README.md
- 添加自定义 Tool：参考 `AnalysisTool` 接口
- 调整 Prompt：修改 `DecisionAgent.SYSTEM_PROMPT`
- 替换 LLM 模型：修改 application.yml 中的 model-name

祝使用愉快！
