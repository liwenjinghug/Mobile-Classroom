package com.ruoyi.proj_fz.mapper;

import com.ruoyi.proj_fz.domain.SystemMonitor;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 系统监控Mapper接口
 *
 * @author proj_fz
 */
@Mapper
public interface SystemMonitorMapper {

    /**
     * 查询系统监控列表
     */
    @Select("<script>" +
            "SELECT monitor_id, monitor_type, monitor_name, metrics, status, alert_level, " +
            "alert_desc, handled, handler, handle_time, monitor_time, handle_remark " +
            "FROM class_sys_monitor " +
            "<where>" +
            "<if test='monitorType != null'> AND monitor_type = #{monitorType}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='alertLevel != null'> AND alert_level = #{alertLevel}</if>" +
            "<if test='handled != null'> AND handled = #{handled}</if>" +
            "<if test='params.beginTime != null and params.beginTime != \"\"'> AND monitor_time &gt;= #{params.beginTime}</if>" +
            "<if test='params.endTime != null and params.endTime != \"\"'> AND monitor_time &lt;= #{params.endTime}</if>" +
            "</where>" +
            " ORDER BY monitor_time DESC" +
            "</script>")
    List<SystemMonitor> selectSystemMonitorList(SystemMonitor systemMonitor);

    /**
     * 查询系统监控详情
     */
    @Select("SELECT monitor_id, monitor_type, monitor_name, metrics, status, alert_level, " +
            "alert_desc, handled, handler, handle_time, monitor_time, handle_remark " +
            "FROM class_sys_monitor WHERE monitor_id = #{monitorId}")
    SystemMonitor selectSystemMonitorById(Long monitorId);

    /**
     * 新增系统监控
     */
    @Insert("INSERT INTO class_sys_monitor(monitor_type, monitor_name, metrics, status, " +
            "alert_level, alert_desc, handled, monitor_time, handle_remark) " +
            "VALUES (#{monitorType}, #{monitorName}, #{metrics}, #{status}, #{alertLevel}, " +
            "#{alertDesc}, #{handled}, #{monitorTime}, #{handleRemark})")
    @Options(useGeneratedKeys = true, keyProperty = "monitorId")
    int insertSystemMonitor(SystemMonitor systemMonitor);

    /**
     * 删除系统监控
     */
    @Delete("DELETE FROM class_sys_monitor WHERE monitor_id = #{monitorId}")
    int deleteSystemMonitorById(Long monitorId);

    /**
     * 批量删除系统监控
     */
    @Delete("<script>" +
            "DELETE FROM class_sys_monitor WHERE monitor_id IN " +
            "<foreach item='monitorId' collection='array' open='(' separator=',' close=')'>" +
            "#{monitorId}" +
            "</foreach>" +
            "</script>")
    int deleteSystemMonitorByIds(Long[] monitorIds);

    /**
     * 更新处理状态
     */
    @Update("UPDATE class_sys_monitor SET handled = #{handled}, handler = #{handler}, " +
            "handle_time = #{handleTime}, handle_remark = #{handleRemark} WHERE monitor_id = #{monitorId}")
    int updateHandleStatus(SystemMonitor systemMonitor);

    /**
     * 监控类型统计
     */
    @Select("SELECT monitor_type AS type, COUNT(*) AS count " +
            "FROM class_sys_monitor " +
            "WHERE monitor_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY monitor_type")
    List<Map<String, Object>> selectMonitorTypeStats(Integer days);

    /**
     * 告警级别统计
     */
    @Select("SELECT alert_level AS level, COUNT(*) AS count " +
            "FROM class_sys_monitor " +
            "WHERE status = 1 AND monitor_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY alert_level")
    List<Map<String, Object>> selectAlertLevelStats(Integer days);

    /**
     * 监控趋势（最近N小时）
     */
    @Select("SELECT DATE_FORMAT(monitor_time, '%Y-%m-%d %H:00:00') AS time, " +
            "COUNT(*) AS total, SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS alertCount " +
            "FROM class_sys_monitor " +
            "WHERE monitor_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "GROUP BY DATE_FORMAT(monitor_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY time")
    List<Map<String, Object>> selectMonitorTrend(int hours);

    /**
     * 未处理告警数
     */
    @Select("SELECT COUNT(*) FROM class_sys_monitor WHERE status = 1 AND handled = 0")
    int selectUnhandledAlertCount();
}

