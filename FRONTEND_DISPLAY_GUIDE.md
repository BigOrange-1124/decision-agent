# 前端展示指南 - 模型驱动 Agent 执行轨迹

## 📊 API 响应结构

### 完整的响应示例

```json
{
  "response": "## 📊 职业决策分析报告\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n**分析维度**：收入维度、成长维度\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n【收入维度分析】\n...",
  "trace": {
    "conversationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userMessage": "我现在20k，想跳槽能涨多少？",
    "extractedProfile": {
      "monthlySalary": 20,
      "workYears": null,
      "currentPosition": null
    },
    "planning": {
      "needsAnalysis": true,
      "selectedTools": ["salary_analysis"],
      "reasoning": "用户询问薪资涨幅"
    },
    "toolExecutions": [
      {
        "toolName": "salary_analysis",
        "displayName": "收入维度分析",
        "status": "success",
        "result": "【收入维度分析】\n• 当前薪资：20k\n• 市场对比：...",
        "durationMs": 15
      }
    ],
    "skippedAnalysis": false,
    "llmAdvice": "根据你的薪资情况，建议跳槽，涨幅预计20-30%...",
    "finalResponse": "完整报告...",
    "startTime": "2024-03-19T10:30:00",
    "endTime": "2024-03-19T10:30:02",
    "totalDurationMs": 2345
  },
  "conversationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timestamp": "2024-03-19T10:30:02",
  "success": true
}
```

---

## 🎨 前端展示方案

### 方案 1：时间线展示（推荐）

展示 Agent 的完整思考过程，让用户看到"模型驱动"的特点。

```html
<!DOCTYPE html>
<html>
<head>
  <title>模型驱动 Agent - 执行轨迹</title>
  <style>
    .timeline {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }
    .timeline-item {
      position: relative;
      padding-left: 40px;
      margin-bottom: 30px;
    }
    .timeline-item::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background: #4CAF50;
      color: white;
      text-align: center;
      line-height: 30px;
      font-weight: bold;
    }
    .timeline-item.step-1::before { content: '1'; }
    .timeline-item.step-2::before { content: '2'; }
    .timeline-item.step-3::before { content: '3'; }
    .timeline-item.step-4::before { content: '4'; }
    .timeline-item.step-5::before { content: '5'; }

    .step-title {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 10px;
    }
    .step-content {
      background: #f5f5f5;
      padding: 15px;
      border-radius: 8px;
    }
    .badge {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      margin-right: 5px;
    }
    .badge-success { background: #4CAF50; color: white; }
    .badge-info { background: #2196F3; color: white; }
    .badge-warning { background: #FF9800; color: white; }
  </style>
</head>
<body>
  <div class="timeline" id="timeline"></div>

  <script>
    // 假设这是从 API 获取的数据
    const data = {
      response: "完整报告...",
      trace: {
        conversationId: "xxx",
        userMessage: "我现在20k，想跳槽能涨多少？",
        extractedProfile: { monthlySalary: 20 },
        planning: {
          needsAnalysis: true,
          selectedTools: ["salary_analysis"],
          reasoning: "用户询问薪资涨幅"
        },
        toolExecutions: [{
          toolName: "salary_analysis",
          displayName: "收入维度分析",
          status: "success",
          result: "【收入维度分析】\n• 当前薪资：20k...",
          durationMs: 15
        }],
        llmAdvice: "根据你的薪资情况...",
        totalDurationMs: 2345
      }
    };

    // 渲染时间线
    function renderTimeline(data) {
      const trace = data.trace;
      const html = `
        <!-- 步骤 1：用户输入 -->
        <div class="timeline-item step-1">
          <div class="step-title">👤 用户输入</div>
          <div class="step-content">
            ${trace.userMessage}
          </div>
        </div>

        <!-- 步骤 2：信息提取 -->
        <div class="timeline-item step-2">
          <div class="step-title">🔍 提取用户信息</div>
          <div class="step-content">
            ${formatProfile(trace.extractedProfile)}
          </div>
        </div>

        <!-- 步骤 3：LLM 规划 -->
        <div class="timeline-item step-3">
          <div class="step-title">🤖 LLM 工具规划</div>
          <div class="step-content">
            <div>
              <strong>选择的工具：</strong>
              ${trace.planning.selectedTools.map(t => `<span class="badge badge-info">${getToolDisplayName(t)}</span>`).join('')}
            </div>
            <div style="margin-top: 10px;">
              <strong>原因：</strong>${trace.planning.reasoning}
            </div>
          </div>
        </div>

        <!-- 步骤 4：工具执行 -->
        <div class="timeline-item step-4">
          <div class="step-title">⚙️ 执行分析工具</div>
          <div class="step-content">
            ${trace.toolExecutions.map(step => `
              <div style="margin-bottom: 15px;">
                <div>
                  <span class="badge badge-success">${step.displayName}</span>
                  <small>${step.durationMs}ms</small>
                </div>
                <pre style="margin-top: 5px; white-space: pre-wrap;">${step.result}</pre>
              </div>
            `).join('')}
          </div>
        </div>

        <!-- 步骤 5：LLM 建议 -->
        <div class="timeline-item step-5">
          <div class="step-title">💡 LLM 综合建议</div>
          <div class="step-content">
            ${trace.llmAdvice}
          </div>
        </div>

        <!-- 总耗时 -->
        <div style="text-align: center; color: #666; margin-top: 20px;">
          总耗时：${trace.totalDurationMs}ms
        </div>
      `;

      document.getElementById('timeline').innerHTML = html;
    }

    // 格式化用户信息
    function formatProfile(profile) {
      if (!profile) return '未提取到信息';
      const fields = [];
      if (profile.monthlySalary) fields.push(`薪资：${profile.monthlySalary}k`);
      if (profile.workYears) fields.push(`年限：${profile.workYears}年`);
      if (profile.currentPosition) fields.push(`职位：${profile.currentPosition}`);
      return fields.join('、') || '未提取到信息';
    }

    // 工具名称转中文
    function getToolDisplayName(toolName) {
      const map = {
        'salary_analysis': '收入维度',
        'growth_analysis': '成长维度',
        'market_analysis': '市场维度',
        'risk_analysis': '风险维度'
      };
      return map[toolName] || toolName;
    }

    // 渲染
    renderTimeline(data);
  </script>
</body>
</html>
```

---

### 方案 2：折叠卡片展示

```javascript
function renderCardView(data) {
  const trace = data.trace;

  return `
    <div class="card">
      <div class="card-header" onclick="toggleSection('user-input')">
        <h3>1️⃣ 用户输入</h3>
      </div>
      <div id="user-input" class="card-body">
        ${trace.userMessage}
      </div>
    </div>

    <div class="card">
      <div class="card-header" onclick="toggleSection('extracted-info')">
        <h3>2️⃣ 提取的信息</h3>
      </div>
      <div id="extracted-info" class="card-body">
        ${JSON.stringify(trace.extractedProfile, null, 2)}
      </div>
    </div>

    <div class="card">
      <div class="card-header" onclick="toggleSection('llm-planning')">
        <h3>3️⃣ LLM 规划</h3>
        <span class="badge badge-info">
          选择了 ${trace.planning.selectedTools.length} 个工具
        </span>
      </div>
      <div id="llm-planning" class="card-body">
        <p><strong>原因：</strong>${trace.planning.reasoning}</p>
        <div>
          ${trace.planning.selectedTools.map(t =>
            `<span class="badge badge-success">${getToolDisplayName(t)}</span>`
          ).join(' ')}
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header" onclick="toggleSection('tool-execution')">
        <h3>4️⃣ 工具执行</h3>
      </div>
      <div id="tool-execution" class="card-body">
        ${trace.toolExecutions.map(step => `
          <div class="tool-result">
            <h4>${step.displayName}</h4>
            <small>耗时：${step.durationMs}ms</small>
            <pre>${step.result}</pre>
          </div>
        `).join('')}
      </div>
    </div>

    <div class="card">
      <div class="card-header" onclick="toggleSection('llm-advice')">
        <h3>5️⃣ LLM 建议</h3>
      </div>
      <div id="llm-advice" class="card-body">
        ${trace.llmAdvice}
      </div>
    </div>
  `;
}
```

---

### 方案 3：React 组件示例

```jsx
import React from 'react';

const AgentExecutionTrace = ({ trace }) => {
  return (
    <div className="agent-trace">
      {/* 步骤指示器 */}
      <Steps current={5}>
        <Steps.Step title="用户输入" description={trace.userMessage} />
        <Steps.Step title="提取信息" description={formatProfile(trace.extractedProfile)} />
        <Steps.Step
          title="LLM 规划"
          description={
            <div>
              <div>原因：{trace.planning.reasoning}</div>
              <div>
                {trace.planning.selectedTools.map(tool => (
                  <Tag key={tool} color="blue">{getToolDisplayName(tool)}</Tag>
                ))}
              </div>
            </div>
          }
        />
        <Steps.Step
          title="执行工具"
          description={
            <div>
              {trace.toolExecutions.map(step => (
                <div key={step.toolName}>
                  <Tag color="green">{step.displayName}</Tag>
                  <small>{step.durationMs}ms</small>
                </div>
              ))}
            </div>
          }
        />
        <Steps.Step title="LLM 建议" description={trace.llmAdvice.substring(0, 50) + '...'} />
      </Steps>

      {/* 详细信息 */}
      <Collapse defaultActiveKey={['1']}>
        <Panel header="1️⃣ 用户输入" key="1">
          {trace.userMessage}
        </Panel>

        <Panel header="2️⃣ 提取的信息" key="2">
          <Descriptions column={1}>
            {trace.extractedProfile?.monthlySalary && (
              <Descriptions.Item label="薪资">{trace.extractedProfile.monthlySalary}k</Descriptions.Item>
            )}
            {trace.extractedProfile?.workYears && (
              <Descriptions.Item label="工作年限">{trace.extractedProfile.workYears}年</Descriptions.Item>
            )}
            {trace.extractedProfile?.currentPosition && (
              <Descriptions.Item label="职位">{trace.extractedProfile.currentPosition}</Descriptions.Item>
            )}
          </Descriptions>
        </Panel>

        <Panel header="3️⃣ LLM 规划" key="3">
          <Alert
            message={trace.planning.reasoning}
            type="info"
            showIcon
          />
          <div style={{ marginTop: 10 }}>
            {trace.planning.selectedTools.map(tool => (
              <Tag key={tool} color="blue" style={{ fontSize: 14 }}>
                {getToolDisplayName(tool)}
              </Tag>
            ))}
          </div>
        </Panel>

        <Panel header="4️⃣ 工具执行" key="4">
          {trace.toolExecutions.map(step => (
            <Card key={step.toolName} size="small" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <strong>{step.displayName}</strong>
                <small>{step.durationMs}ms</small>
              </div>
              <pre style={{ marginTop: 10 }}>{step.result}</pre>
            </Card>
          ))}
        </Panel>

        <Panel header="5️⃣ LLM 建议" key="5">
          <Alert
            message={trace.llmAdvice}
            type="success"
            showIcon
          />
        </Panel>
      </Collapse>
    </div>
  );
};

function formatProfile(profile) {
  if (!profile) return '未提取到信息';
  const fields = [];
  if (profile.monthlySalary) fields.push(`薪资：${profile.monthlySalary}k`);
  if (profile.workYears) fields.push(`年限：${profile.workYears}年`);
  if (profile.currentPosition) fields.push(`职位：${profile.currentPosition}`);
  return fields.join('、') || '未提取到信息';
}

function getToolDisplayName(toolName) {
  const map = {
    'salary_analysis': '收入维度',
    'growth_analysis': '成长维度',
    'market_analysis': '市场维度',
    'risk_analysis': '风险维度'
  };
  return map[toolName] || toolName;
}
```

---

## 🎯 关键展示点

### 1. LLM 规划结果（体现"模型驱动"）

这是最重要的展示点，让用户看到 LLM 的智能决策：

```javascript
// 显示 LLM 选择了哪些工具
<div>
  <h4>🤖 AI 决策</h4>
  <p>根据您的问题，AI 决定分析以下维度：</p>
  {trace.planning.selectedTools.map(tool => (
    <Badge>{getToolDisplayName(tool)}</Badge>
  ))}
  <p><strong>原因：</strong>{trace.planning.reasoning}</p>
</div>
```

### 2. 工具执行过程（体现"动态调用"）

```javascript
// 显示每个工具的执行结果
{trace.toolExecutions.map(step => (
  <div key={step.toolName}>
    <h4>{step.displayName}</h4>
    <Progress
      percent={100}
      status={step.status === 'success' ? 'success' : 'exception'}
      showInfo={false}
    />
    <small>耗时：{step.durationMs}ms</small>
    <pre>{step.result}</pre>
  </div>
))}
```

### 3. 完整思考过程（体现"Agent 特征"）

```javascript
// 用户输入 → 提取信息 → LLM 规划 → 执行工具 → 生成建议
<Steps>
  <Step title="用户输入" icon={<User />} />
  <Step title="提取信息" icon={<Search />} />
  <Step title="LLM 规划" icon={<Robot />} />
  <Step title="执行工具" icon={<Tool />} />
  <Step title="生成建议" icon={<Lightbulb />} />
</Steps>
```

---

## 📱 移动端展示

```javascript
// 简洁的移动端展示
function renderMobileTrace(trace) {
  return `
    <div class="mobile-trace">
      <div class="trace-item">
        <span class="icon">👤</span>
        <span class="text">${trace.userMessage}</span>
      </div>

      <div class="trace-item">
        <span class="icon">🔍</span>
        <span class="text">${formatProfile(trace.extractedProfile)}</span>
      </div>

      <div class="trace-item">
        <span class="icon">🤖</span>
        <span class="text">
          AI 选择：${trace.planning.selectedTools.map(getToolDisplayName).join('、')}
        </span>
      </div>

      <div class="trace-item">
        <span class="icon">⚙️</span>
        <span class="text">已执行 ${trace.toolExecutions.length} 个分析</span>
      </div>

      <div class="trace-item">
        <span class="icon">💡</span>
        <span class="text">${trace.llmAdvice.substring(0, 50)}...</span>
      </div>
    </div>
  `;
}
```

---

## 🎨 高亮展示"模型驱动"特点

```javascript
// 重点展示 LLM 的决策过程
function highlightModelDriven(trace) {
  return `
    <div class="model-driven-highlight">
      <div class="highlight-box">
        <h3>🤖 AI 智能决策</h3>
        <p>不是固定流程，而是根据您的问题动态选择分析维度</p>
        <div class="decision-process">
          <div class="decision-step">
            <strong>您的问题：</strong>
            <span>"${trace.userMessage}"</span>
          </div>
          <div class="arrow">↓</div>
          <div class="decision-step">
            <strong>AI 判断：</strong>
            <span>需要分析 ${trace.planning.selectedTools.length} 个维度</span>
          </div>
          <div class="arrow">↓</div>
          <div class="decision-step">
            <strong>执行的工具：</strong>
            <div class="tools">
              ${trace.planning.selectedTools.map(tool =>
                `<span class="tool-badge">${getToolDisplayName(tool)}</span>`
              ).join('')}
            </div>
          </div>
          <div class="arrow">↓</div>
          <div class="decision-step">
            <strong>原因：</strong>
            <span>${trace.planning.reasoning}</span>
          </div>
        </div>
      </div>
    </div>
  `;
}
```

---

## 总结

**关键展示点：**

1. ✅ **LLM 规划** - 展示 AI 选择了哪些工具，为什么选择
2. ✅ **工具执行** - 展示每个工具的执行过程和结果
3. ✅ **完整流程** - 用户输入 → 提取 → 规划 → 执行 → 建议
4. ✅ **动态特性** - 不同问题调用不同工具，体现"模型驱动"
5. ✅ **性能数据** - 展示每个步骤的耗时

**最佳实践：**

- 使用时间线或步骤条展示流程
- 用不同的颜色/图标区分不同步骤
- 折叠详细内容，保持界面简洁
- 移动端使用简化展示
- 高亮 LLM 决策过程，体现"模型驱动"特点
