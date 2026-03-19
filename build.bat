@echo off
chcp 65001 >nul
echo ====================================
echo 编译项目
echo ====================================
echo.

echo [1/3] 清理旧的编译文件...
call mvn clean
if %errorlevel% neq 0 (
    echo [❌] 清理失败
    pause
    exit /b 1
)
echo [✅] 清理完成
echo.

echo [2/3] 编译项目...
call mvn compile -DskipTests
if %errorlevel% neq 0 (
    echo [❌] 编译失败
    echo.
    echo 请检查错误信息，常见问题：
    echo 1. Java 版本不是 17+
    echo 2. Maven 依赖下载失败
    echo 3. 代码语法错误
    pause
    exit /b 1
)
echo [✅] 编译成功
echo.

echo [3/3] 打包项目（可选）...
echo 是否打包？(Y/N)
set /p choice=
if /i "%choice%"=="Y" (
    call mvn package -DskipTests
    if %errorlevel% neq 0 (
        echo [❌] 打包失败
        pause
        exit /b 1
    )
    echo [✅] 打包成功
    echo.
    echo JAR 文件位置: target\decision-agent-1.0.0.jar
)
echo.

echo ====================================
echo ✅ 编译完成！
echo ====================================
echo.
echo 下一步：
echo   1. 运行项目: mvn spring-boot:run
echo   2. 或运行 JAR: java -jar target\decision-agent-1.0.0.jar
echo   3. 访问界面: http://localhost:8080
echo.
pause
