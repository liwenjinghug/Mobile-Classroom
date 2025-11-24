package com.ruoyi.proj_cyq.mapper;

import java.util.List;
import java.util.Map; // 【新增】引入 Map
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import com.ruoyi.proj_cyq.domain.ClassOperLog;

/**
 * 操作日志记录Mapper接口
 */
@Mapper
public interface ClassOperLogMapper {

    // ... (保留原有的基础 CURD 方法) ...

    @Select("SELECT oper_id, title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time FROM class_oper_log WHERE oper_id = #{operId}")
    ClassOperLog selectClassOperLogById(Long operId);

    @Select({
            "<script>",
            "SELECT oper_id, title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time FROM class_oper_log",
            "<where>",
            "<if test=\"title != null and title != ''\"> AND title like concat('%', #{title}, '%')</if>",
            "<if test=\"operName != null and operName != ''\"> AND oper_name like concat('%', #{operName}, '%')</if>",
            "<if test=\"businessType != null\"> AND business_type = #{businessType}</if>",
            "<if test=\"status != null\"> AND status = #{status}</if>",
            "</where>",
            "order by oper_id desc",
            "</script>"
    })
    List<ClassOperLog> selectClassOperLogList(ClassOperLog classOperLog);

    @Insert("INSERT INTO class_oper_log (title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time) VALUES (#{title}, #{businessType}, #{method}, #{requestMethod}, #{operatorType}, #{operName}, #{deptName}, #{operUrl}, #{operIp}, #{operLocation}, #{operParam}, #{jsonResult}, #{status}, #{errorMsg}, #{operTime})")
    int insertClassOperLog(ClassOperLog classOperLog);

    @Update("UPDATE class_oper_log SET title = #{title}, business_type = #{businessType}, method = #{method}, request_method = #{requestMethod}, operator_type = #{operatorType}, oper_name = #{operName}, dept_name = #{deptName}, oper_url = #{operUrl}, oper_ip = #{operIp}, oper_location = #{operLocation}, oper_param = #{operParam}, json_result = #{jsonResult}, status = #{status}, error_msg = #{errorMsg}, oper_time = #{operTime} WHERE oper_id = #{operId}")
    int updateClassOperLog(ClassOperLog classOperLog);

    @Delete("DELETE FROM class_oper_log WHERE oper_id = #{operId}")
    int deleteClassOperLogById(Long operId);

    @Delete({
            "<script>",
            "DELETE FROM class_oper_log WHERE oper_id IN",
            "<foreach collection='array' item='operId' open='(' separator=',' close=')'>",
            "#{operId}",
            "</foreach>",
            "</script>"
    })
    int deleteClassOperLogByIds(Long[] operIds);

    @Delete("TRUNCATE TABLE class_oper_log")
    int cleanClassOperLog();

    // ========== 【新增】统计查询方法 ==========

    // 1. 按模块标题统计
    @Select("SELECT title as name, COUNT(*) as value FROM class_oper_log GROUP BY title")
    List<Map<String, Object>> selectOperLogStatsByTitle();

    // 2. 按业务类型统计 (0=其它,1=新增,2=修改,3=删除...)
    @Select("SELECT business_type as name, COUNT(*) as value FROM class_oper_log GROUP BY business_type")
    List<Map<String, Object>> selectOperLogStatsByType();

    // 3. 按状态统计 (0=正常,1=异常)
    @Select("SELECT status as name, COUNT(*) as value FROM class_oper_log GROUP BY status")
    List<Map<String, Object>> selectOperLogStatsByStatus();
}