- [ ] 刷新数据功能正常
- [ ] 时间范围切换正常

### 3.3 监控记录页面测试
访问路径：系统监控 > 监控记录

测试项：
- [ ] 页面正常加载
- [ ] 列表数据显示正常
- [ ] 监控类型筛选功能正常
- [ ] 监控项名称搜索功能正常
- [ ] 告警级别筛选功能正常
- [ ] 状态筛选功能正常
- [ ] 处理状态筛选功能正常
- [ ] 时间段筛选功能正常
- [ ] 查看详情功能正常
- [ ] 首条/上条/下条/末尾导航功能正常
- [ ] 打印功能正常
- [ ] 处理告警功能正常
- [ ] 删除功能正常
- [ ] 批量删除功能正常
- [ ] 导出Excel功能正常
- [ ] 立即采集功能正常
- [ ] 分页功能正常

### 3.4 监控统计页面测试
访问路径：系统监控 > 监控统计

测试项：
- [ ] 页面正常加载
- [ ] 今日监控次数统计正常
- [ ] 未处理告警数统计正常
- [ ] 系统正常率统计正常
- [ ] 本周监控总数统计正常
- [ ] 监控类型分布饼图显示正常
- [ ] 告警级别分布饼图显示正常
- [ ] 监控趋势折线图显示正常
- [ ] 时间范围切换（近7天/近30天）正常
- [ ] 快捷操作按钮功能正常
- [ ] 立即采集功能正常
- [ ] 导出报表功能正常

## 四、API接口测试

### 4.1 服务器监控接口
```bash
# 获取实时服务器指标
curl http://localhost:8080/proj_fz/monitor/server/realtime

# 获取服务器历史指标
curl http://localhost:8080/proj_fz/monitor/server/history?hours=24
```

### 4.2 数据库监控接口
```bash
# 获取实时数据库指标
curl http://localhost:8080/proj_fz/monitor/database/realtime

# 获取数据库历史指标
curl http://localhost:8080/proj_fz/monitor/database/history?hours=24
```

### 4.3 监控记录接口
```bash
# 查询监控列表
curl http://localhost:8080/proj_fz/monitor/list?pageNum=1&pageSize=10

# 查询监控详情
curl http://localhost:8080/proj_fz/monitor/1

# 获取统计数据
curl http://localhost:8080/proj_fz/monitor/statistics?days=7

# 获取未处理告警数量
curl http://localhost:8080/proj_fz/monitor/alert/count
```

## 五、常见问题排查

### 问题1：页面无法访问
检查项：
- 路由配置是否正确
- 前端服务是否正常启动
- 浏览器控制台是否有错误

### 问题2：数据无法加载
检查项：
- 后端服务是否正常启动
- 数据库表是否正确创建
- 网络请求是否成功（F12查看Network）
- 后端日志是否有错误

### 问题3：图表不显示
检查项：
- ECharts是否正确安装（npm list echarts）
- 浏览器控制台是否有ECharts相关错误
- 数据格式是否正确

### 问题4：权限不足
检查项：
- 用户是否有相应权限
- 权限标识是否正确配置
- 角色是否分配了对应权限

### 问题5：Mapper找不到
检查项：
- @Mapper注解是否添加
- 包扫描路径是否正确
- IDE缓存是否需要刷新（File > Invalidate Caches）

## 六、性能优化建议

1. **定期清理历史数据**
```sql
-- 删除30天前的监控记录
DELETE FROM proj_fz_system_monitor 
WHERE monitor_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

2. **添加索引优化查询**
已在建表语句中添加以下索引：
- idx_monitor_type
- idx_monitor_time
- idx_alert_level
- idx_handled

3. **配置定时任务**
建议配置定时采集任务，间隔5-10分钟

4. **启用数据归档**
对于重要监控数据，可定期归档到历史表

## 七、权限配置

在若依系统管理 > 菜单管理中添加以下权限：

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| proj_fz:monitor:list | 查看监控列表 | 基础查询权限 |
| proj_fz:monitor:query | 查询监控详情 | 详情查看权限 |
| proj_fz:monitor:add | 新增监控记录 | 添加记录权限 |
| proj_fz:monitor:edit | 处理告警 | 告警处理权限 |
| proj_fz:monitor:remove | 删除监控记录 | 删除权限 |
| proj_fz:monitor:export | 导出监控数据 | 导出权限 |
| proj_fz:monitor:collect | 手动采集数据 | 采集权限 |

## 八、部署完成确认

完成以下所有步骤后，系统监控模块即可正常使用：

- [ ] 数据库脚本执行成功
- [ ] 后端服务启动无错误
- [ ] 前端服务启动无错误
- [ ] 能够访问所有监控页面
- [ ] 所有图表正常显示
- [ ] 数据能够正常加载
- [ ] 增删改查功能正常
- [ ] 导出和打印功能正常

## 九、联系支持

如遇到问题，请：
1. 查看后端日志文件
2. 查看浏览器控制台错误
3. 检查数据库连接状态
4. 参考README_MONITOR.md文档

