# 系统监控模块使用说明

## 模块概述
系统监控模块是一个独立的监控系统，用于实时监控服务器性能、数据库状态、用户行为等关键指标，并提供告警和统计功能。

## 功能特性

### 1. 服务器/运行环境监控
- **实时指标采集**
  - CPU使用率
  - 内存占用（总量/已用/使用率）
  - 磁盘空间（总量/已用/使用率）
  - 网络带宽（上传/下载速率）
  - JVM堆内存使用情况
  - 线程数统计

- **可视化展示**
  - 实时指标卡片展示
  - 历史趋势曲线图（支持6/12/24小时）
  - CPU和内存仪表盘
  - 磁盘使用饼图

- **自动告警**
  - CPU使用率 > 80% 警告，> 90% 严重
  - 内存使用率 > 80% 警告，> 90% 严重
  - 磁盘使用率 > 85% 警告，> 95% 严重

### 2. 数据库监控
- **连接池监控**
  - 总连接数
  - 活跃连接数
  - 空闲连接数

- **性能指标**
  - 查询响应时间
  - 慢查询统计
  - QPS（每秒查询数）
  - TPS（每秒事务数）
  - 缓存命中率

- **存储监控**
  - 数据库大小
  - 表空间使用率

- **告警机制**
  - 慢查询数量 > 5 警告，> 10 严重
  - 查询响应时间 > 1s 警告，> 3s 严重

### 3. 监控记录管理
- **组合查询**
  - 按监控类型筛选
  - 按告警级别筛选
  - 按处理状态筛选
  - 按时间段查询

- **记录操作**
  - 查看详情（支持首条/上条/下条/末尾导航）
  - 处理告警（记录处理人、处理时间、处理备注）
  - 删除记录（单条/批量）
  - 导出Excel报表

- **打印功能**
  - 支持单条记录打印
  - 包含报表标题、统计时间段、打印时间等信息

### 4. 监控统计分析
- **统计卡片**
  - 今日监控次数
  - 未处理告警数
  - 系统正常率
  - 本周监控总数

- **统计图表**
  - 监控类型分布（饼图）
  - 告警级别分布（饼图）
  - 监控趋势图（近24小时折线图）

- **快捷操作**
  - 快速跳转到各监控页面
  - 立即执行监控采集
  - 导出统计报表

## 技术实现

### 后端技术栈
- Spring Boot
- MyBatis（使用注解方式，无XML文件）
- OSHI（系统信息采集库）
- FastJSON2（JSON处理）

### 前端技术栈
- Vue 2
- Element UI
- ECharts 5（图表展示）
- Axios（HTTP请求）

### 数据库设计
- `proj_fz_system_monitor`：主监控记录表
- `proj_fz_server_metrics_history`：服务器历史指标表（可选）
- `proj_fz_database_metrics_history`：数据库历史指标表（可选）

## 部署步骤

### 1. 数据库初始化
执行SQL脚本：
```bash
E:\codes\idea workspace\Mobile-Classroom\sql\proj_fz_system_monitor.sql
```

### 2. 后端配置
确保pom.xml中包含必要的依赖（若依框架已包含）：
- oshi-core（系统信息采集）
- fastjson2（JSON处理）

### 3. 前端配置
安装ECharts（若未安装）：
```bash
cd ruoyi-ui
npm install echarts --save
```

### 4. 启动服务
```bash
# 后端
cd ruoyi-admin
mvn spring-boot:run

# 前端
cd ruoyi-ui
npm run dev
```

### 5. 访问系统
访问地址：http://localhost:80
菜单路径：系统监控 > 监控统计/服务器监控/数据库监控/监控记录

## 使用说明

### 监控统计页面
1. 查看整体监控概况和统计数据
2. 通过图表分析监控趋势
3. 使用快捷操作跳转到具体监控页面

### 服务器监控页面
1. 实时查看服务器各项指标
2. 选择时间范围查看历史趋势
3. 点击"刷新数据"手动更新（自动30秒刷新一次）

### 数据库监控页面
1. 实时查看数据库性能指标
2. 监控连接池使用情况
3. 分析慢查询趋势

### 监控记录页面
1. 使用搜索条件筛选记录
2. 查看详情时可导航到上一条/下一条
3. 对告警记录进行处理
4. 导出或打印监控报表

## 权限配置
模块使用的权限标识：
- `proj_fz:monitor:list` - 查看监控列表
- `proj_fz:monitor:query` - 查询监控详情
- `proj_fz:monitor:add` - 新增监控记录
- `proj_fz:monitor:edit` - 处理告警
- `proj_fz:monitor:remove` - 删除监控记录
- `proj_fz:monitor:export` - 导出监控数据
- `proj_fz:monitor:collect` - 手动采集数据

## 定时任务配置（可选）
可在若依系统的定时任务管理中添加监控采集任务：
- 任务名称：系统监控数据采集
- 调用目标：systemMonitorService.collectSystemMetrics
- cron表达式：`0 */5 * * * ?`（每5分钟执行一次）

## 注意事项
1. 监控数据会持续积累，建议定期清理历史数据
2. 告警阈值可在Service实现中根据实际情况调整
3. 网络速率目前为模拟数据，实际项目中需要实现真实采集
4. 建议生产环境配置独立的监控数据库

## 文件清单

### 后端文件
```
ruoyi-admin/src/main/java/com/ruoyi/proj_fz/
├── domain/
│   ├── SystemMonitor.java          # 监控记录实体
│   ├── ServerMetrics.java          # 服务器指标实体
│   └── DatabaseMetrics.java        # 数据库指标实体
├── mapper/
│   └── SystemMonitorMapper.java    # 数据访问层
├── service/
│   ├── ISystemMonitorService.java  # 服务接口
│   └── impl/
│       └── SystemMonitorServiceImpl.java  # 服务实现
└── controller/
    └── SystemMonitorController.java  # 控制器

ruoyi-admin/src/main/java/com/ruoyi/web/controller/proj_fz/
└── SystemMonitorController.java
```

### 前端文件
```
ruoyi-ui/src/
├── api/proj_fz/
│   └── systemMonitor.js           # API接口
└── views/proj_fz/systemMonitor/
    ├── statistics.vue             # 监控统计页面
    ├── server.vue                 # 服务器监控页面
    ├── database.vue               # 数据库监控页面
    └── record.vue                 # 监控记录页面
```

### 数据库文件
```
sql/
└── proj_fz_system_monitor.sql    # 建表SQL
```

### 路由配置
```
ruoyi-ui/src/router/index.js      # 添加了系统监控路由
```

## 扩展建议
1. 增加用户行为监控功能（登录日志、操作日志分析）
2. 实现接口性能监控（响应时间、调用次数统计）
3. 添加邮件/短信告警通知
4. 实现监控数据的自动归档和清理
5. 增加更多可配置的告警规则

## 技术支持
如有问题，请检查：
1. 数据库表是否正确创建
2. 依赖包是否正确安装
3. 权限是否正确配置
4. 后端日志是否有错误信息

