package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** 考试与课堂关联，多课堂发布支持 */
public class ClassExamSession extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long examId;
    private Long sessionId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}

