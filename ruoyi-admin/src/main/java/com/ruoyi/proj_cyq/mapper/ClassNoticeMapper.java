// src/main/java/com/ruoyi/proj_cyq/mapper/ClassNoticeMapper.java
package com.ruoyi.proj_cyq.mapper;

import com.ruoyi.proj_cyq.domain.ClassNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassNoticeMapper {

    /**
     * 查询公告列表
     */
    @Select("<script>" +
            "SELECT notice_id, title, content, create_by, create_time, update_by, update_time, del_flag " +
            "FROM class_notice " +
            "WHERE del_flag = 0 " +
            "<if test=\"title != null and title != ''\"> AND title LIKE CONCAT('%', #{title}, '%')</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<ClassNotice> selectClassNoticeList(ClassNotice notice);

    /**
     * 根据ID查询公告
     */
    @Select("SELECT notice_id, title, content, create_by, create_time, update_by, update_time, del_flag " +
            "FROM class_notice WHERE notice_id = #{noticeId} AND del_flag = 0")
    ClassNotice selectClassNoticeById(Long noticeId);

    /**
     * 新增公告
     */
    @Insert("INSERT INTO class_notice (title, content, create_by, create_time, del_flag) " +
            "VALUES (#{title}, #{content}, #{createBy}, sysdate(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "noticeId")
    int insertClassNotice(ClassNotice notice);

    /**
     * 修改公告
     */
    @Update("UPDATE class_notice SET title = #{title}, content = #{content}, " +
            "update_by = #{updateBy}, update_time = sysdate() WHERE notice_id = #{noticeId}")
    int updateClassNotice(ClassNotice notice);

    /**
     * 删除公告（逻辑删除）
     */
    @Update("UPDATE class_notice SET del_flag = 1, update_by = #{updateBy}, update_time = sysdate() " +
            "WHERE notice_id = #{noticeId}")
    int deleteClassNoticeById(ClassNotice notice);

    /**
     * 批量删除公告
     */
    @Update("<script>" +
            "UPDATE class_notice SET del_flag = 1, update_by = #{updateBy}, update_time = sysdate() " +
            "WHERE notice_id IN " +
            "<foreach collection='array' item='noticeId' open='(' separator=',' close=')'>" +
            "#{noticeId}" +
            "</foreach>" +
            "</script>")
    int deleteClassNoticeByIds(@Param("array") Long[] noticeIds, @Param("updateBy") String updateBy);
}