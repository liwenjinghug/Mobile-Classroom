package com.ruoyi.proj_lw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import com.ruoyi.proj_lw.domain.ClassStudentLw;

/**
 * 学生信息Mapper接口
 */
@Mapper
public interface ClassStudentLwMapper {
    /**
     * 查询学生信息
     */
    @Select("SELECT * FROM class_student WHERE student_id = #{studentId}")
    ClassStudentLw selectClassStudentByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询学生信息列表
     */
    @Select("SELECT * FROM class_student")
    List<ClassStudentLw> selectClassStudentList(ClassStudentLw classStudentLw);

    /**
     * 新增学生信息
     */
    @Insert("INSERT INTO class_student (student_no, student_name, gender, class_name, phone, email, status, user_id) " +
            "VALUES (#{studentNo}, #{studentName}, #{gender}, #{className}, #{phone}, #{email}, #{status}, #{userId})")
    int insertClassStudent(ClassStudentLw classStudentLw);

    /**
     * 修改学生信息
     */
    @Update("UPDATE class_student SET student_no = #{studentNo}, student_name = #{studentName}, " +
            "gender = #{gender}, class_name = #{className}, phone = #{phone}, email = #{email}, " +
            "status = #{status}, user_id = #{userId} WHERE student_id = #{studentId}")
    int updateClassStudent(ClassStudentLw classStudentLw);

    /**
     * 删除学生信息
     */
    @Delete("DELETE FROM class_student WHERE student_id = #{studentId}")
    int deleteClassStudentByStudentId(Long studentId);

    /**
     * 批量删除学生信息
     */
    @Delete({"<script>",
            "DELETE FROM class_student WHERE student_id IN ",
            "<foreach collection='studentIds' item='studentId' open='(' separator=',' close=')'>",
            "#{studentId}",
            "</foreach>",
            "</script>"})
    int deleteClassStudentByStudentIds(@Param("studentIds") Long[] studentIds);

    // 自定义方法

    /**
     * 根据学号查询学生
     */
    @Select("SELECT * FROM class_student WHERE student_no = #{studentNo}")
    ClassStudentLw selectClassStudentByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据用户ID查询学生
     */
    @Select("SELECT * FROM class_student WHERE user_id = #{userId}")
    ClassStudentLw selectClassStudentByUserId(@Param("userId") Long userId);

    /**
     * 根据学生姓名查询学生列表
     */
    @Select("SELECT * FROM class_student WHERE student_name LIKE CONCAT('%', #{studentName}, '%')")
    List<ClassStudentLw> selectClassStudentByName(@Param("studentName") String studentName);

    /**
     * 检查学号是否存在
     */
    @Select("SELECT COUNT(1) FROM class_student WHERE student_no = #{studentNo} AND (#{studentId} IS NULL OR student_id != #{studentId})")
    int checkStudentNoUnique(@Param("studentNo") String studentNo, @Param("studentId") Long studentId);

    /**
     * 根据关键词搜索学生（学号或姓名）
     */
    @Select("<script>" +
            "SELECT * FROM class_student " +
            "<where>" +
            "   <if test='keyword != null and keyword != \"\"'>" +
            "       (student_no LIKE CONCAT('%', #{keyword}, '%') OR student_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "   </if>" +
            "</where>" +
            "ORDER BY student_id ASC" +
            "</script>")
    List<ClassStudentLw> searchStudentsByKeyword(@Param("keyword") String keyword);
}