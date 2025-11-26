@echo off
echo ========================================
echo 系统监控模块 - 错误修复脚本
echo ========================================
echo.

echo [检查] 验证关键文件是否存在...
echo.

set PROJECT_PATH=E:\codes\idea workspace\Mobile-Classroom

echo 1. 检查 ServerMetrics.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\domain\ServerMetrics.java" (
    echo    [OK] ServerMetrics.java 存在
) else (
    echo    [错误] ServerMetrics.java 不存在！
    pause
    exit /b 1
)

echo 2. 检查 DatabaseMetrics.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\domain\DatabaseMetrics.java" (
    echo    [OK] DatabaseMetrics.java 存在
) else (
    echo    [错误] DatabaseMetrics.java 不存在！
    pause
    exit /b 1
)

echo 3. 检查 SystemMonitorMapper.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\mapper\SystemMonitorMapper.java" (
    echo    [OK] SystemMonitorMapper.java 存在
) else (
    echo    [错误] SystemMonitorMapper.java 不存在！
    pause
    exit /b 1
)

echo 4. 检查 ISystemMonitorService.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\service\ISystemMonitorService.java" (
    echo    [OK] ISystemMonitorService.java 存在
) else (
    echo    [错误] ISystemMonitorService.java 不存在！
    pause
    exit /b 1
)

echo 5. 检查 SystemMonitorServiceImpl.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\service\impl\SystemMonitorServiceImpl.java" (
    echo    [OK] SystemMonitorServiceImpl.java 存在
) else (
    echo    [错误] SystemMonitorServiceImpl.java 不存在！
    pause
    exit /b 1
)

echo 6. 检查 SystemMonitorController.java
if exist "%PROJECT_PATH%\ruoyi-admin\src\main\java\com\ruoyi\web\controller\proj_fz\SystemMonitorController.java" (
    echo    [OK] SystemMonitorController.java 存在
) else (
    echo    [错误] SystemMonitorController.java 不存在！
    pause
    exit /b 1
)

echo.
echo ========================================
echo 所有文件检查通过！
echo ========================================
echo.

echo [修复] 现在执行修复步骤...
echo.

echo 步骤1: 清理Maven编译缓存
cd /d "%PROJECT_PATH%"
call mvn clean
if errorlevel 1 (
    echo Maven clean 失败，请检查Maven配置
    pause
    exit /b 1
)
echo    [OK] Maven缓存已清理

echo.
echo 步骤2: 重新编译项目
call mvn compile -DskipTests
if errorlevel 1 (
    echo Maven编译失败，请查看错误信息
    pause
    exit /b 1
)
echo    [OK] 项目编译成功

echo.
echo ========================================
echo 修复完成！
echo ========================================
echo.
echo 接下来请执行以下操作：
echo.
echo 1. 在IntelliJ IDEA中：
echo    File -^> Invalidate Caches -^> Invalidate and Restart
echo.
echo 2. 等待IDEA重启完成
echo.
echo 3. 执行数据库脚本：
echo    source %PROJECT_PATH%\sql\proj_fz_system_monitor.sql
echo.
echo 4. 启动后端服务：
echo    cd ruoyi-admin
echo    mvn spring-boot:run
echo.
echo 5. 启动前端服务：
echo    cd ruoyi-ui
echo    npm run dev
echo.
pause

