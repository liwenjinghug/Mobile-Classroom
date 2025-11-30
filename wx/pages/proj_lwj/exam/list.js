const api = require('../../../utils/api');

function formatDateMillis(millis) {
  try {
    if (!millis) return '';
    const d = new Date(Number(millis));
    if (isNaN(d.getTime())) return '';
    return d.getFullYear() + '-' + (d.getMonth() + 1).toString().padStart(2, '0') + '-' + d.getDate().toString().padStart(2, '0') + ' ' + d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0');
  } catch (e) { return '' }
}

function parseDateValue(v) {
  if (!v && v !== 0) return 0;
  if (typeof v === 'number') return v;
  if (typeof v === 'string') {
    if (/^\d+$/.test(v)) return Number(v);
    let t = Date.parse(v);
    if (!isNaN(t)) return t;
    try {
      const v2 = v.replace(' ', 'T');
      t = Date.parse(v2);
      if (!isNaN(t)) return t;
    } catch (e) {}
    try {
      const v3 = v.replace(/-/g, '/').replace(' ', 'T');
      t = Date.parse(v3);
      if (!isNaN(t)) return t;
    } catch (e) {}
    return 0;
  }
  return 0;
}

function pickFirst(obj, keys) {
  for (const k of keys) {
    if (!obj) continue;
    if (Object.prototype.hasOwnProperty.call(obj, k) && typeof obj[k] !== 'undefined' && obj[k] !== null) return obj[k];
  }
  return undefined;
}

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return '';

  let totalSeconds = seconds;

  // 如果数值较小（<500），可能是分钟数，需要转换
  if (totalSeconds < 500) {
    totalSeconds = totalSeconds * 60; // 转换为秒
  }

  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);

  if (hours > 0) {
    return hours + '小时' + (minutes > 0 ? minutes + '分钟' : '');
  }
  return minutes + '分钟';
}

Page({
  data: {
    sessions: [],
    filteredSessions: [],
    currentFilter: 'pending', // 默认显示待完成
    loading: true,
    errorMessage: '',
    errorRaw: null
  },

  onLoad(options) {
    const sessionId = options && options.sessionId ? options.sessionId : null;
    this.sessionId = sessionId;
    this.loadSessions(sessionId);
  },

  onShow() {
    // 当页面显示时（如从答题页面返回），重新加载数据以更新状态
    if (this.sessionId) {
      console.log('[list] onShow - reloading sessions');
      this.loadSessions(this.sessionId);
    }
  },

  loadSessions(sessionId) {
    const that = this;
    that.setData({ loading: true, errorMessage: '' });

    // 同时获取两个数据源
    Promise.all([
      api.getExamSessions(sessionId),    // 所有考试
      api.getMyExamRecords(sessionId)    // 我的考试记录
    ]).then(([allExams, myRecords]) => {
      const arr = (allExams && allExams.length) ? allExams : [];
      const records = (myRecords && myRecords.length) ? myRecords : [];

      // 创建考试记录映射表（examId -> record）
      const recordMap = {};
      for (const record of records) {
        const examId = record.examId || record.exam_id || record.id;
        recordMap[examId] = record;
      }

      console.log('[list] all exams:', arr.length, 'my records:', records.length);

      const now = Date.now();

      const sessions = arr.map(raw => {
        const id = pickFirst(raw, ['id', 'examId', 'exam_id', 'examIdStr', 'exam_id_str', 'examID']);

        // 查找对应的参与记录
        const myRecord = recordMap[id];

        let title = pickFirst(raw, ['title', 'name', 'examName', 'exam_name', 'exam_title', 'examTitle']) || '';

        if (!title) {
          title = pickFirst(raw, ['courseName', 'course_name', 'sessionName', 'session_name', 'subject', 'topic']) || '';
        }
        if (!title) {
          try {
            title = id ? ('考试 ' + id) : (JSON.stringify(raw).slice(0, 30) + '...');
          } catch (e) {
            title = '考试';
          }
        }

        const startRaw = pickFirst(raw, ['startAt', 'startAtMillis', 'startTime', 'start_time', 'start', 'beginTime', 'startDate', 'start_date', 'publishTime', 'publish_time', 'createTime', 'create_time']);
        const endRaw = pickFirst(raw, ['endAt', 'endAtMillis', 'endTime', 'end_time', 'end', 'deadline', 'dueTime', 'endDate', 'end_date']);
        const start = parseDateValue(startRaw);
        const end = parseDateValue(endRaw);

        let questionTypes = pickFirst(raw, ['questionTypes', 'types', 'examType', 'exam_type', 'questionTypeNames', 'question_type']) || '';
        if (!questionTypes) {
          questionTypes = pickFirst(raw, ['questionType', 'question_type_names', 'qtypes']) || '';
        }

        // 从考试记录中获取状态信息
        let submitted = false;
        let graded = false;
        let score = null;
        let totalScore = pickFirst(raw, ['totalScore', 'total_score', 'fullScore', 'full_score', 'maxScore', 'max_score']);

        // 时长获取 - 增强字段兼容性
        let duration = pickFirst(raw, [
          'duration', 'durationSeconds', 'duration_seconds',
          'examDuration', 'exam_duration',
          'timeLimit', 'time_limit',
          'examTimeLimit', 'exam_time_limit',
          'limitTime', 'limit_time'
        ]);

        // 时长单位转换
        if (duration) {
          // 如果是字符串，尝试转换为数字
          if (typeof duration === 'string') {
            duration = parseInt(duration) || 0;
          }
          // 如果小于500，假设是分钟，转换为秒用于显示
          // 但保持原值用于formatDuration显示
          console.log('[list] raw duration for exam', id, ':', duration);
        }

        let hasEssayQuestion = false;

        if (myRecord) {
          // 有参与记录，说明已参加过
          submitted = true;

          // 使用 correctStatus 判断是否已批改（0=待批改，1=已批改）
          const correctStatus = pickFirst(myRecord, ['correctStatus', 'correct_status']);
          graded = correctStatus === 1 || correctStatus === '1';

          score = pickFirst(myRecord, ['score', 'totalScore', 'mark', 'grade', 'result', 'studentScore', 'student_score']);
          hasEssayQuestion = pickFirst(myRecord, ['hasEssayQuestion', 'has_essay_question', 'hasSubjective', 'needManualGrade']) || false;

          console.log('[list] exam', id, 'has record, correctStatus:', correctStatus, 'graded:', graded, 'score:', score);
        } else {
          console.log('[list] exam', id, 'no record - pending');
        }

        // 优化状态判断逻辑
        let status = '';
        let statusText = '';
        let statusTag = '';
        let buttonLabel = '';
        let buttonDisabled = false;
        let isGraded = false;
        let showScore = false;

        if (myRecord && submitted) {
          // 有参与记录 - 已完成状态
          if (graded && score !== null && score !== undefined) {
            // 已批改 - 禁用按钮
            status = 'completed';
            statusText = '已完成';
            statusTag = 'graded';
            buttonLabel = '已批改';
            buttonDisabled = true; // 禁用按钮
            isGraded = true;
            showScore = true;
          } else {
            // 待批改
            status = 'completed';
            statusText = '已完成';
            statusTag = 'submitted';
            buttonLabel = '待批改';
            buttonDisabled = true;
            isGraded = false;
            showScore = false;
          }
        } else {
          // 无参与记录 - 待完成状态
          if (now > end && end > 0) {
            // 已截止但未提交
            status = 'pending';
            statusText = '已截止';
            statusTag = 'expired';
            buttonLabel = '已截止';
            buttonDisabled = true;
          } else if (now < start && start > 0) {
            // 未开始
            status = 'pending';
            statusText = '未开始';
            statusTag = 'upcoming';
            buttonLabel = '未开始';
            buttonDisabled = true;
          } else {
            // 待完成（进行中）
            status = 'pending';
            statusText = '待完成';
            statusTag = 'pending';
            buttonLabel = '开始考试';
            buttonDisabled = false;
          }
        }

        // 判断是否及格
        let isPassed = false;
        let passScore = null;

        if (myRecord) {
          // 从参与记录中获取及格分数
          passScore = pickFirst(myRecord, ['passScore', 'pass_score']);
        }

        // 如果记录中没有，尝试从考试信息中获取
        if (!passScore) {
          passScore = pickFirst(raw, ['passScore', 'pass_score']);
        }

        // 判断是否及格
        if (showScore && score !== null && score !== undefined) {
          if (passScore !== null && passScore !== undefined) {
            // 使用配置的及格分数
            isPassed = score >= passScore;
          } else if (totalScore) {
            // 如果没有配置及格分数，使用默认60%
            const passRate = 0.6;
            isPassed = score >= (totalScore * passRate);
          }
        }

        return Object.assign({}, raw, {
          id,
          title,
          status,
          statusText,
          statusTag,
          startAt: start,
          endAt: end,
          startAtDisplay: start ? formatDateMillis(start) : '',
          endAtDisplay: end ? formatDateMillis(end) : '',
          durationDisplay: duration ? formatDuration(duration) : '',
          questionTypes: questionTypes,
          buttonLabel,
          buttonDisabled,
          score: showScore ? score : null,
          totalScore: totalScore,
          isGraded: isGraded,
          hasEssayQuestion: hasEssayQuestion,
          hasRecord: !!myRecord,
          isPassed: isPassed,  // 是否及格
          passScore: passScore // 及格分数
        });
      });

      // 按发布时间排序（先发布的在上面）
      sessions.sort((a, b) => {
        const timeA = a.startAt || 0;
        const timeB = b.startAt || 0;
        return timeA - timeB;
      });

      that.setData({ sessions });
      that.applyFilter();
      that.setData({ loading: false });
    }).catch(err => {
      console.error('loadSessions error', err);
      const short = err && (err.message || err.msg || (err._error && err._error.message)) ? (err.message || err.msg || err._error.message) : (err && err.statusCode ? 'HTTP ' + err.statusCode : '未知错误');
      that.setData({ sessions: [], filteredSessions: [], loading: false, errorMessage: short, errorRaw: err });
    });
  },

  applyFilter() {
    const filter = this.data.currentFilter;
    const sessions = this.data.sessions;
    let filtered = [];

    console.log('[applyFilter] current filter:', filter, 'total sessions:', sessions.length);

    if (filter === 'all') {
      filtered = sessions;
    } else if (filter === 'pending') {
      // 待完成：无参与记录的考试
      filtered = sessions.filter(s => {
        const isPending = !s.hasRecord;
        if (!isPending) {
          console.log('[applyFilter] exam', s.id, 'has record, excluded from pending');
        }
        return isPending;
      });
    } else if (filter === 'completed') {
      // 已完成：有参与记录的考试
      filtered = sessions.filter(s => {
        const isCompleted = s.hasRecord;
        if (isCompleted) {
          console.log('[applyFilter] exam', s.id, 'has record, included in completed');
        }
        return isCompleted;
      });
    }

    console.log('[applyFilter] filtered result:', filtered.length, 'exams');
    this.setData({ filteredSessions: filtered });
  },

  onFilterChange(e) {
    const filter = e.currentTarget.dataset.filter;
    this.setData({ currentFilter: filter });
    this.applyFilter();
  },

  onRefresh() {
    this.loadSessions(this.sessionId);
  },

  showErrorDetails() {
    const raw = this.data.errorRaw;
    const detail = typeof raw === 'string' ? raw : JSON.stringify(raw, null, 2);
    wx.showModal({
      title: '错误详情',
      content: detail.length > 800 ? detail.substring(0, 800) + '...(截断)' : detail,
      showCancel: false
    });
  },

  openExam(e) {
    const id = String(e.currentTarget.dataset.id || '');
    const item = this.data.sessions.find(s => String(s.id) === id);
    if (!item) return;

    if (item.status === 'completed') {
      // 已完成的考试（已批改或待批改），不允许操作
      const msg = item.isGraded ? '考试已批改，分数：' + (item.score || 0) + '分' : '已提交，等待批改';
      wx.showToast({ title: msg, icon: 'none', duration: 2000 });
    } else if (!item.buttonDisabled && item.status === 'pending') {
      // 可以开始考试
      wx.navigateTo({ url: '/pages/proj_lwj/exam/attempt?examId=' + id });
    } else {
      const msg = item.statusText || '无法进入';
      wx.showToast({ title: msg, icon: 'none' });
    }
  }
});
