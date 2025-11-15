package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.AttendanceTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface AttendanceTaskMapper {

    @Insert("INSERT INTO class_attendance_task(session_id, type, center_lat, center_lng, radius, qr_code, start_time, end_time, status, create_by, create_time) VALUES(#{sessionId}, #{type}, #{centerLat}, #{centerLng}, #{radius}, #{qrCode}, #{startTime}, #{endTime}, IFNULL(#{status},1), IFNULL(#{createBy},'system'), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "taskId", keyColumn = "task_id")
    int insertTask(AttendanceTask task);

    @Select("SELECT * FROM class_attendance_task WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<AttendanceTask> selectBySession(Long sessionId);

    @Select("SELECT * FROM class_attendance_task WHERE task_id = #{taskId}")
    AttendanceTask selectById(Long taskId);

    @Update("UPDATE class_attendance_task SET status = #{status}, end_time = #{endTime} WHERE task_id = #{taskId}")
    int updateStatus(AttendanceTask task);

    @Select("SELECT * FROM class_attendance_task WHERE session_id = #{sessionId} ORDER BY create_time DESC LIMIT 1")
    AttendanceTask selectLatestTaskBySession(Long sessionId);

    @Delete("DELETE FROM class_attendance_task WHERE task_id = #{taskId}")
    int deleteById(Long taskId);
}
