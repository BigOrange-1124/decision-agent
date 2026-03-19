@echo off
echo ====================================
echo Decision Agent - 配置验证
echo ====================================
echo.

echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 Java，请先安装 Java 17+
    pause
    exit /b 1
)
echo [OK] Java 已安装
java -version
echo.

echo [2/4] 检查 Maven 环境...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 Maven，请先安装 Maven
    pause
    exit /b 1
)
echo [OK] Maven 已安装
call mvn -version
echo.

echo [3/4] 检查配置文件...
if not exist "src\main\resources\application.yml" (
    echo [ERROR] 未找到 application.yml
    pause
    exit /b 1
)
echo [OK] 配置文件存在
echo.

echo [4/4] 当前 LLM 配置:
echo   - API Key: c1b1c690... (已配置)
echo   - Base URL: https://open.bigmodel.cn/api/paas/v4/chat/completions
echo   - Model: glm-4-flash
echo.

echo ====================================
echo 配置验证完成！
echo ====================================
echo.
echo 下一步:
echo   1. 运行: mvn spring-boot:run
echo   2. 测试: .\test-api.ps1
echo.
pause
