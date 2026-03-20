# 🔌 ReAct Agent 接口对照文档

## 问题说明

之前 Spring Boot 静态资源目录下有个旧的 `index.html`，它还在调用旧的接口 `/api/chat/stream`，现在已经更新为调用 ReAct Agent 的正确接口。

---

## ✅ 正确的接口对照

### 1️⃣ ReAct Agent 接口（新）

| 接口 | 方法 | 说明 | 文件位置 |
|------|------|------|----------|
| `/api/react/chat` | POST | ReAct 普通接口（一次性返回） | ReActChatController |
| `/api/react/chat/stream` | GET | ReAct 流式接口（SSE 推送） | ReActStreamingController |

### 2️⃣ 一次性规划 Agent 接口（旧）

| 接口 | 方法 | 说明 | 文件位置 |
|------|------|------|----------|
| `/api/chat` | POST | 普通接口（一次性返回） | ChatController |
| `/api/chat/stream` | GET | 流式接口（SSE 推送） | StreamingChatController |

---

## 📁 前端文件对照

| 文件 | 使用的接口 | 技术栈 |
|------|-----------|---------|
| `frontend/react-demo.html` | ✅ `/api/react/chat/stream` | 纯 HTML |
| `frontend/ReActAgentChat.jsx` | ✅ `/api/react/chat/stream` | React |
| `src/main/resources/static/index.html` | ✅ `/api/react/chat/stream` | 纯 HTML（已修复） |

---

## 🚀 访问方式

### 方式 A：直接访问静态页面

```bash
# 启动后端
mvn spring-boot:run

# 浏览器访问
http://localhost:8199/index.html
```

### 方式 B：使用前端独立服务器

```bash
# 方式 1：使用 HTML 文件
cd frontend
python -m http.server 8000
# 访问 http://localhost:8000/react-demo.html

# 方式 2：使用 React 项目
npm start
# 访问 http://localhost:3000
```

---

## 📡 接口对比

### ReAct Agent 流式接口（推荐）

**接口**：`GET /api/react/chat/stream?message=xxx`

**SSE 事件**：
```
event: round
data: Round 1

event: thought
data: 用户询问薪资涨幅，我需要先分析收入水平

event: action
data: 收入维度分析

event: observation
data: 【收入维度分析】当前薪资20k，低于市场平均...

event: round
data: Round 2

event: thought
data: 已了解到薪资偏低，可以给出建议

event: final_answer
data: 根据分析，建议跳槽，预计涨幅20-30%

event: done
data: 分析完成（共 2 轮）
```

### 一次性规划 Agent 流式接口（旧）

**接口**：`GET /api/chat/stream?message=xxx`

**SSE 事件**：
```
event: step
data: 提取信息

event: info
data: ✓ 提取到薪资：20k

event: step
data: LLM 规划

event: tool_selected
data: 收入维度

event: tool_start
data: 收入维度分析

event: tool_result
data: 【收入维度分析】...

event: final
data: 根据你的情况...
```

---

## 🎯 如何选择

### 使用 ReAct Agent（推荐）

```javascript
// 前端代码
const eventSource = new EventSource(
  '/api/react/chat/stream?message=我要不要跳槽？'
);
```

**特点**：
- ✅ 逐步思考
- ✅ 每轮只选择一个工具
- ✅ 根据结果动态调整
- ✅ 更准确的最终答案

### 使用一次性规划 Agent

```javascript
// 前端代码
const eventSource = new EventSource(
  '/api/chat/stream?message=我现在20k'
);
```

**特点**：
- ⚡ 更快响应
- 📋 一次性规划所有工具
- 🔧 执行所有工具
- 📊 返回完整轨迹

---

## 🔧 配置 CORS

如果前端和后端不在同一个域名，需要配置 CORS：

```java
// CorsConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",  // React
                    "http://localhost:8000"   // HTML 静态服务器
                )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

---

## 📝 更新日志

### ✅ 已修复

- [x] `src/main/resources/static/index.html` - 更新为使用 `/api/react/chat/stream`
- [x] `frontend/react-demo.html` - 使用 `/api/react/chat/stream`
- [x] `frontend/ReActAgentChat.jsx` - 使用 `/api/react/chat/stream`

---

## 🎉 开始使用

### 1. 启动后端
```bash
mvn spring-boot:run
```

### 2. 访问前端

**选项 A：静态页面（最简单）**
```
http://localhost:8199/index.html
```

**选项 B：独立前端服务器**
```bash
cd frontend
python -m http.server 8000
# 访问 http://localhost:8000/react-demo.html
```

**选项 C：React 项目**
```bash
npm install
npm start
# 访问 http://localhost:3000
```

### 3. 测试

输入问题：`"我要不要跳槽？我现在20k，工作3年"`

观察 ReAct Agent 的思考过程：
- 💭 Round 1：思考 → 行动 → 观察
- 💭 Round 2：思考 → 行动 → 观察
- 💭 Round 3：最终答案

---

**🎉 接口已全部对齐，前端可以正常调用 ReAct Agent 了！**
