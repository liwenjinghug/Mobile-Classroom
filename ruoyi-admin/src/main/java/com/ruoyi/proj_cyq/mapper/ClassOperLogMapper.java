package com.ruoyi.proj_cyq.mapper;

import java.util.List;
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

    /**
     * 查询操作日志记录
     */
    @Select("SELECT oper_id, title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time FROM class_oper_log WHERE oper_id = #{operId}")
    ClassOperLog selectClassOperLogById(Long operId);

    /**
     * 查询操作日志记录列表
     */
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

    /**
     * 新增操作日志记录
     */
    @Insert("INSERT INTO class_oper_log (title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time) VALUES (#{title}, #{businessType}, #{method}, #{requestMethod}, #{operatorType}, #{operName}, #{deptName}, #{operUrl}, #{operIp}, #{operLocation}, #{operParam}, #{jsonResult}, #{status}, #{errorMsg}, #{operTime})")
    int insertClassOperLog(ClassOperLog classOperLog);

    /**
     * 修改操作日志记录
     */
    @Update("UPDATE class_oper_log SET title = #{title}, business_type = #{businessType}, method = #{method}, request_method = #{requestMethod}, operator_type = #{operatorType}, oper_name = #{operName}, dept_name = #{deptName}, oper_url = #{operUrl}, oper_ip = #{operIp}, oper_location = #{operLocation}, oper_param = #{operParam}, json_result = #{jsonResult}, status = #{status}, error_msg = #{errorMsg}, oper_time = #{operTime} WHERE oper_id = #{operId}")
    int updateClassOperLog(ClassOperLog classOperLog);

    /**
     * 删除操作日志记录
     */
    @Delete("DELETE FROM class_oper_log WHERE oper_id = #{operId}")
    int deleteClassOperLogById(Long operId);

    /**
     * 批量删除操作日志记录
     */
    @Delete({
            "<script>",
            "DELETE FROM class_oper_log WHERE oper_id IN",
            "<foreach collection='array' item='operId' open='(' separator=',' close=')'>",
            "#{operId}",
            "</foreach>",
            "</script>"
    })
    int deleteClassOperLogByIds(Long[] operIds);

    /**
     * 清空操作日志
     */
    @Delete("TRUNCATE TABLE class_oper_log")
    int cleanClassOperLog();
}