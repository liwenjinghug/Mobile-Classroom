package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassStudentHomeworkMapper {

    // Return the latest submission per student for the given homework,
    // but include ALL students that belong to the same session (show unsubmitted students too).
    @Select("SELECT " +
            "  csh.id, " +
            "  COALESCE(csh.homework_id, #{homeworkId}) AS homework_id, " +
            "  s.student_id AS student_id, " +
            "  s.student_no AS student_no, " +
            "  COALESCE(s.student_name, su.nick_name) AS student_name, " +
            "  csh.submission_files, " +
            "  csh.submit_time, " +
            "  csh.grade, " +
            "  csh.grade_comment, " +
            "  COALESCE(csh.status, '0') AS status, " +
            "  ch.title AS homework_title, " +
            "  csh.is_graded, csh.corrected_by, csh.corrected_time, csh.grade_attachment, csh.word_count " +
            "FROM class_session_student css " +
            "JOIN class_student s ON css.student_id = s.student_id " +
            "LEFT JOIN sys_user su ON s.student_id = su.user_id " +
            "JOIN class_homework ch ON ch.homework_id = #{homeworkId} AND ch.session_id = css.session_id " +
            "LEFT JOIN (SELECT student_id, MAX(id) AS max_id FROM class_student_homework WHERE homework_id = #{homeworkId} GROUP BY student_id) t ON t.student_id = s.student_id " +
            "LEFT JOIN class_student_homework csh ON csh.id = t.max_id " +
            "ORDER BY s.student_name ASC")
    @Results({
            @Result(property = "studentHomeworkId", column = "id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            // DB column is `grade` (decimal) -> map to property `score`
            @Result(property = "score", column = "grade"),
            // DB column is `grade_comment` -> map to property `remark`
            @Result(property = "remark", column = "grade_comment"),
            @Result(property = "status", column = "status"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "homeworkTitle", column = "homework_title"),
            // new columns
            @Result(property = "isGraded", column = "is_graded"),
            @Result(property = "correctedBy", column = "corrected_by"),
            @Result(property = "correctedTime", column = "corrected_time"),
            @Result(property = "gradeAttachment", column = "grade_attachment"),
            @Result(property = "wordCount", column = "word_count")
    })
    List<ClassStudentHomework> selectByHomeworkId(Long homeworkId);

    @Select("SELECT csh.*, COALESCE(csh.student_name, su.nick_name) AS student_name, ch.title AS homework_title, s.student_no AS student_no FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id LEFT JOIN class_student s ON csh.student_id = s.student_id LEFT JOIN class_homework ch ON csh.homework_id = ch.homework_id WHERE csh.id = #{id}")
    @Results({
            @Result(property = "studentHomeworkId", column = "id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "score", column = "grade"),
            @Result(property = "remark", column = "grade_comment"),
            @Result(property = "status", column = "status"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "homeworkTitle", column = "homework_title"),
            // new columns
            @Result(property = "isGraded", column = "is_graded"),
            @Result(property = "correctedBy", column = "corrected_by"),
            @Result(property = "correctedTime", column = "corrected_time"),
            @Result(property = "gradeAttachment", column = "grade_attachment"),
            @Result(property = "wordCount", column = "word_count")
    })
    ClassStudentHomework selectById(Long id);

    @Select("SELECT csh.*, COALESCE(csh.student_name, su.nick_name) AS student_name, ch.title AS homework_title, s.student_no AS student_no FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id LEFT JOIN class_student s ON csh.student_id = s.student_id LEFT JOIN class_homework ch ON csh.homework_id = ch.homework_id WHERE csh.student_id = #{studentId} ORDER BY csh.submit_time DESC")
    @Results({
            @Result(property = "studentHomeworkId", column = "id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "score", column = "grade"),
            @Result(property = "remark", column = "grade_comment"),
            @Result(property = "status", column = "status"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "homeworkTitle", column = "homework_title"),
            // new columns
            @Result(property = "isGraded", column = "is_graded"),
            @Result(property = "correctedBy", column = "corrected_by"),
            @Result(property = "correctedTime", column = "corrected_time"),
            @Result(property = "gradeAttachment", column = "grade_attachment"),
            @Result(property = "wordCount", column = "word_count")
    })
    List<ClassStudentHomework> selectByStudentId(Long studentId);

    @Select("SELECT csh.*, COALESCE(csh.student_name, su.nick_name) AS student_name, ch.title AS homework_title, s.student_no AS student_no FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id LEFT JOIN class_student s ON csh.student_id = s.student_id LEFT JOIN class_homework ch ON csh.homework_id = ch.homework_id WHERE csh.student_no = #{studentNo} ORDER BY csh.submit_time DESC")
    @Results({
            @Result(property = "studentHomeworkId", column = "id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "score", column = "grade"),
            @Result(property = "remark", column = "grade_comment"),
            @Result(property = "status", column = "status"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "homeworkTitle", column = "homework_title"),
            // new columns
            @Result(property = "isGraded", column = "is_graded"),
            @Result(property = "correctedBy", column = "corrected_by"),
            @Result(property = "correctedTime", column = "corrected_time"),
            @Result(property = "gradeAttachment", column = "grade_attachment"),
            @Result(property = "wordCount", column = "word_count")
    })
    List<ClassStudentHomework> selectByStudentNo(String studentNo);

    @Select("SELECT csh.*, COALESCE(csh.student_name, su.nick_name) AS student_name, ch.title AS homework_title, s.student_no AS student_no FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id LEFT JOIN class_student s ON csh.student_id = s.student_id LEFT JOIN class_homework ch ON csh.homework_id = ch.homework_id WHERE (csh.student_no = #{ident} OR csh.student_name = #{ident}) ORDER BY csh.submit_time DESC")
    @Results({
            @Result(property = "studentHomeworkId", column = "id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "score", column = "grade"),
            @Result(property = "remark", column = "grade_comment"),
            @Result(property = "status", column = "status"),
            @Result(property = "studentName", column = "student_name"),
            @Result(property = "homeworkTitle", column = "homework_title"),
            @Result(property = "isGraded", column = "is_graded"),
            @Result(property = "correctedBy", column = "corrected_by"),
            @Result(property = "correctedTime", column = "corrected_time"),
            @Result(property = "gradeAttachment", column = "grade_attachment"),
            @Result(property = "wordCount", column = "word_count")
    })
    List<ClassStudentHomework> selectByStudentIdentifier(String ident);

    @Insert("INSERT INTO class_student_homework (homework_id, student_id, student_no, student_name, submission_files, submit_time, status, create_by, create_time) " +
            "VALUES (#{homeworkId}, #{studentId}, #{studentNo}, #{studentName}, #{submissionFiles}, #{submitTime}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "studentHomeworkId")
    int insert(ClassStudentHomework shw);

    // use actual DB column names `grade` and `grade_comment`
    @Update("UPDATE class_student_homework SET student_id=#{studentId}, student_no=#{studentNo}, student_name=#{studentName}, submission_files=#{submissionFiles}, submit_time=#{submitTime}, status=#{status}, grade=#{score}, grade_comment=#{remark}, is_graded=#{isGraded}, corrected_by=#{correctedBy}, corrected_time=#{correctedTime}, grade_attachment=#{gradeAttachment}, word_count=#{wordCount}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{studentHomeworkId}")
    int update(ClassStudentHomework shw);

    @Delete("DELETE FROM class_student_homework WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("<script>DELETE FROM class_student_homework WHERE homework_id IN <foreach item='id' collection='array' open='(' separator=',' close=')'> #{id} </foreach></script>")
    int deleteByHomeworkIds(Long[] homeworkIds);
}
