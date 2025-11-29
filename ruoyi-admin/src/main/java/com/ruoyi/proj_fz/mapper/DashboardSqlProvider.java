package com.ruoyi.proj_fz.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class DashboardSqlProvider {

    public String getHomeworkDetailsSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("h.homework_id as homeworkId, h.title, c.course_name as course, h.create_time as publishTime");
            SELECT("h.deadline as deadline");
            SELECT("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.status IN ('1','2','3')) as submittedCount");
            SELECT("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.status = '0') as pendingCount");
            SELECT("CASE WHEN h.deadline < NOW() THEN '已过期' ELSE '进行中' END as status");
            FROM("class_homework h");
            LEFT_OUTER_JOIN("class_course c ON h.course_id = c.course_id");
            WHERE("h.status = '0'");

            if (params.get("title") != null && !params.get("title").toString().isEmpty()) {
                WHERE("h.title LIKE CONCAT('%', #{title}, '%')");
            }
            if (params.get("course") != null && !params.get("course").toString().isEmpty()) {
                WHERE("c.course_name LIKE CONCAT('%', #{course}, '%')");
            }
            if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
                WHERE("DATE(h.create_time) >= #{startTime}");
            }
            if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
                WHERE("DATE(h.create_time) <= #{endTime}");
            }
            if (params.get("deadlineStart") != null && !params.get("deadlineStart").toString().isEmpty()) {
                WHERE("DATE(h.deadline) >= #{deadlineStart}");
            }
            if (params.get("deadlineEnd") != null && !params.get("deadlineEnd").toString().isEmpty()) {
                WHERE("DATE(h.deadline) <= #{deadlineEnd}");
            }
            if (params.get("expireStatus") != null && !params.get("expireStatus").toString().isEmpty()) {
                WHERE("CASE WHEN h.deadline < NOW() THEN '已过期' ELSE '进行中' END = #{expireStatus}");
            }
            if (params.get("gradingStatus") != null && !params.get("gradingStatus").toString().isEmpty()) {
                String gradingStatus = params.get("gradingStatus").toString();
                if ("已批改".equals(gradingStatus)) {
                    WHERE("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.is_graded = 1) > 0");
                } else if ("未批改".equals(gradingStatus)) {
                    WHERE("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.is_graded = 1) = 0");
                }
            }
            if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
                String status = params.get("status").toString();
                if ("pending".equals(status)) {
                    WHERE("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.status = '0') > 0");
                } else if ("submitted".equals(status)) {
                    WHERE("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.status IN ('1','2','3')) > 0");
                } else if ("graded".equals(status)) {
                    WHERE("(SELECT COUNT(*) FROM class_student_homework sh WHERE sh.homework_id = h.homework_id AND sh.is_graded = 1) > 0");
                }
            }

            String sortField = params.get("sortField") != null ? params.get("sortField").toString() : "deadline";
            String sortOrder = params.get("sortOrder") != null ? params.get("sortOrder").toString() : "asc";
            ORDER_BY(sortField + " " + sortOrder);
        }}.toString();
    }

    public String getNoticesSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("a.id, a.title, a.author, a.create_time as createTime, a.content");
            FROM("class_article a");
            WHERE("a.status = 'published'");

            if (params.get("title") != null && !params.get("title").toString().isEmpty()) {
                WHERE("a.title LIKE CONCAT('%', #{title}, '%')");
            }
            if (params.get("author") != null && !params.get("author").toString().isEmpty()) {
                WHERE("a.author LIKE CONCAT('%', #{author}, '%')");
            }
            if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
                WHERE("DATE(a.create_time) >= #{startTime}");
            }
            if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
                WHERE("DATE(a.create_time) <= #{endTime}");
            }
            ORDER_BY("a.create_time DESC");
        }}.toString() + " LIMIT 50";
    }

    public String getOperationLogsSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("oper_id as id, title, oper_name as operator, oper_time as operateTime, oper_ip as ip, business_type as businessType");
            FROM("sys_oper_log");
            WHERE("1=1");

            if (params.get("businessType") != null && !params.get("businessType").toString().isEmpty()) {
                WHERE("business_type = #{businessType}");
            }
            if (params.get("operator") != null && !params.get("operator").toString().isEmpty()) {
                WHERE("oper_name LIKE CONCAT('%', #{operator}, '%')");
            }
            if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
                WHERE("DATE(oper_time) >= #{startTime}");
            }
            if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
                WHERE("DATE(oper_time) <= #{endTime}");
            }
            ORDER_BY("oper_time DESC");
        }}.toString() + " LIMIT 30";
    }
}