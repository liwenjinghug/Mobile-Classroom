// Java
package com.ruoyi.proj_cyq.mapper;

import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_cyq.domain.ClassPasswordReset;

/**
 * 密码重置Mapper接口 (使用MyBatis注解)
 * @author (您的名字)
 */
@Mapper // 确保添加 @Mapper 注解
public interface ClassPasswordResetMapper
{
    /**
     * 插入一条新的重置记录
     */
    @Insert("INSERT INTO class_password_reset(user_id, email, token, expire_time, used_flag, create_time) " +
            "VALUES(#{userId}, #{email}, #{token}, #{expireTime}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public int insert(ClassPasswordReset record);

    /**
     * 根据Token查询
     */
    @Select("SELECT id, user_id, email, token, expire_time, used_flag " +
            "FROM class_password_reset " +
            "WHERE token = #{token} LIMIT 1")
    public ClassPasswordReset selectByToken(@Param("token") String token);

    /**
     * 标记Token为已使用
     */
    @Update("UPDATE class_password_reset " +
            "SET used_flag = 1 " +
            "WHERE id = #{id}")
    public int markUsed(@Param("id") Long id);
}
