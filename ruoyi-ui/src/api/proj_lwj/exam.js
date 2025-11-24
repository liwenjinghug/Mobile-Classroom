import request from '@/utils/request'

// 查询考试列表
export function listExam(query) {
  return request({
    url: '/proj_lwj/exam/list',
    method: 'get',
    params: query
  })
}

// 新增考试
export function addExam(data) {
  return request({
    url: '/proj_lwj/exam',
    method: 'post',
    data: data
  })
}

// 修改考试
export function updateExam(data) {
  return request({
    url: '/proj_lwj/exam',
    method: 'put',
    data: data
  })
}

// 删除考试（支持批量，ids 可以是单个 id 或 id 数组）
export function delExam(ids) {
  if (Array.isArray(ids)) {
    return request({
      url: '/proj_lwj/exam/' + ids.join(','),
      method: 'delete'
    })
  }
  return request({
    url: '/proj_lwj/exam/' + ids,
    method: 'delete'
  })
}

// 发布考试（状态流转）
export function publishExam(id) {
  return request({
    url: `/proj_lwj/exam/status/${id}/1`,
    method: 'put'
  })
}

// 开始考试
export function startExam(id) {
  return request({
    url: `/proj_lwj/exam/status/${id}/2`,
    method: 'put'
  })
}

// 结束考试
export function endExam(id) {
  return request({
    url: `/proj_lwj/exam/status/${id}/3`,
    method: 'put'
  })
}

// 批量创建考试到多个课堂
export function batchAddExam(payload) {
  const { sessionIds = [], exam = {} } = payload || {}
  if (!Array.isArray(sessionIds) || sessionIds.length === 0) {
    return Promise.reject(new Error('缺少课堂列表'))
  }
  const normalizedExam = normalizeExamPayload(exam)
  const batchPayload = { sessionIds, exam: normalizedExam }
  // 若后端已支持 /proj_lwj/exam/batch 则尝试；失败后自动降级
  return request({ url: '/proj_lwj/exam/batch', method: 'post', data: batchPayload })
    .then(res => {
      if (res && (res.code === 200 || res.code === 0)) {
        return res
      }
      // 不是成功码，降级
      return clientSideBatchCreate(sessionIds, normalizedExam)
    })
    .catch(() => clientSideBatchCreate(sessionIds, normalizedExam))
}

function normalizeExamPayload(examTemplate){
  const copy = { ...examTemplate }
  // 后端期望整数型 0/1，而前端表单使用 boolean
  if (typeof copy.antiCheat === 'boolean') copy.antiCheat = copy.antiCheat ? 1 : 0
  if (typeof copy.autoSubmit === 'boolean') copy.autoSubmit = copy.autoSubmit ? 1 : 0
  if (typeof copy.lateSubmit === 'boolean') copy.lateSubmit = copy.lateSubmit ? 1 : 0
  // 容错：如果 showAnswer 未设置默认 0
  if (copy.showAnswer == null) copy.showAnswer = 0
  // 数值字段确保为数字
  ;['examType','examDuration','totalScore','passScore','examMode','questionOrder','showAnswer','status'].forEach(k=>{
    if (copy[k] != null) copy[k] = Number(copy[k])
  })
  return copy
}

function clientSideBatchCreate(sessionIds, examTemplate) {
  const base = normalizeExamPayload(examTemplate)
  const tasks = sessionIds.map(sid => {
    const one = { ...base, sessionId: sid, status: 0 }
    return addExam(one)
      .then(r => ({ sid, ok: r && (r.code === 200 || r.code === 0), res: r }))
      .catch(e => ({ sid, ok: false, error: e }))
  })
  return Promise.all(tasks).then(results => {
    const success = results.filter(r => r.ok).map(r => r.sid)
    const failed = results.filter(r => !r.ok).map(r => r.sid)
    return {
      code: success.length ? 200 : 500,
      msg: failed.length ? `部分创建失败: 成功 ${success.length} / 失败 ${failed.length}` : '批量创建成功',
      success,
      failed,
      details: results
    }
  })
}

// 查询指定学生可参加的考试列表（原函数）
export function listAvailableExam(studentNo) {
  return request({
    url: '/proj_lwj/exam/available',
    method: 'get',
    params: { studentNo }
  })
}
// 兼容前端调用的复数命名与对象参数形式
export function listAvailableExams(arg) {
  if (typeof arg === 'string') return listAvailableExam(arg)
  if (arg && typeof arg === 'object') {
    const studentNo = arg.studentNo || arg.student_no || arg.sn
    return listAvailableExam(studentNo)
  }
  return listAvailableExam(arg)
}

// 学生开始考试（生成参与记录），与状态流转 startExam 区分
export function startExamParticipant(data) {
  // 后端 /proj_lwj/exam/start 支持 JSON body 解析 examId 和 studentNo
  return request({
    url: '/proj_lwj/exam/start',
    method: 'post',
    data
  })
}

// 查询指定考试详情
export function getExam(id) {
  return request({
    url: '/proj_lwj/exam/' + id,
    method: 'get'
  })
}

// 获取考试题目列表
export function listQuestions(query) {
  return request({
    url: '/proj_lwj/exam/question/list',
    method: 'get',
    params: query
  })
}
// 保存答题
export function saveAnswer(data) {
  return request({
    url: '/proj_lwj/exam/answer/save',
    method: 'post',
    data
  })
}
// 提交试卷（参与记录提交）
export function submitExam(data) {
  return request({
    url: '/proj_lwj/exam/participant/submit',
    method: 'post',
    data
  })
}

// 获取答案列表
export function listAnswers(query) {
  return request({
    url: '/proj_lwj/exam/answer/list',
    method: 'get',
    params: query
  })
}

// 新增：按学号查询"我的考试参与记录"
export function listMyParticipants(arg) {
  const params = {}
  if (typeof arg === 'string') params.studentNo = arg
  else if (arg && typeof arg === 'object') params.studentNo = arg.studentNo || arg.student_no || arg.sn
  else params.studentNo = arg
  return request({
    url: '/proj_lwj/exam/participant/list',
    method: 'get',
    params
  })
}

// 获取我的考试（含未批改主观题隐藏总分）
export function listMyExams(studentNo){
  return request({ url:'/proj_lwj/exam/my', method:'get', params:{ studentNo } })
}
// 获取题目统计与批改概览
export function getQuestionCorrectSummary(examId){
  return request({ url:`/proj_lwj/exam/${examId}/questionCorrectSummary`, method:'get' })
}
// 获取主观题未批改答案列表
export function listUngraded(examId){
  return request({ url:`/proj_lwj/exam/${examId}/ungraded`, method:'get' })
}
// 主观题单条批改
export function gradeAnswer(data){
  return request({ url:'/proj_lwj/exam/answer/grade', method:'post', data })
}
// 主观题批量批改
export function gradeAnswerBatch(list){
  return request({ url:'/proj_lwj/exam/answer/gradeBatch', method:'post', data:list })
}
