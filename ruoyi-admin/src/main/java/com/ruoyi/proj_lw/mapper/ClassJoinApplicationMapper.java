package com.ruoyi.proj_lw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import com.ruoyi.proj_lw.domain.ClassJoinApplication;

/**
 * 课堂加入申请Mapper接口
 */
@Mapper
public interface ClassJoinApplicationMapper {
    /**
     * 查询课堂加入申请
     */
    @Select("SELECT * FROM class_join_application WHERE application_id = #{applicationId}")
    ClassJoinApplication selectClassJoinApplicationByApplicationId(Long applicationId);

    /**
     * 查询课堂加入申请列表
     */
    @Select("SELECT * FROM class_join_application")
    List<ClassJoinApplication> selectClassJoinApplicationList(ClassJoinApplication classJoinApplication);

    /**
     * 新增课堂加入申请
     */
    @Insert("INSERT INTO class_join_application (session_id, student_id, student_no, student_name, status, apply_time, remark) " +
            "VALUES (#{sessionId}, #{studentId}, #{studentNo}, #{studentName}, #{status}, #{applyTime}, #{remark})")
    int insertClassJoinApplication(ClassJoinApplication classJoinApplication);

    /**
     * 修改课堂加入申请
     */
    @Update("UPDATE class_join_application SET session_id = #{sessionId}, student_id = #{studentId}, " +
            "student_no = #{studentNo}, student_name = #{studentName}, status = #{status}, " +
            "apply_time = #{applyTime}, audit_time = #{auditTime}, audit_user_id = #{auditUserId}, " +
            "remark = #{remark} WHERE application_id = #{applicationId}")
    int updateClassJoinApplication(ClassJoinApplication classJoinApplication);

    /**
     * 删除课堂加入申请
     */
    @Delete("DELETE FROM class_join_application WHERE application_id = #{applicationId}")
    int deleteClassJoinApplicationByApplicationId(Long applicationId);

    /**
     * 批量删除课堂加入申请
     */
    @Delete({"<script>",
            "DELETE FROM class_join_application WHERE application_id IN ",
            "<foreach collection='applicationIds' item='applicationId' open='(' separator=',' close=')'>",
            "#{applicationId}",
            "</foreach>",
            "</script>"})
    int deleteClassJoinApplicationByApplicationIds(@Param("applicationIds") Long[] applicationIds);

    // 自定义方法

    /**
     * 学生：查询我的申请列表
     */
    @Select("SELECT a.*, s.class_name, s.teacher as teacher_name " +
            "FROM class_join_application a " +
            "LEFT JOIN class_session s ON a.session_id = s.session_id " +
            "WHERE a.student_id = #{studentId} " +
            "ORDER BY a.apply_time DESC")
    List<ClassJoinApplication> selectMyApplications(@Param("studentId") Long studentId);

    /**
     * 教师：查询待审核的申请列表
     */
    @Select("SELECT a.*, s.class_name, s.teacher as teacher_name " +
            "FROM class_join_application a " +
            "LEFT JOIN class_session s ON a.session_id = s.session_id " +
            "WHERE s.teacher_id = #{teacherId} AND a.status = '0' " +
            "ORDER BY a.apply_time DESC")
    List<ClassJoinApplication> selectPendingApplications(@Param("teacherId") Long teacherId);

    /**
     * 检查是否已申请
     */
    @Select("SELECT COUNT(1) FROM class_join_application " +
            "WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int checkApplicationExists(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 更新申请状态
     */
    @Update("UPDATE class_join_application SET status = #{status}, audit_time = NOW(), " +
            "audit_user_id = #{auditUserId}, remark = #{remark} " +
            "WHERE application_id = #{applicationId}")
    int updateApplicationStatus(@Param("applicationId") Long applicationId,
                                @Param("status") String status,
                                @Param("auditUserId") Long auditUserId,
                                @Param("remark") String remark);

    /**
     * 批量更新申请状态
     */
    @Update({"<script>",
            "UPDATE class_join_application SET status = #{status}, audit_time = NOW(), ",
            "audit_user_id = #{auditUserId} WHERE application_id IN ",
            "<foreach collection='applicationIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"})
    int batchUpdateApplicationStatus(@Param("applicationIds") List<Long> applicationIds,
                                     @Param("status") String status,
                                     @Param("auditUserId") Long auditUserId);
    /**
     * 根据课堂ID和学生ID删除申请记录
     */
    @Delete("DELETE FROM class_join_application WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int deleteApplicationBySessionAndStudent(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 根据教师用户名查询待审核申请
     */
    @Select("SELECT a.*, s.class_name, s.teacher as teacher_name " +
            "FROM class_join_application a " +
            "LEFT JOIN class_session s ON a.session_id = s.session_id " +
            "WHERE s.teacher = #{teacherUsername} AND a.status = '0' " +
            "ORDER BY a.apply_time DESC")
    List<ClassJoinApplication> selectPendingApplicationsByTeacher(@Param("teacherUsername") String teacherUsername);
}