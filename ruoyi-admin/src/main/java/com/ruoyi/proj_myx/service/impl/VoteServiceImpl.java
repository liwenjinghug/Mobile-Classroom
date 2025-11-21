package com.ruoyi.proj_myx.service.impl;

import com.ruoyi.proj_myx.domain.Vote;
import com.ruoyi.proj_myx.domain.VoteOption;
import com.ruoyi.proj_myx.domain.VoteRecord;
import com.ruoyi.proj_myx.mapper.VoteMapper;
import com.ruoyi.proj_myx.service.IVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl implements IVoteService {

    @Autowired
    private VoteMapper voteMapper;

    @Override
    @Transactional
    public Vote createVote(Vote vote) {
        voteMapper.insertVote(vote);
        if (vote.getOptions() != null) {
            for (int i = 0; i < vote.getOptions().size(); i++) {
                VoteOption opt = vote.getOptions().get(i);
                opt.setVoteId(vote.getVoteId());
                opt.setSortOrder(i);
                voteMapper.insertOption(opt);
            }
        }
        return getVote(vote.getVoteId());
    }

    @Override
    public List<Vote> getVotesBySession(Long sessionId) {
        List<Vote> votes = voteMapper.selectBySession(sessionId);
        if (votes == null) return Collections.emptyList();
        
        Date now = new Date();
        for (Vote v : votes) {
            // Auto status update logic
            String currentStatus = v.getStatus();
            String newStatus = currentStatus;
            
            if (v.getEndTime() != null && now.after(v.getEndTime())) {
                newStatus = "2"; // Ended
            } else if (v.getStartTime() != null && now.before(v.getStartTime())) {
                newStatus = "0"; // Not started
            } else {
                // If it was not started but time has come, or if it was ended but time extended (unlikely but possible)
                // Usually we only auto-start if it was 0. If it was 2, we keep it 2 unless manually reopened?
                // Let's follow simple logic: if within time window, it should be 1.
                // BUT, if user manually stopped it (status=2) before time, we should respect that?
                // For simplicity, let's stick to time-based auto-correction similar to Attendance
                newStatus = "1";
            }
            
            // Special case: if manually ended (2), maybe we shouldn't auto-reopen?
            // But if we follow Attendance logic strictly:
            // Attendance logic: if now > end -> 2; if now < start -> 0; else -> 1.
            
            if (!Objects.equals(newStatus, currentStatus)) {
                v.setStatus(newStatus);
                voteMapper.updateStatus(v);
            }
            
            // Load options for display
            v.setOptions(voteMapper.selectOptionsByVoteId(v.getVoteId()));
        }
        return votes;
    }

    @Override
    public Vote getVote(Long voteId) {
        Vote v = voteMapper.selectById(voteId);
        if (v != null) {
            v.setOptions(voteMapper.selectOptionsByVoteId(voteId));
        }
        return v;
    }

    @Override
    public int startVote(Long voteId) {
        Vote v = new Vote();
        v.setVoteId(voteId);
        v.setStatus("1");
        v.setStartTime(new Date());
        return voteMapper.updateStatusAndStartTime(v);
    }

    @Override
    public int closeVote(Long voteId) {
        Vote v = new Vote();
        v.setVoteId(voteId);
        v.setStatus("2");
        v.setEndTime(new Date());
        return voteMapper.updateStatusAndEndTime(v);
    }

    @Override
    @Transactional
    public int deleteVote(Long voteId) {
        voteMapper.deleteRecordsByVoteId(voteId);
        voteMapper.deleteOptionsByVoteId(voteId);
        return voteMapper.deleteVoteById(voteId);
    }

    @Override
    @Transactional
    public int submitVote(Long voteId, Long studentId, String studentName, List<Long> optionIds) {
        // Check if already voted
        if (voteMapper.checkUserVoted(voteId, studentId) > 0) {
            throw new RuntimeException("您已经投过票了");
        }
        
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null || !"1".equals(vote.getStatus())) {
            throw new RuntimeException("投票未开始或已结束");
        }

        for (Long optId : optionIds) {
            VoteRecord r = new VoteRecord();
            r.setVoteId(voteId);
            r.setStudentId(studentId);
            r.setStudentName(studentName);
            r.setOptionId(optId);
            voteMapper.insertRecord(r);
        }
        return optionIds.size();
    }

    @Override
    public Map<String, Object> getVoteStats(Long voteId) {
        Vote vote = getVote(voteId);
        if (vote == null) return null;
        
        List<Map<String, Object>> counts = voteMapper.countByOption(voteId);
        Map<Long, Integer> countMap = new HashMap<>();
        for (Map<String, Object> m : counts) {
            Long optId = (Long) m.get("option_id");
            Number cnt = (Number) m.get("count");
            countMap.put(optId, cnt.intValue());
        }
        
        // Check anonymity
        boolean isAnonymous = "1".equals(vote.getIsAnonymous());
        Map<Long, List<String>> namesMap = new HashMap<>();
        
        if (!isAnonymous) {
            List<VoteRecord> records = voteMapper.selectRecordsByVoteId(voteId);
            for (VoteRecord r : records) {
                namesMap.computeIfAbsent(r.getOptionId(), k -> new ArrayList<>()).add(r.getStudentName());
            }
        }
        
        List<VoteOption> options = vote.getOptions();
        int totalVotes = 0;
        for (VoteOption opt : options) {
            Integer c = countMap.getOrDefault(opt.getOptionId(), 0);
            opt.setCount(c);
            totalVotes += c;
            
            if (!isAnonymous) {
                opt.setVoterNames(namesMap.getOrDefault(opt.getOptionId(), Collections.emptyList()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("vote", vote);
        result.put("total", totalVotes);
        
        return result;
    }

    @Override
    public boolean hasUserVoted(Long voteId, Long studentId) {
        return voteMapper.checkUserVoted(voteId, studentId) > 0;
    }
}
