package com.ruoyi.proj_cyq.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_cyq.mapper.TodoMapper;
import com.ruoyi.proj_cyq.domain.Todo;
import com.ruoyi.proj_cyq.service.ITodoService;
import java.util.Date;

@Service
public class TodoServiceImpl implements ITodoService {
    @Autowired
    private TodoMapper todoMapper;

    @Override
    public int insertTodo(Todo todo) {
        // 设置用户信息和创建时间
        Long userId = SecurityUtils.getUserId();
        todo.setUserId(userId);
        todo.setCreateTime(new Date());
        todo.setCreateBy(SecurityUtils.getUsername());

        // 生成连续编号
        Integer maxSequenceNumber = todoMapper.selectMaxSequenceNumber(userId);
        int newSequenceNumber = maxSequenceNumber + 1;
        todo.setSequenceNumber(newSequenceNumber);

        // 设置默认值
        if (todo.getStatus() == null) {
            todo.setStatus("0");
        }
        if (todo.getPriority() == null) {
            todo.setPriority("0");
        }
        if (todo.getIsReminded() == null) {
            todo.setIsReminded("0");
        }
        if (todo.getMessageStatus() == null) {
            todo.setMessageStatus("0");
        }
        if (todo.getMessageRead() == null) {
            todo.setMessageRead("0");
        }

        // 设置提醒时间为结束时间前一天
        if (todo.getEndTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(todo.getEndTime());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            todo.setRemindTime(calendar.getTime());
        }

        System.out.println("🔔 插入待办事项");
        System.out.println("   用户ID: " + userId);
        System.out.println("   连续编号: " + newSequenceNumber);
        System.out.println("   标题: " + todo.getTitle());

        int result = todoMapper.insertTodo(todo);

        System.out.println("✅ 插入结果: " + result);
        System.out.println("🆔 获得的todoId: " + todo.getTodoId());
        System.out.println("🔢 连续编号: " + todo.getSequenceNumber());

        if (result > 0 && todo.getTodoId() == null) {
            System.err.println("❌ 警告：插入成功但todoId未正确返回");
            return 0;
        }

        return result;
    }

    @Override
    public Todo selectTodoById(Long todoId) {
        Todo todo = todoMapper.selectTodoById(todoId);
        if (todo == null) {
            throw new RuntimeException("待办事项不存在");
        }
        return todo;
    }

    @Override
    public List<Todo> selectTodoList(Todo todo) {
        if (todo.getUserId() == null) {
            todo.setUserId(SecurityUtils.getUserId());
        }
        System.out.println("查询待办列表，用户ID: " + todo.getUserId());

        List<Todo> todoList = todoMapper.selectTodoList(todo);
        System.out.println("查询结果数量: " + todoList.size());

        for (Todo item : todoList) {
            System.out.println("待办项 - 连续编号: " + item.getSequenceNumber() +
                    ", ID: " + item.getTodoId() +
                    ", 标题: " + item.getTitle());
        }

        for (Todo item : todoList) {
            if (item.getTodoId() == null) {
                System.err.println("警告：发现todoId为null的数据，标题：" + item.getTitle());
            }
        }

        return todoList;
    }

    @Override
    public int updateTodo(Todo todo) {
        Todo existingTodo = todoMapper.selectTodoById(todo.getTodoId());
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }

        todo.setUpdateTime(new Date());
        todo.setUpdateBy(SecurityUtils.getUsername());

        if (todo.getEndTime() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(todo.getEndTime());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            todo.setRemindTime(calendar.getTime());
        }

        return todoMapper.updateTodo(todo);
    }

    @Override
    public int deleteTodoById(Long todoId) {
        Todo existingTodo = todoMapper.selectTodoById(todoId);
        if (existingTodo == null) {
            throw new RuntimeException("待办事项不存在");
        }

        Long currentUserId = SecurityUtils.getUserId();
        if (!existingTodo.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权删除他人的待办事项");
        }

        return todoMapper.deleteTodoById(todoId);
    }

    @Override
    public int deleteTodoByIds(Long[] todoIds) {
        if (todoIds == null || todoIds.length == 0) {
            return 0;
        }

        Long currentUserId = SecurityUtils.getUserId();
        int successCount = 0;

        for (Long todoId : todoIds) {
            try {
                Todo existingTodo = todoMapper.selectTodoById(todoId);
                if (existingTodo != null && existingTodo.getUserId().equals(currentUserId)) {
                    int result = todoMapper.deleteTodoById(todoId);
                    if (result > 0) {
                        successCount++;
                    }
                }
            } catch (Exception e) {
                System.err.println("删除待办事项失败，ID: " + todoId + ", 错误: " + e.getMessage());
            }
        }

        return successCount;
    }

    @Override
    public Map<String, Object> getTodoStats(Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        params.put("userId", userId);

        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> typeStats = todoMapper.selectTodoStatsByType(params);
            result.put("typeStats", typeStats);

            List<Map<String, Object>> statusStats = todoMapper.selectTodoStatsByStatus(params);
            result.put("statusStats", statusStats);

            List<Map<String, Object>> priorityStats = todoMapper.selectTodoStatsByPriority(params);
            result.put("priorityStats", priorityStats);

            int totalCount = 0;
            for (Map<String, Object> stat : typeStats) {
                Object countObj = stat.get("count");
                if (countObj != null) {
                    totalCount += Integer.parseInt(countObj.toString());
                }
            }
            result.put("totalCount", totalCount);
        } catch (Exception e) {
            System.err.println("统计查询出错: " + e.getMessage());
            e.printStackTrace();
            result.put("typeStats", new ArrayList<>());
            result.put("statusStats", new ArrayList<>());
            result.put("priorityStats", new ArrayList<>());
            result.put("totalCount", 0);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTodoStatsDetail(Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        params.put("userId", userId);

        List<Map<String, Object>> detailList = new ArrayList<>();

        try {
            List<Map<String, Object>> typeStats = todoMapper.selectTodoStatsByType(params);
            for (Map<String, Object> stat : typeStats) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("category", "类型");

                Object typeObj = stat.get("type");
                String type = typeObj != null ? typeObj.toString() : "未分类";
                detail.put("name", getTypeText(type));

                Object countObj = stat.get("count");
                Integer value = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
                detail.put("value", value);

                detailList.add(detail);
            }

            List<Map<String, Object>> statusStats = todoMapper.selectTodoStatsByStatus(params);
            for (Map<String, Object> stat : statusStats) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("category", "状态");

                Object statusObj = stat.get("status");
                String status = statusObj != null ? statusObj.toString() : "0";
                detail.put("name", getStatusText(status));

                Object countObj = stat.get("count");
                Integer value = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
                detail.put("value", value);

                detailList.add(detail);
            }

            List<Map<String, Object>> priorityStats = todoMapper.selectTodoStatsByPriority(params);
            for (Map<String, Object> stat : priorityStats) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("category", "优先级");

                Object priorityObj = stat.get("priority");
                String priority = priorityObj != null ? priorityObj.toString() : "0";
                detail.put("name", getPriorityText(priority));

                Object countObj = stat.get("count");
                Integer value = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
                detail.put("value", value);

                detailList.add(detail);
            }
        } catch (Exception e) {
            System.err.println("统计详情查询出错: " + e.getMessage());
            e.printStackTrace();
        }

        return detailList;
    }

    private String getTypeText(String type) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("study", "学习");
        typeMap.put("work", "工作");
        typeMap.put("life", "生活");
        typeMap.put("other", "其他");
        typeMap.put("未分类", "未分类");
        return typeMap.getOrDefault(type, type);
    }

    private String getStatusText(String status) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("0", "未完成");
        statusMap.put("1", "完成");
        statusMap.put("2", "过期");
        statusMap.put("未分类", "未分类");
        return statusMap.getOrDefault(status, status);
    }

    private String getPriorityText(String priority) {
        Map<String, String> priorityMap = new HashMap<>();
        priorityMap.put("0", "低");
        priorityMap.put("1", "中");
        priorityMap.put("2", "高");
        priorityMap.put("未分类", "未分类");
        return priorityMap.getOrDefault(priority, priority);
    }
}