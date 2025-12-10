package com.ruoyi.common.core.domain.model;

/**
 * APP登录对象
 * 
 * @author ruoyi
 */
public class AppLoginBody extends LoginBody
{
    /**
     * 微信OpenID
     */
    private String openId;

    /**
     * 微信昵称
     */
    private String nickname;

    /**
     * 微信头像
     */
    private String avatar;

    public String getOpenId()
    {
        return openId;
    }

    public void setOpenId(String openId)
    {
        this.openId = openId;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }
}
