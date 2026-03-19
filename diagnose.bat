@echo off
chcp 65001 >nul
echo ====================================
echo 跳槽决策 Agent - 环境诊断工具
echo ====================================
echo.

set ERRORS=0

echo [1/6] 检查 Java 环境...
echo ------------------------------------
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [❌ ERROR] 未找到 Java
    echo.
    echo 请先安装 Java 17 或更高版本：
    echo 1. 访问 https://adoptium.net/
    echo 2. 下载并安装 JDK 17
    echo 3. 配置 JAVA_HOME 环境变量
    echo 4. 将 %%JAVA_HOME%%\bin 添加到 PATH
    echo.
    set /a ERRORS+=1
) else (
    echo [✅ OK] Java 已安装
    java -version
)
echo.

echo [2/6] 检查 Java 版本...
echo ------------------------------------
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%i
echo 检测到版本: %JAVA_VER%
echo 需要: Java 17+
echo.

echo [3/6] 检查 Maven 环境...
echo ------------------------------------
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [❌ ERROR] 未找到 Maven
    echo.
    echo 请先安装 Maven：
    echo 1. 访问 https://maven.apache.org/download.cgi
    echo 2. 下载并解压 Maven
    echo 3. 配置 MAVEN_HOME 环境变量
    echo 4. 将 %%MAVEN_HOME%%\bin 添加到 PATH
    echo.
    set /a ERRORS+=1
) else (
    echo [✅ OK] Maven 已安装
    call mvn -version ^| findstr "Apache Maven"
)
echo.

echo [4/6] 检查项目文件...
echo ------------------------------------
if not exist "pom.xml" (
    echo [❌ ERROR] 未找到 pom.xml
    set /a ERRORS+=1
) else (
    echo [✅ OK] pom.xml 存在
)

if not exist "src\main\java\com\decisionagent\DecisionAgentApplication.java" (
    echo [❌ ERROR] 未找到主启动类
    set /a ERRORS+=1
) else (
    echo [✅ OK] 主启动类存在
)

if not exist "src\main\resources\application.yml" (
    echo [❌ ERROR] 未找到配置文件
    set /a ERRORS+=1
) else (
    echo [✅ OK] 配置文件存在
)
echo.

echo [5/6] 检查端口占用...
echo ------------------------------------
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel% equ 0 (
    echo [⚠️  WARNING] 端口 8080 已被占用
    echo 占用端口情况：
    netstat -ano ^| findstr ":8080"
    echo.
    echo 解决方法：
    echo 1. 关闭占用端口的程序
    echo 2. 或修改 application.yml 中的 server.port
    echo.
) else (
    echo [✅ OK] 端口 8080 可用
)
echo.

echo [6/6] 检查配置文件...
echo ------------------------------------
if exist "src\main\resources\application.yml" (
    echo [✅ OK] application.yml 存在
    findstr "api-key" src\main\resources\application.yml
    echo.
    echo 当前配置：
    findstr "base-url" src\main\resources\application.yml
    findstr "model-name" src\main\resources\application.yml
) else (
    echo [❌ ERROR] 配置文件缺失
    set /a ERRORS+=1
)
echo.

echo ====================================
echo 诊断总结
echo ====================================
if %ERRORS% GTR 0 (
    echo 发现 %ERRORS% 个错误，请先解决以上问题
    echo.
    echo 常见问题快速修复：
    echo.
    echo 1. Java 未安装：
    echo    - 下载: https://adoptium.net/
    echo    - 选择 JDK 17 LTS 版本
    echo.
    echo 2. Maven 未安装：
    echo    - 下载: https://maven.apache.org/download.cgi
    echo    - 解压到 C:\Program Files\Maven
    echo.
    echo 3. 环境变量配置：
    echo    - 右键"此电脑" -> 属性 -> 高级系统设置
    echo    - 环境变量 -> 新建
    echo    - JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x
    echo    - MAVEN_HOME = C:\Program Files\Maven
    echo    - PATH 添加: %%JAVA_HOME%%\bin;%%MAVEN_HOME%%\bin
    echo.
) else (
    echo ✅ 环境检查通过！
    echo.
    echo 下一步：
    echo   1. 编译项目: mvn clean compile
    echo   2. 运行项目: mvn spring-boot:run
    echo   3. 访问界面: http://localhost:8080
)

echo ====================================
echo.
pause
