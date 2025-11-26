# 系统监控表名修改总结

## 修改日期
2025-11-26

## 修改内容
将所有系统监控相关的表名改为带 `class_` 前缀的格式。

## 修改的表名

### SQL 文件中的表（`sql/proj_fz_system_monitor.sql`）

1. **系统监控主表**
   - 原名：`proj_fz_system_monitor`
   - 新名：**`class_sys_monitor`**
   - 用途：存储所有监控告警记录

2. **服务器监控历史表**
   - 原名：`proj_fz_server_metrics_history`
   - 新名：**`class_server_metrics_history`**
   - 用途：存储服务器性能指标的历史趋势数据

3. **数据库监控历史表**
   - 原名：`proj_fz_database_metrics_history`
   - 新名：**`class_database_metrics_history`**
   - 用途：存储数据库性能指标的历史趋势数据

## 修改的代码文件

### SystemMonitorMapper.java
修改了所有 SQL 语句中的表名引用（10处）：

1. `selectSystemMonitorList()` - 查询监控列表
2. `selectSystemMonitorById()` - 根据ID查询
3. `insertSystemMonitor()` - 插入监控记录
4. `deleteSystemMonitorById()` - 删除单条记录
5. `deleteSystemMonitorByIds()` - 批量删除
6. `updateHandleStatus()` - 更新处理状态
7. `selectMonitorTypeStats()` - 监控类型统计
8. `selectAlertLevelStats()` - 告警级别统计
9. `selectMonitorTrend()` - 监控趋势
10. `selectUnhandledAlertCount()` - 未处理告警数

所有 SQL 中的表名都从 `proj_fz_system_monitor` 改为 **`class_sys_monitor`**

## 验证结果
✅ Maven 编译成功：BUILD SUCCESS
✅ 所有 239 个源文件编译通过
✅ 无编译错误

## 使用说明

### 1. 导入数据库表
将 `sql/proj_fz_system_monitor.sql` 中的 SQL 复制到 `ry-vue.sql` 中，然后执行以下表创建语句：

```sql
-- 主表（必需）
CREATE TABLE `class_sys_monitor` (...);

-- 历史表（可选）
CREATE TABLE `class_server_metrics_history` (...);
CREATE TABLE `class_database_metrics_history` (...);
```

### 2. 重启后端服务
代码已经更新为使用新表名，重启后端服务后即可正常使用。

### 3. 测试系统监控功能
访问前端系统监控页面，验证功能是否正常。

## 注意事项
- ✅ 代码中已全部使用新表名 `class_sys_monitor`
- ✅ 数据库需要使用新表名创建表
- ✅ 无需额外配置，直接使用即可
- ✅ 没有创建任何 XML 映射文件，全部使用注解

