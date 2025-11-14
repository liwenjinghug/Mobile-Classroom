// Java
package com.ruoyi.proj_cyq.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.UUID;
import com.ruoyi.proj_cyq.mapper.ClassPasswordResetMapper;
import com.ruoyi.proj_cyq.domain.ClassPasswordReset;
import com.ruoyi.proj_cyq.service.IClassPasswordResetService;
import com.ruoyi.common.core.domain.entity.SysUser;

@Service
public class ClassPasswordResetServiceImpl implements IClassPasswordResetService {

    @Autowired
    private ClassPasswordResetMapper mapper;

    // 令牌有效期（毫秒），这里设为30分钟
    private static final long EXPIRE_DURATION = 30 * 60 * 1000;

    @Override
    public String createResetToken(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");

        ClassPasswordReset r = new ClassPasswordReset();
        r.setUserId(user.getUserId());
        r.setEmail(user.getEmail());
        r.setToken(token);
        r.setExpireTime(new Date(System.currentTimeMillis() + EXPIRE_DURATION));

        mapper.insert(r);
        return token;
    }

    @Override
    public ClassPasswordReset verifyToken(String token) {
        ClassPasswordReset r = mapper.selectByToken(token);

        if (r == null) {
            return null; // 令牌不存在
        }
        if (r.getUsedFlag() != null && r.getUsedFlag() == 1) {
            return null; // 令牌已使用
        }
        if (r.getExpireTime() == null || r.getExpireTime().before(new Date())) {
            return null; // 令牌已过期
        }

        return r;
    }

    @Override
    public void markUsed(Long id) {
        mapper.markUsed(id);
    }
}