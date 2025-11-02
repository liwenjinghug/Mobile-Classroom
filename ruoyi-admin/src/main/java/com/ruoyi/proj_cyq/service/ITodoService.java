package com.ruoyi.proj_cyq.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.proj_cyq.domain.Todo;

public interface ITodoService {
    public Todo selectTodoById(Long todoId);
    public List<Todo> selectTodoList(Todo todo);
    public int insertTodo(Todo todo);
    public int updateTodo(Todo todo);
    public int deleteTodoById(Long todoId);
    public int deleteTodoByIds(Long[] todoIds);

    // 统计相关方法
    public Map<String, Object> getTodoStats(Map<String, Object> params);
    public List<Map<String, Object>> getTodoStatsDetail(Map<String, Object> params);
}