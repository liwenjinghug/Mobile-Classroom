# 系统监控数据库字段错误修复

## 问题描述
前端报错：
```
Unknown column 'remark' in 'field list'
```

## 根本原因
数据库表 `proj_fz_system_monitor` 中的字段名是 `handle_remark`，但 Mapper 中使用的是 `remark`，导致字段名不匹配。

## 修复内容

### 修复的文件
`E:\codes\idea workspace\Mobile-Classroom\ruoyi-admin\src\main\java\com\ruoyi\proj_fz\mapper\SystemMonitorMapper.java`

### 修改详情
将以下 SQL 语句中的 `remark` 字段全部改为 `handle_remark`：

1. **selectSystemMonitorList** - 查询监控列表
   - 修改前: `... monitor_time, remark FROM ...`
   - 修改后: `... monitor_time, handle_remark FROM ...`

2. **selectSystemMonitorById** - 根据ID查询
   - 修改前: `... monitor_time, remark FROM ...`
   - 修改后: `... monitor_time, handle_remark FROM ...`

3. **insertSystemMonitor** - 插入监控记录
   - 修改前: `INSERT INTO ... (... remark) VALUES (... #{remark})`
   - 修改后: `INSERT INTO ... (... handle_remark) VALUES (... #{handleRemark})`

## 验证结果
✅ Maven 编译成功：BUILD SUCCESS
✅ 所有 239 个源文件编译通过
✅ 无编译错误

## 数据库表结构（供参考）
```sql
CREATE TABLE `proj_fz_system_monitor` (
  `monitor_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `monitor_type` int(11) NOT NULL,
  `monitor_name` varchar(100) NOT NULL,
  `metrics` text,
  `alert_level` int(11) DEFAULT 0,
  `alert_desc` varchar(500) DEFAULT NULL,
  `status` int(11) DEFAULT 0,
  `monitor_time` datetime NOT NULL,
  `handled` int(11) DEFAULT 0,
  `handler` varchar(50) DEFAULT NULL,
  `handle_time` datetime DEFAULT NULL,
  `handle_remark` varchar(500) DEFAULT NULL,  -- 注意：是 handle_remark 不是 remark
  PRIMARY KEY (`monitor_id`)
);
```

## 注意事项
- 实体类 `SystemMonitor.java` 已经使用正确的字段名 `handleRemark`
- 无需修改实体类，只需修改 Mapper 中的 SQL 语句
- 重启后端服务后，系统监控功能应该可以正常使用

## 下一步
重启后端应用，刷新前端页面，系统监控模块应该可以正常加载数据了。

