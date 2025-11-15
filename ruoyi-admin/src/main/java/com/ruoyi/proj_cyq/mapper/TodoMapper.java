package com.ruoyi.proj_cyq.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_cyq.domain.Todo;
import java.util.Date;

@Mapper
public interface TodoMapper {

    @Select("SELECT * FROM class_todo WHERE todo_id = #{todoId}")
    @Results({
            @Result(column = "todo_id", property = "todoId"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "todo_type", property = "todoType"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "status", property = "status"),
            @Result(column = "remind_time", property = "remindTime"),
            @Result(column = "is_reminded", property = "isReminded"),
            @Result(column = "create_by", property = "createBy"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_by", property = "updateBy"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "remark", property = "remark"),
            @Result(column = "message_status", property = "messageStatus"),
            @Result(column = "message_read", property = "messageRead")
    })
    Todo selectTodoById(Long todoId);

    @Select("<script>" +
            "SELECT * FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "<if test='title != null and title != \"\"'> AND title LIKE CONCAT('%', #{title}, '%')</if>" +
            "<if test='todoType != null and todoType != \"\"'> AND todo_type = #{todoType}</if>" +
            "<if test='priority != null and priority != \"\"'> AND priority = #{priority}</if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    @Results({
            @Result(column = "todo_id", property = "todoId"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "todo_type", property = "todoType"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "status", property = "status"),
            @Result(column = "remind_time", property = "remindTime"),
            @Result(column = "is_reminded", property = "isReminded"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "message_status", property = "messageStatus"),
            @Result(column = "message_read", property = "messageRead")
    })
    List<Todo> selectTodoList(Todo todo);

    // 【修改点 1】查询用于消息中心的待办事项
    @Select("SELECT * FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "AND status = '0' " + // 必须是未完成的
            "AND (message_status = '0' OR (message_status = '1' AND remind_time <= NOW())) " + // 核心逻辑
            "ORDER BY create_time DESC")
    @Results({
            @Result(column = "todo_id", property = "todoId"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "todo_type", property = "todoType"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "status", property = "status"),
            @Result(column = "remind_time", property = "remindTime"),
            @Result(column = "is_reminded", property = "isReminded"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "message_status", property = "messageStatus"),
            @Result(column = "message_read", property = "messageRead")
    })
    List<Todo> selectTodoMessages(Long userId);

    // 【修改点 3】更新消息状态（改为动态SQL）
    @Update("<script>" +
            "UPDATE class_todo SET " +
            "<if test='messageStatus != null'> message_status = #{messageStatus}, </if>" +
            "<if test='messageRead != null'> message_read = #{messageRead}, </if>" +
            "update_by = #{updateBy}, " +
            "update_time = #{updateTime} " +
            "WHERE todo_id = #{todoId}" +
            "</script>")
    int updateTodoMessageStatus(Todo todo);

    // 【修改点 2】获取未读消息数量
    @Select("SELECT COUNT(*) FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "AND status = '0' " + // 必须是未完成的
            "AND (" +
            "  (message_status = '0' AND message_read = '0') " + // 1. 正常未读的
            "  OR (message_status = '1' AND remind_time <= NOW() AND message_read = '0')" + // 2. 已删除但到期重现的(并且未读)
            ")")
    int selectUnreadMessageCount(Long userId);

    @Insert("INSERT INTO class_todo(" +
            "user_id, title, content, todo_type, priority, " +
            "start_time, end_time, status, remind_time, is_reminded, " +
            "message_status, message_read, " +
            "create_by, create_time, update_by, update_time, remark" +
            ") VALUES(" +
            "#{userId}, #{title}, #{content}, #{todoType}, #{priority}, " +
            "#{startTime}, #{endTime}, #{status}, #{remindTime}, #{isReminded}, " +
            "#{messageStatus}, #{messageRead}, " +
            "#{createBy}, #{createTime}, #{updateBy}, #{updateTime}, #{remark}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "todoId", keyColumn = "todo_id")
    int insertTodo(Todo todo);

    @Update("UPDATE class_todo SET " +
            "title = #{title}, content = #{content}, " +
            "todo_type = #{todoType}, priority = #{priority}, start_time = #{startTime}, " +
            "end_time = #{endTime}, status = #{status}, remind_time = #{remindTime}, " +
            "is_reminded = #{isReminded}, message_status = #{messageStatus}, message_read = #{messageRead}, " +
            "update_by = #{updateBy}, update_time = #{updateTime}, " +
            "remark = #{remark} " +
            "WHERE todo_id = #{todoId}")
    int updateTodo(Todo todo);

    @Delete("DELETE FROM class_todo WHERE todo_id = #{todoId}")
    int deleteTodoById(Long todoId);

    @Delete("<script>" +
            "DELETE FROM class_todo WHERE todo_id IN " +
            "<foreach collection='array' item='todoId' open='(' separator=',' close=')'>" +
            "#{todoId}" +
            "</foreach>" +
            "</script>")
    int deleteTodoByIds(Long[] todoIds);

    @Select("SELECT * FROM class_todo WHERE status = '0' AND is_reminded = '0' AND remind_time <= NOW()")
    @Results({
            @Result(column = "todo_id", property = "todoId"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "title", property = "title"),
            @Result(column = "content", property = "content"),
            @Result(column = "todo_type", property = "todoType"),
            @Result(column = "priority", property = "priority"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "status", property = "status"),
            @Result(column = "remind_time", property = "remindTime"),
            @Result(column = "is_reminded", property = "isReminded"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "message_status", property = "messageStatus"),
            @Result(column = "message_read", property = "messageRead")
    })
    List<Todo> selectTodoRemindList();

    // 统计相关方法 (保持不变)
    @Select("<script>" +
            "SELECT COALESCE(todo_type, '未分类') as type, COUNT(*) as count " +
            "FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            "GROUP BY COALESCE(todo_type, '未分类')" +
            "</script>")
    List<Map<String, Object>> selectTodoStatsByType(Map<String, Object> params);

    @Select("<script>" +
            "SELECT COALESCE(status, '0') as status, COUNT(*) as count " +
            "FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            "GROUP BY COALESCE(status, '0')" +
            "</script>")
    List<Map<String, Object>> selectTodoStatsByStatus(Map<String, Object> params);

    @Select("<script>" +
            "SELECT COALESCE(priority, '0') as priority, COUNT(*) as count " +
            "FROM class_todo " +
            "WHERE user_id = #{userId} " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            "GROUP BY COALESCE(priority, '0')" +
            "</script>")
    List<Map<String, Object>> selectTodoStatsByPriority(Map<String, Object> params);
}