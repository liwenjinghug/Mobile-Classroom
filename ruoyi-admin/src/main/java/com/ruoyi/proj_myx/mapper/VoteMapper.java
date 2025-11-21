package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.Vote;
import com.ruoyi.proj_myx.domain.VoteOption;
import com.ruoyi.proj_myx.domain.VoteRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface VoteMapper {
    // Vote operations
    @Insert("INSERT INTO class_vote(session_id, title, type, status, is_anonymous, start_time, end_time, create_by, create_time) " +
            "VALUES(#{sessionId}, #{title}, #{type}, #{status}, #{isAnonymous}, #{startTime}, #{endTime}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "voteId", keyColumn = "vote_id")
    int insertVote(Vote vote);

    @Select("SELECT * FROM class_vote WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<Vote> selectBySession(Long sessionId);

    @Select("SELECT * FROM class_vote WHERE vote_id = #{voteId}")
    Vote selectById(Long voteId);

    @Update("UPDATE class_vote SET status = #{status}, update_time = NOW() WHERE vote_id = #{voteId}")
    int updateStatus(Vote vote);
    
    @Update("UPDATE class_vote SET status = #{status}, start_time = #{startTime}, update_time = NOW() WHERE vote_id = #{voteId}")
    int updateStatusAndStartTime(Vote vote);

    @Update("UPDATE class_vote SET status = #{status}, end_time = #{endTime}, update_time = NOW() WHERE vote_id = #{voteId}")
    int updateStatusAndEndTime(Vote vote);

    @Delete("DELETE FROM class_vote WHERE vote_id = #{voteId}")
    int deleteVoteById(Long voteId);

    // Option operations
    @Insert("INSERT INTO class_vote_option(vote_id, label, content, sort_order) VALUES(#{voteId}, #{label}, #{content}, #{sortOrder})")
    int insertOption(VoteOption option);

    @Select("SELECT * FROM class_vote_option WHERE vote_id = #{voteId} ORDER BY sort_order ASC")
    List<VoteOption> selectOptionsByVoteId(Long voteId);

    @Delete("DELETE FROM class_vote_option WHERE vote_id = #{voteId}")
    int deleteOptionsByVoteId(Long voteId);

    // Record operations
    @Insert("INSERT INTO class_vote_record(vote_id, student_id, student_name, option_id, vote_time) " +
            "VALUES(#{voteId}, #{studentId}, #{studentName}, #{optionId}, NOW())")
    int insertRecord(VoteRecord record);

    @Select("SELECT * FROM class_vote_record WHERE vote_id = #{voteId}")
    List<VoteRecord> selectRecordsByVoteId(Long voteId);
    
    @Select("SELECT count(1) FROM class_vote_record WHERE vote_id = #{voteId} AND student_id = #{studentId}")
    int checkUserVoted(@Param("voteId") Long voteId, @Param("studentId") Long studentId);

    @Delete("DELETE FROM class_vote_record WHERE vote_id = #{voteId}")
    int deleteRecordsByVoteId(Long voteId);
    
    @Select("SELECT option_id, COUNT(*) as count FROM class_vote_record WHERE vote_id = #{voteId} GROUP BY option_id")
    List<Map<String, Object>> countByOption(Long voteId);
}
