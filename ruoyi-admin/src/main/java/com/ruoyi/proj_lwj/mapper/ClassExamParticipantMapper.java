package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ClassExamParticipantMapper {
    /**
     * 列表查询（使用 XML 中的 <select id="selectList"> 映射）
     */
    List<ClassExamParticipant> selectList(ClassExamParticipant p);

    /** 根据主键查询 */
    ClassExamParticipant selectById(Long id);

    /** 根据考试与学生查询单条 */
    ClassExamParticipant selectByExamStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);

    /** 插入记录 */
    int insert(ClassExamParticipant p);

    /** 更新记录 */
    int update(ClassExamParticipant p);

    /** 删除单条 */
    int deleteById(Long id);

    /** 批量删除 */
    int deleteByIds(Long[] ids);

    /** 按考试删除参与记录 */
    int deleteByExamId(Long examId);
}
