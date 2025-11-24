package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.ClassDebate;
import com.ruoyi.proj_qhy.domain.ClassDebateMsg;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassDebateMapper {

    // 查询列表
    @Select("SELECT * FROM class_debate ORDER BY create_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time")
    })
    List<ClassDebate> selectClassDebateList(ClassDebate classDebate);

    // 查询单条
    @Select("SELECT * FROM class_debate WHERE id = #{id}")
    ClassDebate selectClassDebateById(Long id);

    // 新增
    @Insert("INSERT INTO class_debate(title, pro_viewpoint, con_viewpoint, invite_code, status, create_by, create_time) " +
            "VALUES(#{title}, #{proViewpoint}, #{conViewpoint}, #{inviteCode}, #{status}, #{createBy}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertClassDebate(ClassDebate classDebate);

    // 修改
    @Update("<script>" +
            "UPDATE class_debate" +
            " <set>" +
            "   <if test='title != null'>title=#{title},</if>" +
            "   <if test='proViewpoint != null'>pro_viewpoint=#{proViewpoint},</if>" +
            "   <if test='conViewpoint != null'>con_viewpoint=#{conViewpoint},</if>" +
            "   <if test='inviteCode != null'>invite_code=#{inviteCode},</if>" +
            "   <if test='status != null'>status=#{status},</if>" +
            "   <if test='winner != null'>winner=#{winner},</if>" +
            "   <if test='speechLimit != null'>speech_limit=#{speechLimit},</if>" +
            "   <if test='currentTurn != null'>current_turn=#{currentTurn},</if>" +
            "   <if test='currentRole != null'>current_role=#{currentRole},</if>" +
            "   <if test='turnStartTime != null'>turn_start_time=#{turnStartTime},</if>" +
            "   <if test='updateBy != null'>update_by=#{updateBy},</if>" +
            "   <if test='updateTime != null'>update_time=#{updateTime},</if>" +
            " </set>" +
            " WHERE id = #{id}" +
            "</script>")
    int updateClassDebate(ClassDebate classDebate);

    // 新增：获取某方选手的名字列表
    @Select("SELECT nick_name FROM class_debate_user WHERE debate_id = #{debateId} AND role = #{role}")
    List<String> selectUserNamesByRole(@Param("debateId") Long debateId, @Param("role") String role);

    // 删除
    @Delete("DELETE FROM class_debate WHERE id = #{id}")
    int deleteClassDebateById(Long id);

    // --- 用户关联相关 ---

    @Select("SELECT COUNT(*) FROM class_debate_user WHERE debate_id=#{debateId} AND user_id=#{userId}")
    int checkUserJoined(@Param("debateId") Long debateId, @Param("userId") Long userId);

    @Select("SELECT role FROM class_debate_user WHERE debate_id=#{debateId} AND user_id=#{userId}")
    String getUserRole(@Param("debateId") Long debateId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM class_debate_user WHERE debate_id=#{debateId} AND role=#{role}")
    int countRoleUsers(@Param("debateId") Long debateId, @Param("role") String role);

    @Insert("INSERT INTO class_debate_user(debate_id, user_id, nick_name, role, join_time) " +
            "VALUES(#{debateId}, #{userId}, #{nickName}, #{role}, NOW())")
    int insertDebateUser(@Param("debateId") Long debateId, @Param("userId") Long userId,
                         @Param("nickName") String nickName, @Param("role") String role);

    // 投票相关
    @Update("UPDATE class_debate_user SET vote_side = #{side} WHERE debate_id=#{debateId} AND user_id=#{userId}")
    int updateVote(@Param("debateId") Long debateId, @Param("userId") Long userId, @Param("side") String side);

    @Select("SELECT vote_side FROM class_debate_user WHERE debate_id=#{debateId} AND user_id=#{userId}")
    String getUserVote(@Param("debateId") Long debateId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM class_debate_user WHERE debate_id=#{debateId} AND vote_side='1'")
    Long getProVotes(Long debateId);

    @Select("SELECT COUNT(*) FROM class_debate_user WHERE debate_id=#{debateId} AND vote_side='2'")
    Long getConVotes(Long debateId);

    // --- 消息相关 ---

    @Select("SELECT * FROM class_debate_msg WHERE debate_id = #{debateId} ORDER BY create_time ASC")
    List<ClassDebateMsg> selectMsgList(Long debateId);

    @Insert("INSERT INTO class_debate_msg(debate_id, user_id, nick_name, role, content, create_time) " +
            "VALUES(#{debateId}, #{userId}, #{nickName}, #{role}, #{content}, NOW())")
    int insertMsg(ClassDebateMsg msg);
}