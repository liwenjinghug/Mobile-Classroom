package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.RandomPickRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RandomPickMapper {

    @Insert("INSERT INTO class_random_pick(session_id, teacher_id, student_id, pick_time, round_no, remark) VALUES(#{sessionId}, #{teacherId}, #{studentId}, #{pickTime}, #{roundNo}, #{remark})")
    int insertRecord(RandomPickRecord record);

    @Select("SELECT student_id FROM class_random_pick WHERE session_id = #{sessionId}")
    List<Long> selectPickedStudentIdsBySession(Long sessionId);

    @Select("SELECT r.rpick_id AS rpickId, r.session_id AS sessionId, r.teacher_id AS teacherId, r.student_id AS studentId, r.pick_time AS pickTime, r.round_no AS roundNo, r.remark AS remark, s.student_name AS studentName, s.student_no AS studentNo FROM class_random_pick r LEFT JOIN class_student s ON r.student_id = s.student_id WHERE r.session_id = #{sessionId} ORDER BY r.pick_time DESC")
    List<RandomPickRecord> selectHistoryBySession(Long sessionId);
}
