# 项目文件清单

## 📁 项目结构总览

```
DecisionAgent/
│
├── 📄 pom.xml                                    # Maven 配置文件
├── 📄 .gitignore                                 # Git 忽略文件配置
├── 📄 README.md                                  # 项目说明文档
├── 📄 QUICKSTART.md                              # 快速启动指南
├── 📄 test-api.sh                                # API 测试脚本 (Linux/Mac)
├── 📄 test-api.ps1                               # API 测试脚本 (Windows)
│
├── src/
│   ├── main/
│   │   ├── java/com/decisionagent/
│   │   │   │
│   │   │   ├── 🚀 DecisionAgentApplication.java              # 主启动类
│   │   │   │
│   │   │   ├── agent/
│   │   │   │   └── 🤖 DecisionAgent.java                    # 核心决策 Agent
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── 🌐 CorsConfig.java                       # CORS 跨域配置
│   │   │   │   └── 🛡️ GlobalExceptionHandler.java            # 全局异常处理
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   └── 🎮 ChatController.java                    # REST API 控制器
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── 📝 ChatRequest.java                       # 请求 DTO
│   │   │   │   ├── 📤 ChatResponse.java                      # 响应 DTO
│   │   │   │   └── 👤 UserProfile.java                       # 用户信息 DTO
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── 🧠 LlmService.java                        # LLM 服务
│   │   │   │   └── 🔍 UserProfileExtractor.java              # 信息提取服务
│   │   │   │
│   │   │   └── tool/
│   │   │       ├── 🔧 AnalysisTool.java                      # Tool 接口
│   │   │       └── impl/
│   │   │           ├── 💰 SalaryAnalysisTool.java            # 收入分析 Tool
│   │   │           ├── 📈 GrowthAnalysisTool.java            # 成长分析 Tool
│   │   │           ├── 📊 MarketAnalysisTool.java            # 市场分析 Tool
│   │   │           └── ⚠️ RiskAnalysisTool.java              # 风险分析 Tool
│   │   │
│   │   └── resources/
│   │       ├── ⚙️ application.yml                            # 主配置文件
│   │       └── 🛠️ application-dev.yml                        # 开发环境配置
│   │
│   └── test/
│       └── java/com/decisionagent/
│           ├── service/
│           │   └── ✅ UserProfileExtractorTest.java          # 信息提取测试
│           └── tool/
│               └── impl/
│                   └── ✅ SalaryAnalysisToolTest.java       # 薪资分析测试
```

---

## 📊 文件统计

| 类型 | 数量 | 说明 |
|------|------|------|
| Java 源文件 | 17 | 核心业务代码 |
| 测试文件 | 2 | 单元测试 |
| 配置文件 | 2 | application.yml, pom.xml |
| 文档文件 | 4 | README, QUICKSTART 等 |
| 脚本文件 | 2 | 测试脚本 |

**总计：27 个文件**

---

## 🔑 核心文件说明

### 业务核心

| 文件 | 行数 | 职责 |
|------|------|------|
| `DecisionAgent.java` | ~150 | Agent 核心逻辑，协调各 Tool |
| `LlmService.java` | ~80 | 封装 LangChain4j LLM 调用 |
| `ChatController.java` | ~100 | REST API 接口 |

### Tool 实现

| Tool | 职责 |
|------|------|
| `SalaryAnalysisTool` | 薪资水平分析、市场对比 |
| `GrowthAnalysisTool` | 职业成长、技能发展评估 |
| `MarketAnalysisTool` | 市场环境、招聘趋势分析 |
| `RiskAnalysisTool` | 跳槽风险评估、规避建议 |

### 服务层

| Service | 职责 |
|---------|------|
| `LlmService` | OpenAI API 封装 |
| `UserProfileExtractor` | 正则提取用户信息 |

---

## 🚀 启动流程

1. **配置 API Key**
   ```bash
   export OPENAI_API_KEY=sk-xxx
   ```

2. **编译项目**
   ```bash
   mvn clean install
   ```

3. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

4. **测试接口**
   ```bash
   ./test-api.sh  # Linux/Mac
   .\test-api.ps1 # Windows
   ```

---

## 📝 使用示例

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我要不要跳槽？我现在月薪20k，工作3年，做Java开发"
  }'
```

---

## 🛠️ 技术栈

- **Java 17** - 编程语言
- **Spring Boot 3.2.0** - 应用框架
- **LangChain4j 0.34.0** - LLM 集成框架
- **OpenAI GPT-4** - 大语言模型
- **Maven** - 构建工具
- **Lombok** - 代码简化

---

## 📚 文档导航

- 📖 **README.md** - 项目完整说明
- ⚡ **QUICKSTART.md** - 快速启动指南
- 📁 **本项目文件** - 项目结构总览

---

## ✨ 特性亮点

✅ 多维度分析框架
✅ 可扩展 Tool 机制
✅ 智能 NER 信息提取
✅ LLM 综合决策
✅ REST API 接口
✅ 企业级代码规范
✅ 完整单元测试
✅ 详细文档说明

---

**版本：** 1.0.0
**作者：** DecisionAgent Team
**日期：** 2026-03-19
