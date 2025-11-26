@echo off
echo ====================================
echo 系统监控模块安装脚本
echo ====================================
echo.

echo [1/3] 检查数据库连接...
echo 请确保MySQL数据库正在运行
echo.

echo [2/3] 执行数据库脚本...
echo 请手动执行以下SQL文件：
echo E:\codes\idea workspace\Mobile-Classroom\sql\proj_fz_system_monitor.sql
echo.
echo 或使用以下命令（需要配置MySQL环境变量）：
echo mysql -u root -p database_name ^< "E:\codes\idea workspace\Mobile-Classroom\sql\proj_fz_system_monitor.sql"
echo.
pause

cd /d "E:\codes\idea workspace\Mobile-Classroom\ruoyi-ui"

echo [3/3] 安装前端依赖...
echo 正在检查并安装ECharts...
call npm list echarts >nul 2>&1
if errorlevel 1 (
    echo ECharts未安装，正在安装...
    call npm install echarts --save
    if errorlevel 1 (
        echo 安装失败，请手动执行: npm install echarts --save
        pause
        exit /b 1
    )
    echo ECharts安装成功！
) else (
    echo ECharts已安装
)

echo.
echo ====================================
echo 安装完成！
echo ====================================
echo.
echo 后续步骤：
echo 1. 启动后端服务（ruoyi-admin）
echo 2. 启动前端服务: npm run dev
echo 3. 访问 http://localhost:80
echo 4. 在菜单中找到"系统监控"模块
echo.
echo 详细文档请查看：
echo E:\codes\idea workspace\Mobile-Classroom\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\README_MONITOR.md
echo.
pause

