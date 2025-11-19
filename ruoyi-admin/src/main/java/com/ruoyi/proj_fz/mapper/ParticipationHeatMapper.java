package com.ruoyi.proj_fz.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ParticipationHeatMapper
{
    @Select("SELECT " +
            "    s.student_id, " +
            "    s.student_no, " +
            "    s.student_name, " +
            "    COALESCE(ROUND( " +
            "        (SELECT COUNT(*) FROM class_attendance ca  " +
            "         WHERE ca.student_id = s.student_id AND ca.attendance_status IN (1,2,4)) * 100.0 / " +
            "        GREATEST((SELECT COUNT(*) FROM class_attendance_task cat  " +
            "               WHERE cat.session_id IN (SELECT session_id FROM class_session_student WHERE student_id = s.student_id)), 1) " +
            "    , 0), 0) as attendance_rate, " +
            "    COALESCE(ROUND( " +
            "        (SELECT COUNT(*) FROM class_student_homework csh  " +
            "         WHERE csh.student_id = s.student_id AND csh.status IN ('1','2','3')) * 100.0 / " +
            "        GREATEST((SELECT COUNT(*) FROM class_homework ch  " +
            "               WHERE ch.session_id IN (SELECT session_id FROM class_session_student WHERE student_id = s.student_id)), 1) " +
            "    , 0), 0) as homework_rate, " +
            "    COALESCE( " +
            "        (SELECT COUNT(*) FROM class_forum_post cfp WHERE cfp.user_id = s.student_id AND cfp.del_flag = '0') * 5 + " +
            "        (SELECT COUNT(*) FROM class_forum_comment cfc WHERE cfc.user_id = s.student_id AND cfc.del_flag = '0') * 2 + " +
            "        (SELECT COUNT(*) FROM class_forum_like cfl WHERE cfl.user_id = s.student_id AND cfl.del_flag = '0') " +
            "    , 0) as forum_score " +
            "FROM class_student s " +
            "WHERE s.status = 1 " +
            "ORDER BY attendance_rate DESC, homework_rate DESC, forum_score DESC")
    List<Map<String, Object>> selectParticipationHeatData();

    @Select("SELECT " +
            "    ROUND(AVG(attendance.attendance_rate), 1) as avg_attendance_rate, " +
            "    ROUND(AVG(homework.homework_rate), 1) as avg_homework_rate, " +
            "    MAX(attendance.attendance_rate) as max_attendance_rate, " +
            "    MAX(homework.homework_rate) as max_homework_rate, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_attendance WHERE attendance_status IN (1,2,4)) as active_students, " +
            "    (SELECT COUNT(*) FROM class_forum_post WHERE del_flag = '0') as total_posts, " +
            "    (SELECT COUNT(*) FROM class_forum_comment WHERE del_flag = '0') as total_comments, " +
            "    (SELECT COUNT(*) FROM class_forum_like WHERE del_flag = '0') as total_likes " +
            "FROM ( " +
            "    SELECT  " +
            "        student_id, " +
            "        ROUND( " +
            "            COUNT(CASE WHEN attendance_status IN (1,2,4) THEN 1 END) * 100.0 / " +
            "            GREATEST(COUNT(*), 1) " +
            "        , 1) as attendance_rate " +
            "    FROM class_attendance  " +
            "    GROUP BY student_id " +
            ") attendance " +
            "LEFT JOIN ( " +
            "    SELECT  " +
            "        student_id, " +
            "        ROUND( " +
            "            COUNT(CASE WHEN status IN ('1','2','3') THEN 1 END) * 100.0 / " +
            "            GREATEST(COUNT(*), 1) " +
            "        , 1) as homework_rate " +
            "    FROM class_student_homework  " +
            "    GROUP BY student_id " +
            ") homework ON attendance.student_id = homework.student_id")
    Map<String, Object> selectParticipationStats();
}