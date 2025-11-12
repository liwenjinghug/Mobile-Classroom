package com.ruoyi.proj_lw.service;

import java.util.List;
import com.ruoyi.proj_lw.domain.Material;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 资料Service接口
 */
public interface IMaterialService
{
    /**
     * 查询资料
     */
    Material selectMaterialByMaterialId(Long materialId);

    /**
     * 查询资料列表
     */
    List<Material> selectMaterialList(Material material);

    /**
     * 新增资料
     */
    int insertMaterial(Material material);

    /**
     * 修改资料
     */
    int updateMaterial(Material material);

    /**
     * 批量删除资料
     */
    int deleteMaterialByMaterialIds(Long[] materialIds);

    /**
     * 删除资料信息
     */
    int deleteMaterialByMaterialId(Long materialId);

    /**
     * 推送资料给学生
     */
    AjaxResult pushMaterialToStudents(Long materialId);
}