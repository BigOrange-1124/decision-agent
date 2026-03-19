# 实时 Agent 思考过程展示 - 完整指南

## 🎯 核心特性

使用 **SSE (Server-Sent Events)** 实时推送 Agent 的思考过程，前端可以实时看到 AI 的决策过程。

---

## 📋 API 端点

### GET /api/chat/stream?message=xxx

实时推送 Agent 的思考过程。

#### 事件类型

| 事件 | 说明 | 示例 |
|------|------|------|
| `step` | 步骤名称 | "提取信息"、"AI 思考"、"执行分析" |
| `thinking` | AI 思考内容 | "AI 正在分析问题..." |
| `info` | 信息提示 | "提取到薪资：20k" |
| `decision` | AI 决策 | "AI 决定分析以下维度：" |
| `tool_selected` | 选中的工具 | "收入维度"、"成长维度" |
| `tool_start` | 工具开始执行 | "收入维度分析" |
| `progress` | 执行进度 | "当前薪资：20k"、"市场对比：111%" |
| `tool_end` | 工具执行完成 | "收入维度分析" |
| `final` | 最终建议 | "根据以上分析..." |
| `done` | 完成 | "分析完成" |

---

## 🎨 前端实现

### 完整示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>真 Agent - 实时思考过程</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            padding: 30px;
        }
        .timeline {
            margin-top: 30px;
            padding-left: 30px;
            border-left: 3px solid #e0e0e0;
        }
        .timeline-item {
            margin-bottom: 20px;
            padding-left: 20px;
            position: relative;
        }
        .timeline-item::before {
            content: '';
            position: absolute;
            left: -34px;
            top: 5px;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: #667eea;
        }
        .step {
            font-weight: bold;
            color: #667eea;
        }
        .thinking {
            color: #ffc107;
            font-style: italic;
        }
        .tool {
            background: #e8f5e9;
            padding: 10px;
            border-left: 4px solid #28a745;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🤖 真 Agent - 实时思考过程</h1>
        <textarea id="userMessage" placeholder="请输入问题..."></textarea>
        <button onclick="startAnalysis()">🚀 开始分析</button>
        <div id="timeline" class="timeline"></div>
    </div>
    <script>
        function startAnalysis() {
            const message = document.getElementById('userMessage').value;
            const eventSource = new EventSource('/api/chat/stream?message=' + encodeURIComponent(message));

            eventSource.addEventListener('step', (e) => {
                addStep(e.data);
            });

            eventSource.addEventListener('thinking', (e) => {
                addThinking(e.data);
            });

            eventSource.addEventListener('tool_start', (e) => {
                addToolStart(e.data);
            });

            eventSource.addEventListener('progress', (e) => {
                addProgress(e.data);
            });

            eventSource.addEventListener('final', (e) => {
                addFinal(e.data);
            });

            eventSource.addEventListener('done', (e) => {
                addDone();
                eventSource.close();
            });
        }

        function addStep(text) {
            append('<div class="timeline-item"><div class="step">📍 ' + text + '</div></div>');
        }

        function addThinking(text) {
            append('<div class="timeline-item"><div class="thinking">🤔 ' + text + '</div></div>');
        }

        function addToolStart(text) {
            append('<div class="timeline-item"><div class="step">⚙️ ' + text + '</div><div class="tool" id="tool"></div></div>');
        }

        function addProgress(text) {
            const tool = document.querySelector('#tool');
            if (tool) tool.innerHTML += '→ ' + text + '<br>';
        }

        function addFinal(text) {
            append('<div class="timeline-item"><div style="background: #fff3cd; padding: 15px;">💡 ' + text + '</div></div>');
        }

        function addDone() {
            append('<div class="timeline-item"><div style="color: #28a745; font-weight: bold;">✅ 分析完成</div></div>');
        }

        function append(html) {
            document.getElementById('timeline').innerHTML += html;
        }
    </script>
</body>
</html>
```

---

## 📊 实时展示效果

### 用户输入："我现在20k，工作3年，想跳槽能涨多少？"

```
┌─────────────────────────────────────────────┐
│ 📍 提取信息                                │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ ℹ️ 提取到薪资：20k                         │
│ ℹ️ 提取到工作年限：3年                       │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📍 AI 思考                                  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 🤔 AI 正在分析问题，决定需要哪些分析维度...  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📍 AI 决策                                  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ AI 决定分析以下维度：                        │
│ ✓ 收入维度                                  │
│ ✓ 成长维度                                  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📍 执行分析                                  │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ ⚙️ 收入维度分析                              │
│ → 当前薪资：20.0k                           │
│ → 市场对比：111%                             │
│ → 评估：薪资处于正常水平                     │
│ → 预期涨幅：15-30%                           │
│ ✓ 完成                                      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ ⚙️ 成长维度分析                              │
│ → 职业阶段：成长阶段                         │
│ → 建议：可以考虑跳槽                         │
│ ✓ 完成                                      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📍 AI 生成建议                                │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 🤔 AI 正在基于分析结果生成综合建议...        │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 📍 完成                                      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ 💡 综合建议                                   │
│ 根据以上分析，给出以下建议：                 │
│ 1. 薪资方面：你的薪资处于正常水平...          │
│ 2. 成长方面：可以考虑跳槽...                 │
│ 3. 综合建议：建议跳槽，可以争取 15-30% 涨幅    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ ✅ 分析完成                                  │
└─────────────────────────────────────────────┘
```

---

## 🔧 后端实现

### 核心代码

```java
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamChat(@RequestParam String message) {
    SseEmitter emitter = new SseEmitter(300000L);

    executorService.submit(() -> {
        try {
            // 步骤 1：提取信息
            emitEvent(emitter, "step", "提取信息");
            UserProfile profile = profileExtractor.extract(message);
            emitEvent(emitter, "info", "提取到薪资：" + profile.getMonthlySalary() + "k");

            // 步骤 2：AI 思考
            emitEvent(emitter, "step", "AI 思考");
            emitEvent(emitter, "thinking", "AI 正在分析问题...");

            // 步骤 3：AI 决策
            emitEvent(emitter, "step", "AI 决策");
            emitEvent(emitter, "decision", "AI 决定分析以下维度：");
            emitEvent(emitter, "tool_selected", "收入维度");

            // 步骤 4：执行工具
            emitEvent(emitter, "step", "执行分析");
            emitEvent(emitter, "tool_start", "收入维度分析");
            emitEvent(emitter, "progress", "当前薪资：20k");
            emitEvent(emitter, "progress", "市场对比：111%");
            emitEvent(emitter, "tool_end", "收入维度分析");

            // 步骤 5：生成建议
            emitEvent(emitter, "step", "AI 生成建议");
            emitEvent(emitter, "final", llmAdvice);
            emitEvent(emitter, "done", "分析完成");

        } finally {
            emitter.complete();
        }
    });

    return emitter;
}

private void emitEvent(SseEmitter emitter, String event, String data) {
    emitter.send(SseEmitter.event().name(event).data(data));
}
```

---

## 🎯 与之前的对比

### ❌ 之前：一次性返回

```javascript
// 发送请求
const response = await fetch('/api/chat', {
    method: 'POST',
    body: JSON.stringify({ message: '...' })
});

// 等待...（5秒）
const data = await response.json();

// 一次性返回所有结果
console.log(data.trace);
// 用户需要等待 5 秒才能看到结果
```

### ✅ 现在：实时推送

```javascript
// 创建 SSE 连接
const eventSource = new EventSource('/api/chat/stream?message=...');

// 实时接收事件
eventSource.addEventListener('step', (e) => {
    // 立即显示步骤
    console.log('步骤：', e.data);
});

eventSource.addEventListener('thinking', (e) => {
    // 实时显示思考
    console.log('思考：', e.data);
});

eventSource.addEventListener('progress', (e) => {
    // 实时显示进度
    console.log('进度：', e.data);
});

// 用户可以实时看到 Agent 的思考过程！
```

---

## 🚀 启动应用

### 1. 启动 Spring Boot

```bash
mvn spring-boot:run
```

### 2. 访问前端

```
http://localhost:8080/index.html
```

### 3. 输入问题并分析

```
"我现在20k，工作3年，想跳槽能涨多少？"
```

### 4. 实时看到 Agent 的思考过程

```
📍 提取信息
ℹ️ 提取到薪资：20k
ℹ️ 提取到工作年限：3年

📍 AI 思考
🤔 AI 正在分析问题...

📍 AI 决策
AI 决定分析以下维度：
✓ 收入维度
✓ 成长维度

📍 执行分析
⚙️ 收入维度分析
→ 当前薪资：20k
→ 市场对比：111%
→ 预期涨幅：15-30%
✓ 完成

⚙️ 成长维度分析
→ 职业阶段：成长
✓ 完成

💡 综合建议
根据以上分析，建议跳槽...

✅ 分析完成
```

---

## 📝 总结

### 核心优势

1. ✅ **实时推送**：前端实时接收 Agent 的思考过程
2. ✅ **可视化展示**：用户可以清楚地看到 AI 的决策过程
3. ✅ **更好的体验**：用户不需要等待，可以实时看到进度
4. ✅ **真正的 Agent**：体现了模型驱动的特点

### 技术实现

- **后端**：使用 SseEmitter 发送 SSE 事件
- **前端**：使用 EventSource 接收 SSE 事件
- **协议**：SSE (Server-Sent Events)
- **实时性**：毫秒级的实时推送

### 与真 Agent 的结合

- 使用 **LangChain4j 的 @Tool** 定义工具
- 使用 **SSE 实时推送**执行过程
- 完美展示"真 Agent"的思考过程
