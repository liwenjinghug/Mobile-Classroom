package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ClassDebate;
import com.ruoyi.proj_qhy.domain.ClassDebateMsg;
import java.util.List;
import java.util.Map;

public interface IClassDebateService {
    List<ClassDebate> selectClassDebateList(ClassDebate classDebate);
    ClassDebate selectClassDebateById(Long id);
    int insertClassDebate(ClassDebate classDebate);
    int updateClassDebate(ClassDebate classDebate);
    int deleteClassDebateByIds(Long[] ids);

    // 业务方法
    Map<String, Object> joinDebate(Long debateId, String inviteCode, String role);
    Map<String, Object> getRoomInfo(Long debateId);
    int sendMsg(ClassDebateMsg msg);
    List<ClassDebateMsg> getMsgList(Long debateId);
    int vote(Long debateId, String side);
    int startDebate(Long debateId);
    int stopDebate(Long debateId);
}