package com.ruoyi.proj_lwj.domain;

public class ClassStudent {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String courseCode; // 旧模型字段：如果迁移完成，可考虑删除；当前为兼容保留
    private Long sessionId;   // 旧模型字段：课堂与学生正式通过 class_session_student 关联

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
