package com.ruoyi.proj_qhy.domain;

import lombok.Data;
import com.ruoyi.common.core.domain.entity.SysUser;

@Data
public class ClassDebateParticipant {
    private static final long serialVersionUID = 1L;

    /** 参与者ID */
    private Long participantId;

    /** 辩论ID */
    private Long debateId;

    /** 用户ID */
    private Long userId;

    /** 队伍（P=正方, N=反方） */
    private String team;

    /** 角色（D=辩手, A=观众） */
    private String role;

    /** 关联用户信息 */
    private SysUser user;
}
