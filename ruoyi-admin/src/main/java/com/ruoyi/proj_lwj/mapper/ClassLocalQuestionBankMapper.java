package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassLocalQuestionBank;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassLocalQuestionBankMapper {

    @Select("<script>SELECT * FROM class_local_question_bank <where>" +
            "<if test=\"keyword != null and keyword.trim() != ''\"> AND question_content LIKE CONCAT('%',#{keyword},'%')</if>" +
            "<if test=\"questionType != null\"> AND question_type=#{questionType}</if>" +
            "<if test=\"difficulty != null\"> AND difficulty=#{difficulty}</if>" +
            "<if test=\"category != null and category.trim() != ''\"> AND category=#{category}</if>" +
            "<if test=\"status != null\"> AND status=#{status}</if>" +
            "</where> ORDER BY id DESC" +
            "<if test=\"limit != null\"> LIMIT #{limit}</if>" +
            "<if test=\"offset != null\"> OFFSET #{offset}</if></script>")
    List<ClassLocalQuestionBank> search(@Param("keyword") String keyword,
                                   @Param("questionType") Integer questionType,
                                   @Param("difficulty") Integer difficulty,
                                   @Param("category") String category,
                                   @Param("status") Integer status,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    @Select("SELECT * FROM class_local_question_bank WHERE id=#{id}")
    ClassLocalQuestionBank selectById(Long id);

    // 批量ID查询
    @Select("<script>SELECT * FROM class_local_question_bank WHERE id IN <foreach collection='list' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<ClassLocalQuestionBank> selectByIds(@Param("list") List<Long> ids);

    @Insert("INSERT INTO class_local_question_bank(question_content, question_type, difficulty, category, options_json, correct_answer, analysis, score, tags, status, created_time, updated_time, created_by) VALUES (#{questionContent}, #{questionType}, #{difficulty}, #{category}, #{optionsJson}, #{correctAnswer}, #{analysis}, #{score}, #{tags}, #{status}, NOW(), NOW(), #{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassLocalQuestionBank q);

    @Update("UPDATE class_local_question_bank SET question_content=#{questionContent}, question_type=#{questionType}, difficulty=#{difficulty}, category=#{category}, options_json=#{optionsJson}, correct_answer=#{correctAnswer}, analysis=#{analysis}, score=#{score}, tags=#{tags}, status=#{status}, updated_time=NOW(), update_by=#{updateBy} WHERE id=#{id}")
    int update(ClassLocalQuestionBank q);

    @Delete("DELETE FROM class_local_question_bank WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT DISTINCT category FROM class_local_question_bank WHERE status=1")
    List<String> selectCategories();

    @Select("SELECT COUNT(1) FROM class_local_question_bank WHERE status=1")
    int countEnabled();

    @Select("SELECT COUNT(1) FROM class_local_question_bank")
    int countAll();
}
