package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamMonitor;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassExamMonitorMapper {

    @Select("<script>SELECT * FROM class_exam_monitor <where>" +
            "<if test='examId != null'> AND exam_id = #{examId}</if>" +
            "<if test='studentId != null'> AND student_id = #{studentId}</if>" +
            "<if test='eventType != null'> AND event_type = #{eventType}</if>" +
            "</where> ORDER BY event_time DESC</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "eventType", column = "event_type"),
            @Result(property = "eventTime", column = "event_time"),
            @Result(property = "eventDetail", column = "event_detail"),
            @Result(property = "ipAddress", column = "ip_address"),
            @Result(property = "handled", column = "handled"),
            @Result(property = "createTime", column = "create_time")
    })
    List<ClassExamMonitor> selectList(ClassExamMonitor m);

    @Insert("INSERT INTO class_exam_monitor (exam_id, student_id, student_no, event_type, event_time, event_detail, ip_address, handled, create_time) VALUES (#{examId}, #{studentId}, #{studentNo}, #{eventType}, #{eventTime}, #{eventDetail}, #{ipAddress}, #{handled}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassExamMonitor m);

    @Update("UPDATE class_exam_monitor SET handled=#{handled} WHERE id=#{id}")
    int updateHandled(ClassExamMonitor m);

    @Delete("DELETE FROM class_exam_monitor WHERE id=#{id}")
    int deleteById(Long id);
}

