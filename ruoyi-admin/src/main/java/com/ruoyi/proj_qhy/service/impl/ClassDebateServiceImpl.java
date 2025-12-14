package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.proj_qhy.domain.ClassDebate;
import com.ruoyi.proj_qhy.domain.ClassDebateMsg;
import com.ruoyi.proj_qhy.mapper.ClassDebateMapper;
import com.ruoyi.proj_qhy.service.IClassDebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ClassDebateServiceImpl implements IClassDebateService {

    @Autowired
    private ClassDebateMapper classDebateMapper;

    @Override
    public List<ClassDebate> selectClassDebateList(ClassDebate classDebate) {
        Long currentUserId = SecurityUtils.getUserId();
        List<ClassDebate> list = classDebateMapper.selectClassDebateList(classDebate);
        for (ClassDebate d : list) {
            // 只有创建者或已加入的人才能看到邀请码，否则显示 "-"
            boolean isCreator = d.getCreateBy().equals(SecurityUtils.getUsername());
            boolean isJoined = classDebateMapper.checkUserJoined(d.getId(), currentUserId) > 0;
            if (!isCreator && !isJoined) {
                d.setInviteCode("-");
                d.setJoined(false);
            } else {
                d.setJoined(true);
            }
        }
        return list;
    }

    @Override
    public ClassDebate selectClassDebateById(Long id) {
        return classDebateMapper.selectClassDebateById(id);
    }

    @Override
    @Transactional
    public int insertClassDebate(ClassDebate classDebate) {
        classDebate.setCreateTime(DateUtils.getNowDate());
        classDebate.setCreateBy(SecurityUtils.getUsername());
        classDebate.setStatus("0"); // 未开始
        // 生成唯一邀请码 (6位随机)
        classDebate.setInviteCode(IdUtils.simpleUUID().substring(0, 6).toUpperCase());

        int rows = classDebateMapper.insertClassDebate(classDebate);

        // 创建人自动成为观众 (3)
        classDebateMapper.insertDebateUser(classDebate.getId(), SecurityUtils.getUserId(),
                SecurityUtils.getLoginUser().getUser().getNickName(), "3");

        return rows;
    }

    @Override
    public int updateClassDebate(ClassDebate classDebate) {
        ClassDebate old = classDebateMapper.selectClassDebateById(classDebate.getId());
        if (!"0".equals(old.getStatus())) {
            throw new RuntimeException("辩论已开始，无法修改信息");
        }
        classDebate.setUpdateTime(DateUtils.getNowDate());
        classDebate.setUpdateBy(SecurityUtils.getUsername());
        return classDebateMapper.updateClassDebate(classDebate);
    }

    @Override
    public int deleteClassDebateByIds(Long[] ids) {
        int count = 0;
        Long currentUserId = SecurityUtils.getUserId();
        String currentUsername = SecurityUtils.getUsername();

        for (Long id : ids) {
            ClassDebate debate = classDebateMapper.selectClassDebateById(id);
            if (debate != null) {
                // 校验：不是超级管理员 且 不是创建者，则禁止删除
                if (!SecurityUtils.isAdmin(currentUserId) && !debate.getCreateBy().equals(currentUsername)) {
                    throw new RuntimeException("无权删除由 " + debate.getCreateBy() + " 创建的辩论");
                }
                count += classDebateMapper.deleteClassDebateById(id);
            }
        }
        return count;
    }

    @Override
    @Transactional
    public Map<String, Object> joinDebate(Long debateId, String inviteCode, String role) {
        Long userId = SecurityUtils.getUserId();
        ClassDebate debate = classDebateMapper.selectClassDebateById(debateId);

        if (debate == null) throw new RuntimeException("辩论不存在");

        // 校验邀请码
        if (!debate.getInviteCode().equalsIgnoreCase(inviteCode)) {
            throw new RuntimeException("邀请码错误");
        }

        // 检查是否已加入
        if (classDebateMapper.checkUserJoined(debateId, userId) > 0) {
            // 已加入，直接返回成功，不做处理
            Map<String, Object> res = new HashMap<>();
            res.put("role", classDebateMapper.getUserRole(debateId, userId));
            return res;
        }

        // 检查状态，如果开始了，只能做观众
        if (!"0".equals(debate.getStatus()) && !"3".equals(role)) {
            throw new RuntimeException("辩论已开始，无法作为选手加入，只能作为观众");
        }

        // 检查人数限制 (选手各5人)
        if ("1".equals(role) || "2".equals(role)) {
            int count = classDebateMapper.countRoleUsers(debateId, role);
            if (count >= 5) {
                throw new RuntimeException("该方选手已满员");
            }
        }

        classDebateMapper.insertDebateUser(debateId, userId,
                SecurityUtils.getLoginUser().getUser().getNickName(), role);

        Map<String, Object> res = new HashMap<>();
        res.put("role", role);
        return res;
    }

    @Override
    public List<ClassDebateMsg> getMsgList(Long debateId) {
        return classDebateMapper.selectMsgList(debateId);
    }

    @Override
    public int vote(Long debateId, String side) {
        Long userId = SecurityUtils.getUserId();
        String role = classDebateMapper.getUserRole(debateId, userId);
        ClassDebate debate = classDebateMapper.selectClassDebateById(debateId);

        // 1. 只有观众能投票 (创建者是观众身份，也可以投)
        if (!"3".equals(role)) {
            throw new RuntimeException("选手不能投票");
        }
        // 2. 只有进行中或刚结束时能投？需求说“辩论结束后点赞截止”
        if (!"1".equals(debate.getStatus())) {
            throw new RuntimeException("当前状态无法投票");
        }

        return classDebateMapper.updateVote(debateId, userId, side);
    }



    @Override
    public int stopDebate(Long debateId) {
        ClassDebate debate = classDebateMapper.selectClassDebateById(debateId);
        Long proVotes = classDebateMapper.getProVotes(debateId);
        Long conVotes = classDebateMapper.getConVotes(debateId);

        String winner = "0"; // 平局
        if (proVotes > conVotes) winner = "1";
        if (conVotes > proVotes) winner = "2";

        debate.setStatus("2"); // 结束
        debate.setWinner(winner);
        debate.setUpdateBy(SecurityUtils.getUsername());
        debate.setUpdateTime(DateUtils.getNowDate());

        return classDebateMapper.updateClassDebate(debate);
    }

    @Override
    @Transactional // 开启事务，因为读取时可能修改状态
    public Map<String, Object> getRoomInfo(Long debateId) {
        // 1. 获取辩论基本信息
        ClassDebate debate = classDebateMapper.selectClassDebateById(debateId);
        if (debate == null) throw new RuntimeException("辩论不存在");

        // --- 核心逻辑：自动检查时间并切换回合 ---
        if ("1".equals(debate.getStatus())) { // 只有进行中才检查
            checkAndSwitchTurn(debate);
        }
        // ------------------------------------

        // 2. 填充选手名单 (新增需求)
        debate.setProPlayerNames(classDebateMapper.selectUserNamesByRole(debateId, "1"));
        debate.setConPlayerNames(classDebateMapper.selectUserNamesByRole(debateId, "2"));

        Map<String, Object> data = new HashMap<>();
        Long userId = SecurityUtils.getUserId();

        data.put("debate", debate);
        data.put("currentUserRole", classDebateMapper.getUserRole(debateId, userId));
        data.put("currentVote", classDebateMapper.getUserVote(debateId, userId));
        data.put("proVotes", classDebateMapper.getProVotes(debateId));
        data.put("conVotes", classDebateMapper.getConVotes(debateId));

        // 加上服务器当前时间，防止客户端时间不准导致倒计时错误
        data.put("serverTime", new Date());

        return data;
    }

    /**
     * 检查当前回合是否超时，如果超时则切换到下一方
     */
    private void checkAndSwitchTurn(ClassDebate debate) {
        if (debate.getTurnStartTime() == null) return;

        long now = System.currentTimeMillis();
        long start = debate.getTurnStartTime().getTime();
        long limitMs = debate.getSpeechLimit() * 1000; // 秒转毫秒

        // 如果超时
        if (now - start >= limitMs) {
            int nextTurn = debate.getCurrentTurn() + 1;

            // 如果超过10回合 (每方5次，共10次)，则结束辩论
            if (nextTurn > 10) {
                stopDebate(debate.getId()); // 调用结束逻辑
                debate.setStatus("2"); // 更新内存中的对象以便返回给前端
            } else {
                // 切换回合
                debate.setCurrentTurn(nextTurn);
                // 角色互换: 1->2, 2->1
                debate.setCurrentRole("1".equals(debate.getCurrentRole()) ? "2" : "1");
                debate.setTurnStartTime(DateUtils.getNowDate());
                debate.setUpdateBy("system");

                classDebateMapper.updateClassDebate(debate);
            }
        }
    }

    @Override
    public int startDebate(Long debateId) {
        ClassDebate debate = classDebateMapper.selectClassDebateById(debateId);
        if (debate == null) throw new RuntimeException("不存在");

        debate.setStatus("1"); // 进行中
        debate.setCurrentTurn(1); // 第1回合
        debate.setTurnStartTime(DateUtils.getNowDate());

        // 随机先手
        String firstRole = new Random().nextBoolean() ? "1" : "2";
        debate.setCurrentRole(firstRole);

        // 如果创建时没设置时长，默认60秒
        if (debate.getSpeechLimit() == null || debate.getSpeechLimit() <= 0) {
            debate.setSpeechLimit(60);
        }

        debate.setUpdateBy(SecurityUtils.getUsername());
        return classDebateMapper.updateClassDebate(debate);
    }

    @Override
    public int sendMsg(ClassDebateMsg msg) {
        ClassDebate debate = classDebateMapper.selectClassDebateById(msg.getDebateId());

        // 1. 基础状态检查
        if (!"1".equals(debate.getStatus())) {
            throw new RuntimeException("辩论未开始或已结束");
        }

        // 2. 检查用户角色
        Long userId = SecurityUtils.getUserId();
        String userRole = classDebateMapper.getUserRole(msg.getDebateId(), userId);

        // 3. 【新增】检查是否是当前发言方
        // 只有当前 currentRole 等于 用户的 role 才能发言
        if (!debate.getCurrentRole().equals(userRole)) {
            throw new RuntimeException("当前不是我方发言回合，请等待！");
        }

        // 4. 双重检查时间 (防止刚刚超时那一瞬间发出)
        long now = System.currentTimeMillis();
        long start = debate.getTurnStartTime().getTime();
        if (now - start > debate.getSpeechLimit() * 1000) {
            // 这里可以选择抛出异常，或者静默触发切换。抛异常让前端刷新比较好。
            checkAndSwitchTurn(debate); // 尝试触发一次切换
            throw new RuntimeException("本回合时间已到，无法发送");
        }

        msg.setUserId(userId);
        msg.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        msg.setRole(userRole);
        return classDebateMapper.insertMsg(msg);
    }
}