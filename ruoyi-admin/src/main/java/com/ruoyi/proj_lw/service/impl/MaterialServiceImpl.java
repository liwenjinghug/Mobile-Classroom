package com.ruoyi.proj_lw.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_lw.mapper.MaterialMapper;
import com.ruoyi.proj_lw.domain.Material;
import com.ruoyi.proj_lw.service.IMaterialService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 资料Service业务层处理
 */
@Service
public class MaterialServiceImpl implements IMaterialService
{
    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public Material selectMaterialByMaterialId(Long materialId) {
        return materialMapper.selectMaterialByMaterialId(materialId);
    }

    @Override
    public List<Material> selectMaterialList(Material material) {
        return materialMapper.selectMaterialList(material);
    }

    @Override
    public int insertMaterial(Material material) {
        material.setCreateTime(new Date());
        material.setCreateBy(SecurityUtils.getUsername());
        return materialMapper.insertMaterial(material);
    }

    @Override
    public int updateMaterial(Material material) {
        material.setUpdateTime(new Date());
        material.setUpdateBy(SecurityUtils.getUsername());
        return materialMapper.updateMaterial(material);
    }

    @Override
    public int deleteMaterialByMaterialIds(Long[] materialIds) {
        return materialMapper.deleteMaterialByMaterialIds(materialIds);
    }

    @Override
    public int deleteMaterialByMaterialId(Long materialId) {
        return materialMapper.deleteMaterialByMaterialId(materialId);
    }

    @Override
    public AjaxResult pushMaterialToStudents(Long materialId) {
        Material material = materialMapper.selectMaterialByMaterialId(materialId);
        if (material == null) {
            return AjaxResult.error("资料不存在");
        }

        if ("1".equals(material.getPushStatus())) {
            return AjaxResult.error("资料已推送，无需重复推送");
        }

        material.setPushStatus("1");
        material.setPushTime(new Date());
        material.setUpdateTime(new Date());
        material.setUpdateBy(SecurityUtils.getUsername());

        int result = materialMapper.updateMaterial(material);
        if (result > 0) {
            return AjaxResult.success("资料推送成功");
        } else {
            return AjaxResult.error("资料推送失败");
        }
    }
}