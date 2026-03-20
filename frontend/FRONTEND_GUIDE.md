# 🎨 ReAct Agent 前端集成指南

本文档说明如何在前端集成 ReAct Agent 流式接口。

---

## 📁 文件说明

### 1️⃣ react-demo.html
**纯 HTML 版本** - 无需框架，直接在浏览器打开即可使用

**优点**：
- ✅ 无需构建工具
- ✅ 无需依赖管理
- ✅ 快速原型验证
- ✅ 易于理解和修改

**适用场景**：
- 快速演示 ReAct 功能
- 不使用前端框架的项目
- 学习 SSE 集成方式

**使用方法**：
```bash
# 直接在浏览器打开
open frontend/react-demo.html

# 或使用本地服务器
python -m http.server 8000
# 然后访问 http://localhost:8000/frontend/react-demo.html
```

---

### 2️⃣ ReActAgentChat.jsx + ReActAgentChat.css
**React 组件版本** - 可集成到 React 项目

**优点**：
- ✅ 组件化设计
- ✅ 状态管理完善
- ✅ 易于维护和扩展
- ✅ 符合 React 最佳实践

**适用场景**：
- React + SPA 项目
- 需要深度定制 UI
- 大型前端应用

**使用方法**：
```bash
# 1. 复制文件到你的 React 项目
cp frontend/ReActAgentChat.jsx src/components/
cp frontend/ReActAgentChat.css src/components/

# 2. 在你的页面中使用
import ReActAgentChat from './components/ReActAgentChat';

function App() {
  return (
    <div>
      <ReActAgentChat apiUrl="http://localhost:8199" />
    </div>
  );
}
```

---

## 🚀 快速开始

### 方式 A：使用 HTML 版本（最快）

1. **打开页面**
   ```bash
   # 方式 1：直接双击打开
   react-demo.html

   # 方式 2：使用本地服务器（推荐）
   cd frontend
   python -m http.server 8000
   # 访问 http://localhost:8000/react-demo.html
   ```

2. **测试功能**
   - 点击示例问题
   - 或手动输入问题
   - 观察 AI 的思考过程

---

### 方式 B：集成到 React 项目

#### 步骤 1：安装依赖（如果需要）
```bash
# 你的项目应该已经有这些依赖
# 如果使用 Create React App，默认已包含
```

#### 步骤 2：复制组件文件
```bash
# 复制到你的 React 项目
cp ReActAgentChat.jsx src/components/
cp ReActAgentChat.css src/components/
```

#### 步骤 3：在你的页面中使用
```jsx
import ReActAgentChat from './components/ReActAgentChat';

function App() {
  return (
    <div className="app">
      <h1>我的应用</h1>
      <ReActAgentChat apiUrl="http://localhost:8199" />
    </div>
  );
}

export default App;
```

#### 步骤 4：配置 API 地址
```jsx
// 开发环境
<ReActAgentChat apiUrl="http://localhost:8199" />

// 生产环境（需要配置代理）
<ReActAgentChat apiUrl="/api" />
```

---

## 🔧 配置说明

### 1. 修改 API 地址

#### HTML 版本
```javascript
// 在 react-demo.html 中修改
const API_URL = 'http://localhost:8199'; // 你的后端地址
```

#### React 版本
```jsx
// 通过 props 传入
<ReActAgentChat apiUrl="http://your-backend:8199" />
```

### 2. 配置 CORS

后端已经配置了 CORS，但如果需要限制域名：

```java
// CorsConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000") // 你的前端地址
            .allowedMethods("GET", "POST", "OPTIONS")
            .allowedHeaders("*");
}
```

### 3. 配置代理（生产环境）

#### 使用 Nginx 代理
```nginx
location /api/ {
    proxy_pass http://localhost:8199/api/;
}
```

#### 使用 React 开发服务器代理
```javascript
// package.json
{
  "proxy": "http://localhost:8199"
}

// 或使用 http-proxy-middleware
// src/setupProxy.js
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8199',
      changeOrigin: true,
    })
  );
};
```

---

## 📡 SSE 事件处理

### 事件类型

| 事件名 | 触发时机 | 数据示例 |
|--------|---------|---------|
| `start` | 开始处理 | "开始 ReAct 分析..." |
| `round` | 新轮次开始 | "Round 1" |
| `thought` | LLM 思考 | "用户询问薪资涨幅..." |
| `action` | 选择工具 | "收入维度分析" |
| `observation` | 工具结果 | "【收入维度分析】..." |
| `final_answer` | 最终答案 | "根据分析，建议跳槽..." |
| `done` | 处理完成 | "分析完成（共 2 轮）" |
| `error` | 发生错误 | "处理失败：..." |

### 前端处理示例

```javascript
const eventSource = new EventSource(
  '/api/react/chat/stream?message=我要不要跳槽？'
);

// 监听各种事件
eventSource.addEventListener('round', (e) => {
  console.log('开始第 ' + e.data);
  displayRound(e.data);
});

eventSource.addEventListener('thought', (e) => {
  console.log('💭 ' + e.data);
  displayThought(e.data);
});

eventSource.addEventListener('action', (e) => {
  console.log('🎬 ' + e.data);
  displayAction(e.data);
});

eventSource.addEventListener('observation', (e) => {
  console.log('👁️ ' + e.data);
  displayObservation(e.data);
});

eventSource.addEventListener('final_answer', (e) => {
  console.log('✅ ' + e.data);
  displayFinalAnswer(e.data);
});

eventSource.addEventListener('done', (e) => {
  console.log('✓ ' + e.data);
  eventSource.close();
});

eventSource.addEventListener('error', (e) => {
  console.error('❌ ' + e.data);
  eventSource.close();
});
```

---

## 🎨 UI 展示建议

### 时间线展示

```
用户：我要不要跳槽？我现在20k，工作3年
         ↓
┌───────────────────────────────────┐
│ Round 1                           │
│ 💭 思考：用户询问薪资涨幅...      │
│ 🎬 行动：收入维度分析             │
│ 👁️ 观察：当前薪资20k，低于...     │
└───────────────────────────────────┘
         ↓
┌───────────────────────────────────┐
│ Round 2                           │
│ 💭 思考：已了解薪资偏低...        │
│ 🎬 行动：市场维度分析             │
│ 👁️ 观察：当前是金三银四期间...     │
└───────────────────────────────────┘
         ↓
┌───────────────────────────────────┐
│ ✅ 最终答案：根据分析，建议跳槽   │
└───────────────────────────────────┘
```

### 思维链展示

```
🤖 AI 思考过程：

Round 1:
  💭 "用户询问薪资涨幅，我需要先分析收入水平"
  🎬 执行：收入维度分析
  👁️ 结果：当前薪资20k，低于市场平均25k...

Round 2:
  💭 "已了解到薪资偏低，现在需要分析市场时机"
  🎬 执行：市场维度分析
  👁️ 结果：当前是金三银四期间，机会较多...

✅ 结论：根据分析，建议跳槽，预计涨幅20-30%
```

---

## 🔍 调试技巧

### 1. 查看原始事件

```javascript
eventSource.addEventListener('message', (e) => {
  console.log('原始事件：', e);
  console.log('事件类型：', e.type);
  console.log('事件数据：', e.data);
});
```

### 2. 监听所有事件

```javascript
const events = [
  'start', 'round', 'thought', 'action',
  'observation', 'final_answer', 'done', 'error'
];

events.forEach(eventName => {
  eventSource.addEventListener(eventName, (e) => {
    console.log(`[${eventName}]`, e.data);
  });
});
```

### 3. 错误处理

```javascript
eventSource.onerror = (error) => {
  console.error('SSE 连接错误：', error);

  // 根据错误类型处理
  if (eventSource.readyState === EventSource.CLOSED) {
    console.log('连接已关闭');
  } else if (eventSource.readyState === EventSource.CONNECTING) {
    console.log('正在重连...');
  }
};
```

---

## 🎯 最佳实践

### 1. 自动重连

```javascript
class ReActAgentClient {
  constructor(url) {
    this.url = url;
    this.reconnectAttempts = 3;
    this.currentAttempt = 0;
  }

  connect(message) {
    const eventSource = new EventSource(
      `${this.url}?message=${encodeURIComponent(message)}`
    );

    eventSource.onerror = () => {
      if (this.currentAttempt < this.reconnectAttempts) {
        this.currentAttempt++;
        setTimeout(() => {
          this.connect(message);
        }, 1000 * this.currentAttempt);
      }
    };

    return eventSource;
  }
}
```

### 2. 取消请求

```javascript
let eventSource = null;

function sendMessage(message) {
  // 取消之前的请求
  if (eventSource) {
    eventSource.close();
  }

  // 发起新请求
  eventSource = new EventSource(url);
  // ...
}

// 组件卸载时关闭连接
useEffect(() => {
  return () => {
    if (eventSource) {
      eventSource.close();
    }
  };
}, []);
```

### 3. 超时处理

```javascript
const TIMEOUT = 30000; // 30秒

function connectWithTimeout(message) {
  const eventSource = new EventSource(url);

  const timeoutId = setTimeout(() => {
    eventSource.close();
    showError('请求超时，请重试');
  }, TIMEOUT);

  eventSource.addEventListener('done', () => {
    clearTimeout(timeoutId);
  });

  return eventSource;
}
```

---

## 🐛 常见问题

### 1. CORS 错误

**错误信息**：
```
Access to fetch at 'http://localhost:8199' from origin 'http://localhost:3000'
has been blocked by CORS policy
```

**解决方案**：
```java
// 后端配置
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // 添加你的前端地址
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

### 2. SSE 连接断开

**可能原因**：
- 后端服务停止
- 网络问题
- 代理配置错误

**解决方案**：
```javascript
eventSource.onerror = (error) => {
  console.warn('SSE 连接错误，尝试重连...');

  setTimeout(() => {
    if (!eventSource || eventSource.readyState === EventSource.CLOSED) {
      connect();
    }
  }, 3000);
};
```

### 3. 中文字符乱码

**解决方案**：
```javascript
// 确保使用 encodeURIComponent
const url = `${apiUrl}/api/react/chat/stream?message=${encodeURIComponent(message)}`;
```

---

## 📦 完整示例

### HTML 版本完整示例

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>ReAct Agent Demo</title>
  <style>
    /* 复制 react-demo.html 中的样式 */
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>🤖 ReAct Agent</h1>
      <p>AI 职业决策助手</p>
    </div>

    <div class="chat-container">
      <div class="chat-messages" id="chatMessages"></div>

      <div class="input-area">
        <input
          type="text"
          id="userInput"
          placeholder="输入你的问题..."
          onkeypress="if(event.key==='Enter') sendMessage()"
        >
        <button onclick="sendMessage()">发送</button>
      </div>
    </div>
  </div>

  <script>
    const API_URL = 'http://localhost:8199';
    let eventSource = null;

    function sendMessage() {
      const input = document.getElementById('userInput');
      const message = input.value.trim();

      if (!message) return;

      // 显示用户消息
      addMessage('user', message);
      input.value = '';

      // 连接 SSE
      const url = `${API_URL}/api/react/chat/stream?message=${encodeURIComponent(message)}`;
      eventSource = new EventSource(url);

      let currentRound = null;
      let aiMessageDiv = null;

      // 添加 AI 消息容器
      aiMessageDiv = addAIMessage();

      // 监听事件
      eventSource.addEventListener('round', (e) => {
        currentRound = parseInt(e.data.replace('Round ', ''));
        addThinkingRound(aiMessageDiv, currentRound);
      });

      eventSource.addEventListener('thought', (e) => {
        updateThinkingRound(aiMessageDiv, currentRound, 'thought', e.data);
      });

      eventSource.addEventListener('action', (e) => {
        updateThinkingRound(aiMessageDiv, currentRound, 'action', e.data);
      });

      eventSource.addEventListener('observation', (e) => {
        updateThinkingRound(aiMessageDiv, currentRound, 'observation', e.data);
      });

      eventSource.addEventListener('final_answer', (e) => {
        addFinalAnswer(aiMessageDiv, e.data);
      });

      eventSource.addEventListener('done', (e) => {
        setTimeout(() => {
          eventSource.close();
        }, 500);
      });

      eventSource.onerror = () => {
        eventSource.close();
      };
    }

    function addMessage(type, content) {
      const chatMessages = document.getElementById('chatMessages');
      const div = document.createElement('div');
      div.className = `message ${type}-message`;
      div.innerHTML = `<div class="bubble">${content}</div>`;
      chatMessages.appendChild(div);
      chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function addAIMessage() {
      const chatMessages = document.getElementById('chatMessages');
      const div = document.createElement('div');
      div.className = 'message ai-message';
      div.innerHTML = '<div class="bubble"><div class="typing-indicator"><span></span><span></span><span></span></div></div>';
      chatMessages.appendChild(div);
      chatMessages.scrollTop = chatMessages.scrollHeight;
      return div.querySelector('.bubble');
    }

    function addThinkingRound(container, roundNumber) {
      const roundDiv = document.createElement('div');
      roundDiv.className = 'thinking-round';
      roundDiv.id = `round-${roundNumber}`;
      roundDiv.innerHTML = `
        <div class="round-header">
          💭 Round ${roundNumber}
          <div class="loading-dots"><span></span><span></span><span></span></div>
        </div>
      `;
      container.appendChild(roundDiv);
      scrollToBottom();
    }

    function updateThinkingRound(container, roundNumber, field, content) {
      const roundDiv = container.querySelector(`#round-${roundNumber}`);
      if (!roundDiv) return;

      const icons = {
        thought: '💭',
        action: '🎬',
        observation: '👁️'
      };

      const contentDiv = document.createElement('div');
      contentDiv.className = 'round-content';
      contentDiv.innerHTML = `<strong>${icons[field]} ${field}：</strong>${content}`;

      roundDiv.appendChild(contentDiv);

      // 移除加载动画
      if (field === 'observation') {
        roundDiv.querySelector('.loading-dots')?.remove();
      }

      scrollToBottom();
    }

    function addFinalAnswer(container, answer) {
      const answerDiv = document.createElement('div');
      answerDiv.className = 'final-answer';
      answerDiv.innerHTML = `
        <h3>✅ 最终答案</h3>
        <div class="answer-content">${answer.replace(/\n/g, '<br>')}</div>
      `;
      container.appendChild(answerDiv);
      scrollToBottom();
    }

    function scrollToBottom() {
      const chatMessages = document.getElementById('chatMessages');
      chatMessages.scrollTop = chatMessages.scrollHeight;
    }
  </script>
</body>
</html>
```

---

## 🚀 部署建议

### 开发环境
```bash
# 前端（React）
npm start
# 运行在 http://localhost:3000

# 后端（Spring Boot）
mvn spring-boot:run
# 运行在 http://localhost:8199
```

### 生产环境
```bash
# 1. 构建前端
npm run build

# 2. 使用 Nginx 配置
# /etc/nginx/conf.d/react-agent.conf

server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/react-agent/build;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8199/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 3. 重启 Nginx
sudo systemctl restart nginx
```

---

**🎉 前端集成完成！现在可以在浏览器中体验 ReAct Agent 的思考过程了！**
