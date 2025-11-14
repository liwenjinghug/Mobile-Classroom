// Java
package com.ruoyi.proj_cyq.service;

import com.ruoyi.proj_cyq.domain.ClassPasswordReset;
import com.ruoyi.common.core.domain.entity.SysUser;

public interface IClassPasswordResetService {

    /**
     * 为用户创建重置令牌
     * @param user SysUser对象
     * @return 令牌字符串
     */
    public String createResetToken(SysUser user);

    /**
     * 验证令牌
     * @param token 令牌
     * @return 令牌记录，如果无效则返回 null
     */
    public ClassPasswordReset verifyToken(String token);

    /**
     * 标记令牌已使用
     * @param id 令牌ID
     */
    public void markUsed(Long id);
}