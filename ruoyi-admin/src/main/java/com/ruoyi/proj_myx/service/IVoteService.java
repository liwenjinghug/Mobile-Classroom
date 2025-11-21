package com.ruoyi.proj_myx.service;

import com.ruoyi.proj_myx.domain.Vote;
import com.ruoyi.proj_myx.domain.VoteRecord;

import java.util.List;
import java.util.Map;

public interface IVoteService {
    Vote createVote(Vote vote);
    List<Vote> getVotesBySession(Long sessionId);
    Vote getVote(Long voteId);
    int startVote(Long voteId);
    int closeVote(Long voteId);
    int deleteVote(Long voteId);
    int submitVote(Long voteId, Long studentId, String studentName, List<Long> optionIds);
    Map<String, Object> getVoteStats(Long voteId);
    boolean hasUserVoted(Long voteId, Long studentId);
}
