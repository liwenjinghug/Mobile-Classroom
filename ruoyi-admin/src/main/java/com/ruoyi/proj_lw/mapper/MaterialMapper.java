package com.ruoyi.proj_lw.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_lw.domain.Material;

/**
 * 资料Mapper接口
 */
@Mapper
public interface MaterialMapper
{
    /**
     * 查询资料
     */
    @Select("select material_id, session_id, material_name, file_type, file_size, file_path, push_status, push_time, create_by, create_time, update_by, update_time, remark from class_material where material_id = #{materialId}")
    Material selectMaterialByMaterialId(Long materialId);

    /**
     * 查询资料列表
     */
    @Select({
            "<script>",
            "select material_id, session_id, material_name, file_type, file_size, file_path, push_status, push_time, create_by, create_time, update_by, update_time, remark from class_material",
            "<where>",
            "<if test='sessionId != null'> and session_id = #{sessionId}</if>",
            "<if test='materialName != null and materialName != \"\"'> and material_name like concat('%', #{materialName}, '%')</if>",
            "<if test='fileType != null and fileType != \"\"'> and file_type = #{fileType}</if>",
            "<if test='pushStatus != null and pushStatus != \"\"'> and push_status = #{pushStatus}</if>",
            // 如果是学生，即使没有设置pushStatus，也强制只查询已推送的资料
            "<if test='pushStatus == null'>",
            "   and 1=1", // 这个条件只是为了语法正确，实际逻辑在Service层控制
            "</if>",
            "</where>",
            "order by create_time desc",
            "</script>"
    })
    List<Material> selectMaterialList(Material material);

    /**
     * 新增资料
     */
    @Insert("insert into class_material(" +
            "session_id, material_name, file_type, file_size, file_path, push_status, push_time, create_by, create_time, update_by, update_time, remark" +
            ") values (" +
            "#{sessionId}, #{materialName}, #{fileType}, #{fileSize}, #{filePath}, #{pushStatus}, #{pushTime}, #{createBy}, #{createTime}, #{updateBy}, #{updateTime}, #{remark}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "materialId")
    int insertMaterial(Material material);

    /**
     * 修改资料
     */
    @Update({
            "<script>",
            "update class_material",
            "<set>",
            "<if test='sessionId != null'>session_id = #{sessionId},</if>",
            "<if test='materialName != null and materialName != \"\"'>material_name = #{materialName},</if>",
            "<if test='fileType != null and fileType != \"\"'>file_type = #{fileType},</if>",
            "<if test='fileSize != null'>file_size = #{fileSize},</if>",
            "<if test='filePath != null and filePath != \"\"'>file_path = #{filePath},</if>",
            "<if test='pushStatus != null and pushStatus != \"\"'>push_status = #{pushStatus},</if>",
            "<if test='pushTime != null'>push_time = #{pushTime},</if>",
            "<if test='updateBy != null and updateBy != \"\"'>update_by = #{updateBy},</if>",
            "<if test='updateTime != null'>update_time = #{updateTime},</if>",
            "<if test='remark != null'>remark = #{remark},</if>",
            "</set>",
            "where material_id = #{materialId}",
            "</script>"
    })
    int updateMaterial(Material material);

    /**
     * 删除资料
     */
    @Delete("delete from class_material where material_id = #{materialId}")
    int deleteMaterialByMaterialId(Long materialId);

    /**
     * 批量删除资料
     */
    @Delete({
            "<script>",
            "delete from class_material where material_id in",
            "<foreach collection='array' item='materialId' open='(' separator=',' close=')'>",
            "#{materialId}",
            "</foreach>",
            "</script>"
    })
    int deleteMaterialByMaterialIds(Long[] materialIds);
}