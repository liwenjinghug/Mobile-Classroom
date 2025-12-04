const api = require('../../../utils/api');
let timer = null;
Page({
  data: { exam: { questions: [] }, answers: {}, remaining: 0, remainingDisplay: '', loading: true, antiCheatEnabled: false, violations: 0, lastViolationAt: 0 },
  onLoad(options) {
    const examId = options && options.examId ? options.examId : null;
    if (!examId) { wx.showToast({ title: '考试缺失ID', icon: 'none' }); return; }
    this.examId = examId;
    this.participant = null;
    try { if (wx.hideShareMenu) wx.hideShareMenu(); } catch(e) {}
    this.loadAndStartExam(examId);
  },
  onShow() {
    // no-op; violation is recorded onHide
  },
  onHide() {
    // 记录一次违规（切出页面/进入后台）——在提交过程中或提交结束后不再计违规
    if (this.submitting || this.examEnded) return;
    if (this.data.antiCheatEnabled) {
      const now = Date.now();
      if (now - (this.data.lastViolationAt || 0) > 1500) {
        this.setData({ lastViolationAt: now });
        this.handleViolation('检测到切出考试页面/进入后台');
      }
    }
  },
  onUnload() {
    if (timer) clearInterval(timer);
    timer = null;
    try { if (wx.disableAlertBeforeUnload) wx.disableAlertBeforeUnload(); } catch(e) {}
  },

  // Combined flow: start participant, load exam meta, load questions, load answers
  loadAndStartExam(examId) {
    const that = this;
    that.setData({ loading: true });

    // 1) Ensure participant record exists (server will create or return existing)
    api.startExamParticipant({ examId: Number(examId) })
      .then(part => {
        that.participant = part || null;
        // 2) load exam detail metadata
        return api.getExamDetail(examId);
      })
      .then(examMeta => {
        // set minimal exam info now; questions will be loaded next
        const exam = examMeta || { id: examId, title: '考试 ' + examId, durationSeconds: 0, questions: [] };
        // 防作弊开关
        const antiCheatEnabled = !!(exam && (exam.antiCheat === 1 || exam.anti_cheat === 1));
        that.setData({ antiCheatEnabled });
        try {
          if (antiCheatEnabled && wx.enableAlertBeforeUnload) {
            wx.enableAlertBeforeUnload({ message: '考试进行中，离开将记违规，三次违规后将自动交卷' });
          }
        } catch(e) {}
        // prefer durationSeconds or examDuration or exam.duration or examTimeLimit
        let duration = exam.durationSeconds || exam.examDuration || exam.duration || exam.examTimeLimit || exam.timeLimit || 0;
        // 如果是分钟数，转换为秒
        if (duration > 0 && duration < 500) {
          // 假设小于500的是分钟数
          duration = duration * 60;
        }
        exam.durationSeconds = duration;
        console.log('[attempt] exam duration:', duration, 'seconds');
        that.setData({ exam, answers: {}, remaining: duration, remainingDisplay: that.formatTime(duration) });
        // 3) load questions list
        return api.listExamQuestions(examId);
      })
      .then(qs => {
        const questions = Array.isArray(qs) ? qs : [];
        // normalize question fields to front-end shape: id, type, content, options
        const normalized = questions.map(q => {
          const id = q.id || q.questionId || q.question_id;
          const rawType = q.questionType || q.question_type || q.type;
          // 新编码映射：1=判断 2=选择 3=简答
          let typeStr = '';
          if (rawType === 1 || String(rawType) === '1') typeStr = 'judge'; // 判断题
          else if (rawType === 2 || String(rawType) === '2') typeStr = 'single'; // 选择题
          else if (rawType === 3 || String(rawType) === '3') typeStr = 'text'; // 简答题
          else typeStr = (typeof rawType === 'string' ? rawType : String(rawType)) || 'text';

          // normalize options into objects {id, text}
          let options = [];
          try {
            // 判断题特殊处理：自动添加"正确/错误"选项
            if (typeStr === 'judge') {
              options = [
                { id: '正确', text: '正确' },
                { id: '错误', text: '错误' }
              ];
            } else {
              let rawOptions = [];
              if (q.questionOptions) {
                rawOptions = typeof q.questionOptions === 'string' ? JSON.parse(q.questionOptions || '[]') : q.questionOptions;
              } else if (q.options) {
                rawOptions = q.options;
              }
              if (Array.isArray(rawOptions)) {
                // if array of strings, map to {id: index, text}
                options = rawOptions.map((opt, idx) => {
                  if (opt && typeof opt === 'object') {
                    // if object like {id,text}
                    return { id: opt.id || String(idx), text: opt.text || opt.label || String(opt) };
                  }
                  return { id: String(idx), text: String(opt) };
                });
              }
            }
          } catch (e) {
            console.error('parse options error', e);
            if (typeStr === 'judge') {
              options = [{ id: '正确', text: '正确' }, { id: '错误', text: '错误' }];
            } else {
              options = [];
            }
          }

          return { id, type: typeStr, content: q.questionContent || q.content || q.question_content || q.title || '', options };
        });
        const exam = Object.assign({}, that.data.exam, { questions: normalized });
        that.setData({ exam });
        // 4) load existing answers for this exam and current student
        return api.listAnswers({ examId: Number(examId) });
      })
      .then(ansList => {
        const answers = {};
        if (Array.isArray(ansList)) {
          for (const a of ansList) {
            const qid = a.questionId || a.question_id || a.questionIdStr || a.questionid;
            // use stored studentAnswer or student_answer
            const val = a.studentAnswer || a.student_answer || a.studentAnswerSnapshot || a.answer || null;
            if (typeof val !== 'undefined' && val !== null) answers[qid] = val;
          }
        }
        that.setData({ answers, loading: false });
        // start timer only after we loaded duration and questions
        that.startTimer();
      })
      .catch(err => {
        console.error('loadAndStartExam error', err);
        // 如果是权限错误（403），可能是答案列表接口的问题，但不影响答题
        if (err && err.code === 403) {
          console.warn('答案列表加载失败（权限问题），继续答题流程');
          that.setData({ loading: false, answers: {} });
          that.startTimer();
        } else {
          that.setData({ loading: false });
          const msg = (err && (err.msg || err.message)) ? (err.msg || err.message) : '加载考试失败';
          wx.showToast({ title: msg, icon: 'none' });
        }
      });
  },

  startTimer() {
    const that = this;
    // compute remaining from exam.durationSeconds if not set
    let duration = (that.data.remaining && that.data.remaining > 0) ? that.data.remaining : (that.data.exam && that.data.exam.durationSeconds ? that.data.exam.durationSeconds : 0);

    // 如果时长为0，设置一个默认值（如30分钟）
    if (!duration || duration <= 0) {
      console.warn('[startTimer] duration is 0, setting default 30 minutes');
      duration = 30 * 60; // 30分钟
    }

    console.log('[startTimer] starting timer with duration:', duration, 'seconds');
    that.setData({ remaining: duration, remainingDisplay: that.formatTime(duration) });

    if (timer) clearInterval(timer);
    timer = setInterval(() => {
      let r = that.data.remaining - 1;
      if (r < 0) r = 0;
      that.setData({ remaining: r, remainingDisplay: that.formatTime(r) });
      if (r === 0) {
        clearInterval(timer);
        timer = null;
        wx.showToast({ title: '时间到，正在提交', icon: 'none' });
        that.submitExam();
      }
      // autosave every tick
      try { wx.setStorageSync('exam_auto_' + that.examId, { updatedAt: Date.now(), answers: that.data.answers }); } catch (e) {}
    }, 1000);
  },

  formatTime(sec) {
    if (!sec || sec <= 0) return '00:00';
    const m = Math.floor(sec / 60); const s = sec % 60; return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
  },

  onSingleChange(e) {
    const qid = e.currentTarget.dataset.qid; const val = e.detail.value;
    const answers = Object.assign({}, this.data.answers, { [qid]: val });
    this.setData({ answers });
  },
  onMultiChange(e) {
    const qid = e.currentTarget.dataset.qid;
    const checkedValues = e.detail.value || [];
    const answers = Object.assign({}, this.data.answers, { [qid]: checkedValues });
    this.setData({ answers });
  },
  onTextInput(e) {
    const qid = e.currentTarget.dataset.qid; const val = e.detail.value;
    const answers = Object.assign({}, this.data.answers, { [qid]: val });
    this.setData({ answers });
  },
  onTextBlur(e) {
    const qid = e.currentTarget.dataset.qid; const val = e.detail.value;
    const answers = Object.assign({}, this.data.answers, { [qid]: val });
    this.setData({ answers });
  },


  // 顶层长按捕获：作为一次违规（无法完全屏蔽复制，但可提示与计数）
  onLongPress() {
    if (!this.data.antiCheatEnabled) return;
    const now = Date.now();
    if (now - (this.data.lastViolationAt || 0) <= 1500) return;
    this.setData({ lastViolationAt: now });
    this.handleViolation('检测到长按操作');
  },

  handleViolation(reason) {
    // 在提交过程中或考试结束后不再提示违规
    if (this.submitting || this.examEnded) return;
    if (!this.data.antiCheatEnabled) return;
    const count = (this.data.violations || 0) + 1;
    this.setData({ violations: count });
    const left = Math.max(0, 3 - count);
    if (count < 3) {
      wx.showToast({ title: `防作弊警告(${count}/3)：${reason}。再违规${left}次将自动交卷`, icon: 'none', duration: 2500 });
    } else {
      wx.showToast({ title: '多次违规，正在自动交卷', icon: 'none', duration: 1500 });
      // 提交试卷
      setTimeout(() => this.submitExam(), 800);
    }
  },

  submitExam() {
    const that = this;
    if (this.submitting) return;
    this.submitting = true;
    // 提交阶段关闭防作弊提示，避免多次违规弹框
    this.setData({ antiCheatEnabled: false });
    try { if (wx.disableAlertBeforeUnload) wx.disableAlertBeforeUnload(); } catch(e) {}
    wx.showLoading({ title: '提交中...' });

    const payload = { answers: [], durationSeconds: (this.data.exam.durationSeconds - this.data.remaining) };
    for (const q of (this.data.exam.questions || [])) {
      payload.answers.push({ questionId: q.id, answer: this.data.answers[q.id] || null });
    }

    // Call updated API (participant/submit)
    api.submitExam(this.examId, payload).then(res => {
      wx.hideLoading();
      that.submitting = false;
      that.examEnded = true;
      try { if (wx.disableAlertBeforeUnload) wx.disableAlertBeforeUnload(); } catch(e) {}

      // 根据返回结果决定提示内容
      let title = '提交成功';
      let content = '已提交';

      if (res) {
        if (res.hasUnscoredSubjective) {
          content = '已提交，等待批改';
        } else if (res.totalScore !== null && res.totalScore !== undefined) {
          content = '得分：' + res.totalScore + '分';
        }
      }

      wx.showModal({
        title: title,
        content: content,
        showCancel: false,
        success() {
          wx.navigateBack({ delta: 1 });
        }
      });
    }).catch(err => {
      wx.hideLoading();
      that.submitting = false;
      that.examEnded = true;
      try { if (wx.disableAlertBeforeUnload) wx.disableAlertBeforeUnload(); } catch(e) {}

      // 错误情况下也标记考试结束，避免返回过程出现违规弹框
      that.examEnded = true;
      console.error('submitExam API error', err);

      if (err && (err.code === 500 || err.code === '500') && err.msg && err.msg.indexOf('已提交') !== -1) {
        if (timer) clearInterval(timer);
        timer = null;
        try { wx.removeStorageSync('exam_auto_' + that.examId); } catch (e) {}
        wx.showModal({
          title: '提示',
          content: '该考试已提交，请等待批改',
          showCancel: false,
          success() {
            wx.navigateBack({ delta: 1 });
          }
        });
        return;
      }

      if (err && err.code === 404) {
        wx.showModal({ title: '提交失败', content: (err.msg || err.message || '未开始考试或记录不存在') + '\n请返回考试列表并重新进入以尝试创建参与记录。', showCancel: false });
        return;
      }

      wx.showToast({ title: (err && (err.msg || err.message)) ? (err.msg || err.message) : '提交失败', icon: 'none' });
    });
  }
});
