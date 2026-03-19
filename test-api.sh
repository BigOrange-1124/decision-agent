#!/bin/bash

# Decision Agent API Test Script
# 用于快速测试 API 接口

BASE_URL="http://localhost:8080"

echo "==================================="
echo "Decision Agent API Test"
echo "==================================="
echo ""

# 测试健康检查
echo "1. Testing health check..."
curl -s "$BASE_URL/api/health"
echo -e "\n"

# 测试基本咨询
echo "2. Testing career consultation..."
curl -s -X POST "$BASE_URL/api/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "我要不要跳槽？我现在月薪20k，工作3年"}'
echo -e "\n"

# 测试复杂咨询
echo "3. Testing detailed consultation..."
curl -s -X POST "$BASE_URL/api/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "我收到一个offer，给25k，但我现在20k，工作5年，该不该去？"}'
echo -e "\n"

echo "==================================="
echo "Test completed!"
echo "==================================="
