# 智谱AI 集成配置指南

## 📋 配置说明

项目已配置为使用**智谱AI (BigModel)** 作为LLM提供商。

---

## 🔑 配置信息

### 当前配置

```yaml
llm:
  api-key: c1b1c690e1974f8b84312ae41145ebd2.X7gTmeJNhpKiSSNW
  base-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  model-name: glm-4-flash
  temperature: 0.7
  max-tokens: 8000
  timeout: 1200
```

### 参数说明

| 参数 | 值 | 说明 |
|------|------|------|
| `api-key` | 智谱AI API Key | 必填，用于身份验证 |
| `base-url` | 智谱AI API地址 | 使用兼容OpenAI格式的endpoint |
| `model-name` | glm-4-flash | 模型名称 |
| `temperature` | 0.7 | 输出随机性（0-1） |
| `max-tokens` | 8000 | 最大输出token数 |
| `timeout` | 1200 | 请求超时时间（秒） |

---

## 🚀 快速启动

### 方法一：直接启动（已配置）

```bash
cd D:\WorkSpace\DecisionAgent
mvn spring-boot:run
```

### 方法二：使用环境变量

```powershell
# Windows PowerShell
$env:ZHIPU_API_KEY="c1b1c690e1974f8b84312ae41145ebd2.X7gTmeJNhpKiSSNW"
mvn spring-boot:run
```

```bash
# Linux/Mac
export ZHIPU_API_KEY="c1b1c690e1974f8b84312ae41145ebd2.X7gTmeJNhpKiSSNW"
mvn spring-boot:run
```

---

## 🧪 测试接口

### PowerShell

```powershell
.\test-api.ps1
```

### cURL

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我要不要跳槽？我现在月薪20k，工作3年"}'
```

### 浏览器访问

```
http://localhost:8080
```

---

## 🔄 切换LLM提供商

### 切换回 OpenAI

修改 `application.yml`：

```yaml
llm:
  api-key: sk-your-openai-key-here
  base-url:  # 留空，使用默认OpenAI
  model-name: gpt-4o-mini
  temperature: 0.7
  max-tokens: 2000
  timeout: 60
```

### 使用其他兼容OpenAI的API

```yaml
llm:
  api-key: your-api-key
  base-url: https://your-api-endpoint.com/v1/chat/completions
  model-name: your-model-name
```

---

## 📊 智谱AI模型选择

| 模型 | 说明 | 适用场景 |
|------|------|----------|
| `glm-4-flash` | 速度快，成本低 | 日常对话、快速响应 |
| `glm-4-plus` | 性能均衡 | 通用场景 |
| `glm-4-0520` | 最新版本 | 复杂分析、深度推理 |
| `glm-4-air` | 轻量级 | 简单任务 |

**当前使用：** `glm-4-flash` （速度快，适合本项目的职业咨询场景）

---

## ⚙️ 自定义配置

### 修改模型

在 `application.yml` 中修改：

```yaml
llm:
  model-name: glm-4-plus  # 切换到其他模型
```

### 调整输出参数

```yaml
llm:
  temperature: 0.7    # 值越低输出越确定，值越高越随机
  max-tokens: 8000    # 最大输出长度
  timeout: 1200       # 请求超时时间（秒）
```

---

## 🔍 故障排查

### 问题1：连接超时

**原因：** 网络问题或智谱API不可用

**解决：**
```yaml
llm:
  timeout: 1800  # 增加超时时间
```

### 问题2：API Key 无效

**原因：** API Key 错误或已过期

**解决：**
1. 访问 [智谱AI开放平台](https://open.bigmodel.cn/)
2. 获取新的API Key
3. 更新配置

### 问题3：模型不存在

**原因：** 模型名称错误

**解决：**
```yaml
llm:
  model-name: glm-4-flash  # 确保使用正确的模型名
```

---

## 📈 性能优化建议

### 1. 根据场景选择模型

- **快速响应** → `glm-4-flash`
- **深度分析** → `glm-4-plus`
- **复杂推理** → `glm-4-0520`

### 2. 调整 token 限制

```yaml
llm:
  max-tokens: 8000  # 智谱AI支持较大输出
```

### 3. 控制随机性

```yaml
llm:
  temperature: 0.7  # 建议0.7，保持创造性和稳定性平衡
```

---

## 💡 成本参考

智谱AI定价（仅供参考，以官网为准）：

| 模型 | 输入价格 | 输出价格 |
|------|----------|----------|
| glm-4-flash | ¥0.1/千tokens | ¥0.1/千tokens |
| glm-4-plus | ¥0.5/千tokens | ¥0.5/千tokens |
| glm-4-0520 | ¥10/千tokens | ¥10/千tokens |

**估算：** 每次咨询约消耗 500-1000 tokens，成本约 ¥0.05-0.1

---

## 🔗 相关链接

- [智谱AI官网](https://bigmodel.cn/)
- [智谱AI开放平台](https://open.bigmodel.cn/)
- [智谱AI文档](https://open.bigmodel.cn/dev/api)
- [LangChain4j文档](https://docs.langchain4j.dev/)

---

## ✅ 验证配置

启动后查看日志，确认配置正确：

```
LLM Service initialized with model: glm-4-flash, provider: https://open.bigmodel.cn/api/paas/v4/chat/completions
```

如果看到以上日志，说明智谱AI配置成功！
