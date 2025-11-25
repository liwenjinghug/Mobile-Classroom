package com.ruoyi.proj_cyq.mapper;

import java.util.List;
import java.util.Map; // 引入 Map
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;

/**
 * 系统登录日志Mapper接口
 */
@Mapper
public interface ClassLoginLogMapper {

    /**
     * 查询系统登录日志
     */
    @Select("SELECT login_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time FROM class_login_log WHERE login_id = #{loginId}")
    ClassLoginLog selectClassLoginLogById(Long loginId);

    /**
     * 查询系统登录日志列表
     */
    @Select({
            "<script>",
            "SELECT login_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time FROM class_login_log",
            "<where>",
            "<if test=\"userName != null and userName != ''\"> AND user_name like concat('%', #{userName}, '%')</if>",
            "<if test=\"ipaddr != null and ipaddr != ''\"> AND ipaddr like concat('%', #{ipaddr}, '%')</if>",
            "<if test=\"status != null\"> AND status = #{status}</if>",
            "<if test=\"msg != null and msg != ''\"> AND msg like concat('%', #{msg}, '%')</if>",
            "</where>",
            "order by login_id desc",
            "</script>"
    })
    List<ClassLoginLog> selectClassLoginLogList(ClassLoginLog classLoginLog);

    /**
     * 新增系统登录日志
     */
    @Insert("INSERT INTO class_login_log (user_name, ipaddr, login_location, browser, os, status, msg, login_time) VALUES (#{userName}, #{ipaddr}, #{loginLocation}, #{browser}, #{os}, #{status}, #{msg}, #{loginTime})")
    int insertClassLoginLog(ClassLoginLog classLoginLog);

    /**
     * 修改系统登录日志
     */
    @Update("UPDATE class_login_log SET user_name = #{userName}, ipaddr = #{ipaddr}, login_location = #{loginLocation}, browser = #{browser}, os = #{os}, status = #{status}, msg = #{msg}, login_time = #{loginTime} WHERE login_id = #{loginId}")
    int updateClassLoginLog(ClassLoginLog classLoginLog);

    /**
     * 删除系统登录日志
     */
    @Delete("DELETE FROM class_login_log WHERE login_id = #{loginId}")
    int deleteClassLoginLogById(Long loginId);

    /**
     * 批量删除系统登录日志
     */
    @Delete({
            "<script>",
            "DELETE FROM class_login_log WHERE login_id IN",
            "<foreach collection='array' item='loginId' open='(' separator=',' close=')'>",
            "#{loginId}",
            "</foreach>",
            "</script>"
    })
    int deleteClassLoginLogByIds(Long[] loginIds);

    /**
     * 清空登录日志
     */
    @Delete("TRUNCATE TABLE class_login_log")
    int cleanClassLoginLog();

    // ========== 统计查询方法 ==========

    // 1. 按登录状态统计 (0=成功, 1=失败)
    @Select("SELECT status as name, COUNT(*) as value FROM class_login_log GROUP BY status")
    List<Map<String, Object>> selectLoginLogStatsByStatus();

    // 2. 按浏览器统计
    @Select("SELECT browser as name, COUNT(*) as value FROM class_login_log GROUP BY browser")
    List<Map<String, Object>> selectLoginLogStatsByBrowser();

    // 3. 按操作系统统计
    @Select("SELECT os as name, COUNT(*) as value FROM class_login_log GROUP BY os")
    List<Map<String, Object>> selectLoginLogStatsByOs();
}