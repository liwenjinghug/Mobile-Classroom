package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamParticipant;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassExamParticipantMapper {

    @Select("<script>SELECT * FROM class_exam_participant <where>" +
            "<if test='examId != null'> AND exam_id = #{examId}</if>" +
            "<if test='studentId != null'> AND student_id = #{studentId}</if>" +
            "<if test='participantStatus != null'> AND participant_status = #{participantStatus}</if>" +
            "</where> ORDER BY id DESC</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "participantStatus", column = "participant_status"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "ipAddress", column = "ip_address"),
            @Result(property = "deviceInfo", column = "device_info"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "timeUsed", column = "time_used"),
            @Result(property = "objectiveScore", column = "objective_score"),
            @Result(property = "subjectiveScore", column = "subjective_score"),
            @Result(property = "correctStatus", column = "correct_status"),
            @Result(property = "passStatus", column = "pass_status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<ClassExamParticipant> selectList(ClassExamParticipant p);

    @Select("SELECT * FROM class_exam_participant WHERE id=#{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "participantStatus", column = "participant_status"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "ipAddress", column = "ip_address"),
            @Result(property = "deviceInfo", column = "device_info"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "timeUsed", column = "time_used"),
            @Result(property = "objectiveScore", column = "objective_score"),
            @Result(property = "subjectiveScore", column = "subjective_score"),
            @Result(property = "correctStatus", column = "correct_status"),
            @Result(property = "passStatus", column = "pass_status")
    })
    ClassExamParticipant selectById(Long id);

    @Select("SELECT * FROM class_exam_participant WHERE exam_id=#{examId} AND student_id=#{studentId} LIMIT 1")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "participantStatus", column = "participant_status"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "ipAddress", column = "ip_address"),
            @Result(property = "deviceInfo", column = "device_info"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "timeUsed", column = "time_used"),
            @Result(property = "objectiveScore", column = "objective_score"),
            @Result(property = "subjectiveScore", column = "subjective_score"),
            @Result(property = "correctStatus", column = "correct_status"),
            @Result(property = "passStatus", column = "pass_status")
    })
    ClassExamParticipant selectByExamStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Insert("INSERT INTO class_exam_participant (exam_id, student_id, student_no, student_name, participant_status, start_time, submit_time, ip_address, device_info, total_score, time_used, objective_score, subjective_score, correct_status, pass_status, create_time) VALUES (#{examId}, #{studentId}, #{studentNo}, #{studentName}, #{participantStatus}, #{startTime}, #{submitTime}, #{ipAddress}, #{deviceInfo}, #{totalScore}, #{timeUsed}, #{objectiveScore}, #{subjectiveScore}, #{correctStatus}, #{passStatus}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassExamParticipant p);

    @Update("UPDATE class_exam_participant SET participant_status=#{participantStatus}, start_time=#{startTime}, submit_time=#{submitTime}, ip_address=#{ipAddress}, device_info=#{deviceInfo}, total_score=#{totalScore}, time_used=#{timeUsed}, objective_score=#{objectiveScore}, subjective_score=#{subjectiveScore}, correct_status=#{correctStatus}, pass_status=#{passStatus}, update_time=NOW() WHERE id=#{id}")
    int update(ClassExamParticipant p);

    @Delete("DELETE FROM class_exam_participant WHERE id=#{id}")
    int deleteById(Long id);

    @Delete("<script>DELETE FROM class_exam_participant WHERE id IN <foreach item='i' collection='array' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int deleteByIds(Long[] ids);
}

