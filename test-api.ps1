# Decision Agent API Test Script for Windows PowerShell
# 用于快速测试 API 接口

$BASE_URL = "http://localhost:8080"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Decision Agent API Test" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 测试健康检查
Write-Host "1. Testing health check..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/api/health" -Method Get
Write-Host ""

# 测试基本咨询
Write-Host "2. Testing career consultation..." -ForegroundColor Yellow
$body1 = @{
    message = "我要不要跳槽？我现在月薪20k，工作3年"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BASE_URL/api/chat" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body1
Write-Host ""

# 测试复杂咨询
Write-Host "3. Testing detailed consultation..." -ForegroundColor Yellow
$body2 = @{
    message = "我收到一个offer，给25k，但我现在20k，工作5年，该不该去？"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BASE_URL/api/chat" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body2
Write-Host ""

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Test completed!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan
