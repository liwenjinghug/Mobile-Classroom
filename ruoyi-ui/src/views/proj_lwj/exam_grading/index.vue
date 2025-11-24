<template>
  <div class="exam-grading-page">
    <el-card shadow="never" class="box-card">
      <!-- 筛选和操作区 -->
      <div class="filter-section">
        <el-form inline label-width="80px">
          <el-form-item label="选择课堂">
            <el-select v-model="currentSessionId" placeholder="请先选择课堂" filterable clearable style="width:300px" @change="onSessionChange">
              <el-option v-for="s in teacherSessions" :key="s.sessionId" :label="s.className" :value="s.sessionId" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择考试">
            <el-select v-model="currentExamId" placeholder="请选择考试" filterable clearable style="width:300px" @change="onExamChange" :disabled="!currentSessionId">
              <el-option v-for="ex in exams" :key="ex.id" :label="`${ex.examName} (${statusText(ex.status)})`" :value="ex.id">
                <span style="float:left">{{ ex.examName }}</span>
                <el-tag :type="statusTagType(ex.status)" size="mini" style="float:right;margin-left:8px">{{ statusText(ex.status) }}</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" icon="el-icon-refresh" @click="loadTeacherSessions" :loading="loadingExams">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 考试信息概览 -->
      <div v-if="currentExamId && questionStats" class="exam-overview">
        <el-descriptions :column="4" size="small" border>
          <el-descriptions-item label="考试名称">{{ questionStats.examName }}</el-descriptions-item>
          <el-descriptions-item label="参与人数">
            <span :style="{color: questionStats.participantsCount === 0 ? '#f56c6c' : '#67c23a'}">
              {{ questionStats.participantsCount || 0 }}
            </span>
            <el-button
              v-if="hasMultipleSessions"
              type="text"
              size="mini"
              @click="sessionDetailsVisible = !sessionDetailsVisible"
              style="margin-left: 8px">
              <i :class="sessionDetailsVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
              {{ sessionDetailsVisible ? '收起' : '详情' }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="题目总数">
            <span :style="{color: questionStats.totalQuestions === 0 ? '#f56c6c' : '#67c23a'}">
              {{ questionStats.totalQuestions || 0 }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="包含主观题">
            <el-tag :type="questionStats.hasSubjective ? 'warning' : 'info'" size="mini">
              {{ questionStats.hasSubjective ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 多课堂人数明细 -->
        <el-collapse-transition>
          <div v-if="sessionDetailsVisible && hasMultipleSessions" class="session-details">
            <el-divider content-position="left">
              <i class="el-icon-school" /> 课堂人数明细（共 {{ questionStats.sessionCount || 0 }} 个课堂）
            </el-divider>
            <div class="session-cards">
              <el-card
                v-for="session in (questionStats.sessionDetails || [])"
                :key="session.sessionId"
                shadow="hover"
                class="session-card">
                <div class="session-info">
                  <div class="session-name">
                    <i class="el-icon-s-home" />
                    {{ session.sessionName || `课堂${session.sessionId}` }}
                  </div>
                  <div class="session-count">
                    <el-tag type="primary" size="small">
                      <i class="el-icon-user" /> {{ session.studentCount || 0 }} 人
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </div>
            <div class="session-summary">
              <el-tag type="success" effect="dark" size="medium">
                <i class="el-icon-s-data" /> 总计：{{ questionStats.participantsCount || 0 }} 人
              </el-tag>
            </div>
          </div>
        </el-collapse-transition>
      </div>

      <!-- 未选择考试提示 -->
      <el-empty v-if="!currentExamId" description="请选择一个考试查看统计信息" :image-size="120" />

      <!-- 题目统计表格 -->
      <div v-if="currentExamId && questionStats" class="stats-section">
        <div class="section-header">
          <h3>题目统计与批改</h3>
          <div class="actions">
            <el-button type="primary" size="mini" icon="el-icon-refresh" @click="reloadData" :loading="loadingStats">刷新数据</el-button>
            <el-button v-if="hasUngradedSubjective" type="success" size="mini" icon="el-icon-document-checked" @click="batchGradeDialog=true" :disabled="!hasSelectedUngraded">
              批量批改 ({{ selectedUngraded.length }})
            </el-button>
          </div>
        </div>

        <!-- 数据状态提示 -->
        <div v-if="currentExamId && !loadingStats" style="margin:12px 0">
          <el-alert v-if="!questionStats" type="warning" :closable="false" title="无法获取考试数据，请检查考试ID是否正确" show-icon />
          <el-alert v-else-if="questionStats.totalQuestions === 0" type="info" :closable="false" title="该考试暂无题目" show-icon />
          <el-alert v-else-if="questionStats.participantsCount === 0" type="info" :closable="false" title="该考试暂无参与学生" show-icon />
        </div>
        <el-table
          v-if="questionStats && questionStats.questions"
          :data="sortedQuestions"
          stripe
          size="small"
          :loading="loadingStats"
          :expand-row-keys="expandedRows"
          :row-key="row => row.questionId"
          @expand-change="handleExpandChange"
          class="stats-table">
          <el-table-column type="expand">
            <template slot-scope="props">
              <div class="expand-content">
                <!-- 学生名单 -->
                <el-tabs v-model="props.row._activeTab" type="border-card">
                  <el-tab-pane :label="`正确 (${props.row.correctCount})`" name="correct">
                    <el-table :data="props.row.correctStudents" size="mini" max-height="300">
                      <el-table-column type="index" label="#" width="50" />
                      <el-table-column prop="studentNo" label="学号" width="140" />
                      <el-table-column prop="studentName" label="姓名" />
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane :label="`错误 (${props.row.incorrectCount})`" name="incorrect">
                    <el-table :data="props.row.incorrectStudents" size="mini" max-height="300">
                      <el-table-column type="index" label="#" width="50" />
                      <el-table-column prop="studentNo" label="学号" width="140" />
                      <el-table-column prop="studentName" label="姓名" />
                    </el-table>
                  </el-tab-pane>
                  <el-tab-pane :label="`未答 (${props.row.unansweredCount})`" name="unanswered">
                    <el-table :data="props.row.unansweredStudents" size="mini" max-height="300">
                      <el-table-column type="index" label="#" width="50" />
                      <el-table-column prop="studentNo" label="学号" width="140" />
                      <el-table-column prop="studentName" label="姓名" />
                    </el-table>
                  </el-tab-pane>
                  <!-- 主观题批改 -->
                  <el-tab-pane v-if="isSubjective(props.row.questionType)" :label="`待批改 (${getUngradedCount(props.row.questionId)})`" name="grade">
                    <div v-if="getUngradedAnswers(props.row.questionId).length > 0">
                      <el-table :data="getUngradedAnswers(props.row.questionId)" size="mini" @selection-change="val => onUngradedSelection(props.row.questionId, val)">
                        <el-table-column type="selection" width="45" />
                        <el-table-column prop="studentNo" label="学号" width="120" />
                        <el-table-column prop="studentAnswer" label="学生答案" min-width="200" show-overflow-tooltip />
                        <el-table-column label="得分" width="100">
                          <template slot-scope="scope">
                            <el-input-number v-model="scope.row._editScore" :min="0" :max="props.row.score" size="mini" @change="markDirty(scope.row)" />
                          </template>
                        </el-table-column>
                        <el-table-column label="评语" min-width="150">
                          <template slot-scope="scope">
                            <el-input v-model="scope.row._editComment" size="mini" placeholder="评语" @input="markDirty(scope.row)" />
                          </template>
                        </el-table-column>
                        <el-table-column label="操作" width="100">
                          <template slot-scope="scope">
                            <el-button type="primary" size="mini" :disabled="!scope.row._dirty" @click="gradeOne(scope.row)">提交</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                    <el-empty v-else description="暂无待批改" :image-size="80" />
                  </el-tab-pane>
                </el-tabs>
              </div>
            </template>
          </el-table-column>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column label="题型" width="80">
            <template slot-scope="scope">
              <el-tag :type="isSubjective(scope.row.questionType) ? 'warning' : 'success'" size="mini">
                {{ typeLabel(scope.row.questionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="questionContent" label="题目内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="70" align="center" />
          <el-table-column label="正确率" width="200" sortable :sort-method="(a,b) => a.correctRate - b.correctRate">
            <template slot-scope="scope">
              <div class="rate-display">
                <el-progress
                  :text-inside="true"
                  :stroke-width="20"
                  :percentage="Number(scope.row.correctRate.toFixed(1))"
                  :color="getRateColor(scope.row.correctRate)"
                  :status="getRateStatus(scope.row.correctRate)" />
                <div class="rate-text">{{ scope.row.correctCount }}/{{ scope.row.participantsCount }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="统计" width="260">
            <template slot-scope="scope">
              <div class="stats-tags">
                <el-tag size="small" type="success" effect="plain">正确 {{ scope.row.correctCount }}</el-tag>
                <el-tag size="small" type="danger" effect="plain">错误 {{ scope.row.incorrectCount }}</el-tag>
                <el-tag size="small" type="info" effect="plain">未答 {{ scope.row.unansweredCount }}</el-tag>
                <el-tag v-if="isSubjective(scope.row.questionType) && getUngradedCount(scope.row.questionId) > 0"
                  size="small" type="warning" effect="plain">
                  待批 {{ getUngradedCount(scope.row.questionId) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ExamGrading',
  props: { examId: { type: [String, Number], required: false } },
  data() {
    return {
      exams: [],
      currentExamId: null,
      currentSessionId: null,
      loadingExams: false,
      loadingStats: false,
      questionStats: null,
      ungraded: [],
      ungradedMap: {}, // questionId -> ungraded answers
      selectedUngraded: [],
      expandedRows: [],
      batchGradeDialog: false,
      sessionDetailsVisible: false
    }
  },
  computed: {
    filteredExams() {
      let result = this.exams
      if (this.statusFilter !== null) {
        result = result.filter(e => e.status === this.statusFilter)
      }
      if (this.sessionFilter !== null) {
        result = result.filter(e => e.sessionId === this.sessionFilter)
      }
      // 按发布时间倒序（或创建时间）
      return result.sort((a, b) => {
        const timeA = new Date(a.startTime || a.createTime || 0).getTime()
        const timeB = new Date(b.startTime || b.createTime || 0).getTime()
        return timeB - timeA
      })
    },
    uniqueSessions() {
      const sessions = []
      const seen = new Set()
      this.exams.forEach(e => {
        if (e.sessionId && !seen.has(e.sessionId)) {
          seen.add(e.sessionId)
          sessions.push({ sessionId: e.sessionId, className: e.className })
        }
      })
      return sessions
    },
    sortedQuestions() {
      if (!this.questionStats || !this.questionStats.questions) return []
      return this.questionStats.questions.map((q, index) => ({
        ...q,
        _index: index + 1,
        _activeTab: 'correct'
      }))
    },
    hasUngradedSubjective() {
      return this.ungraded && this.ungraded.length > 0
    },
    hasSelectedUngraded() {
      return this.selectedUngraded.length > 0
    },
    hasMultipleSessions() {
      return this.questionStats &&
             this.questionStats.sessionDetails &&
             this.questionStats.sessionDetails.length > 1
    },
    teacherSessions() {
      // 假设教师只能看到自己创建的考试的课堂
      const sessionSet = new Set()
      this.exams.forEach(exam => {
        if (exam.sessionId) {
          sessionSet.add(exam.sessionId)
        }
      })
      return Array.from(sessionSet).map(sessionId => {
        const exam = this.exams.find(ex => ex.sessionId === sessionId)
        return { sessionId, className: exam ? exam.className : `课堂${sessionId}` }
      })
    }
  },
  created() {
    this.loadExams().then(() => {
      if (this.examId) {
        this.currentExamId = Number(this.examId)
        this.reloadData()
      }
    })
  },
  watch: {
    examId(val) {
      if (val) {
        this.currentExamId = Number(val)
        this.reloadData()
      }
    }
  },
  methods: {
    async loadExams() {
      this.loadingExams = true
      try {
        const resp = await request({ url: '/proj_lwj/exam/list', method: 'get', params: { pageNum: 1, pageSize: 500 } })
        this.exams = (resp.rows || []).map(r => ({ ...r }))
      } catch (e) {
        this.$message.error('考试列表加载失败: ' + (e.message || ''))
      } finally {
        this.loadingExams = false
      }
    },
    applyFilters() {
      // 过滤器变更时清空当前选择
      this.currentExamId = null
      this.questionStats = null
    },
    async onSessionChange() {
      if (!this.currentSessionId) {
        this.currentExamId = null
        this.questionStats = null
        return
      }
      // 载入该课堂下的考试
      this.loadExams()
    },
    async onExamChange() {
      if (!this.currentExamId) {
        this.questionStats = null
        return
      }
      await this.reloadData()
    },
    async reloadData() {
      if (!this.currentExamId) return
      await Promise.all([
        this.reloadQuestionStats(),
        this.reloadUngraded()
      ])
    },
    async reloadQuestionStats() {
      if (!this.currentExamId) return
      this.loadingStats = true
      try {
        console.log('Loading question stats for exam:', this.currentExamId)
        const resp = await request({ url: `/proj_lwj/exam/${this.currentExamId}/questionCorrectSummary`, method: 'get' })
        console.log('Question stats response:', resp)

        // 确保数据结构正确
        if (resp && (resp.code === 200 || resp.code === 0)) {
          this.questionStats = resp.data || resp
        } else {
          this.questionStats = resp
        }

        // 如果还是没有数据，显示警告
        if (!this.questionStats || (!this.questionStats.questions && !this.questionStats.totalQuestions)) {
          console.warn('No question data received for exam:', this.currentExamId)
          this.$message.warning('该考试暂无题目数据')
        }
      } catch (e) {
        console.error('Failed to load question stats:', e)
        this.$message.error('题目统计获取失败: ' + (e.message || e.toString()))
        this.questionStats = null
      } finally {
        this.loadingStats = false
      }
    },
    async reloadUngraded() {
      if (!this.currentExamId) return
      try {
        const data = await request({ url: `/proj_lwj/exam/${this.currentExamId}/ungraded`, method: 'get' })
        this.ungraded = (data || []).map(a => ({
          ...a,
          _editScore: a.score || 0,
          _editComment: a.correctComment || '',
          _dirty: false
        }))
        // 按题目分组
        this.ungradedMap = {}
        this.ungraded.forEach(a => {
          if (!this.ungradedMap[a.questionId]) {
            this.ungradedMap[a.questionId] = []
          }
          this.ungradedMap[a.questionId].push(a)
        })
      } catch (e) {
        console.error('未批改列表获取失败:', e)
      }
    },
    getUngradedAnswers(questionId) {
      return this.ungradedMap[questionId] || []
    },
    getUngradedCount(questionId) {
      return (this.ungradedMap[questionId] || []).length
    },
    isSubjective(type) {
      return [4, 5, 6].includes(Number(type))
    },
    typeLabel(t) {
      return { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答', 6: '文件' }[t] || t
    },
    statusText(s) {
      return { 0: '草稿', 1: '已发布', 2: '进行中', 3: '已结束' }[Number(s)] || '未知'
    },
    statusTagType(s) {
      const map = { 0: 'info', 1: '', 2: 'success', 3: 'danger' }
      return map[Number(s)] || 'info'
    },
    getRateColor(rate) {
      if (rate >= 70) return '#67c23a'
      if (rate >= 30) return '#e6a23c'
      return '#f56c6c'
    },
    getRateStatus(rate) {
      if (rate >= 70) return 'success'
      if (rate < 30) return 'exception'
      return null
    },
    handleExpandChange(row, expandedRows) {
      this.expandedRows = expandedRows.map(r => r.questionId)
    },
    onUngradedSelection(questionId, selection) {
      // 更新选中的未批改答案
      const others = this.selectedUngraded.filter(s => s.questionId !== questionId)
      this.selectedUngraded = [...others, ...selection]
    },
    markDirty(row) {
      row._dirty = true
    },
    async gradeOne(row) {
      if (!row._dirty) return
      if (row._editScore == null || row._editScore < 0) {
        return this.$message.warning('分数必须为非负数')
      }
      try {
        await request({
          url: '/proj_lwj/exam/answer/grade',
          method: 'post',
          data: { id: row.id, score: row._editScore, correctComment: row._editComment }
        })
        this.$message.success('批改成功')
        await this.reloadData()
      } catch (e) {
        this.$message.error('批改失败: ' + (e.message || ''))
      }
    },
    async debugAPI() {
      if (!this.currentExamId) return

      try {
        console.log('=== DEBUG API CALL ===')
        console.log('Exam ID:', this.currentExamId)
        console.log('URL:', `/proj_lwj/exam/${this.currentExamId}/questionCorrectSummary`)

        // 测试request工具
        const requestResponse = await request({
          url: `/proj_lwj/exam/${this.currentExamId}/questionCorrectSummary`,
          method: 'get'
        })
        console.log('Request util response:', requestResponse)

        // 显示在页面上
        this.$alert(`
          <pre style="text-align: left; max-height: 400px; overflow: auto;">
Request工具响应: ${JSON.stringify(requestResponse, null, 2)}
          </pre>
        `, 'API调试结果', {
          dangerouslyUseHTMLString: true
        })

      } catch (e) {
        console.error('Debug API call failed:', e)
        this.$alert(`API调用失败: ${e.message}`, '调试错误', { type: 'error' })
      }
    }
  }
}
</script>
<style scoped>
.exam-grading-page {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.box-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.filter-section {
  padding: 16px;
  background: #fff;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #ebeef5;
}

.exam-overview {
  padding: 16px;
  background: #f9fafc;
  border-bottom: 1px solid #ebeef5;
}

.stats-section {
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-header .actions {
  display: flex;
  gap: 8px;
}

.stats-table {
  margin-top: 12px;
}

.rate-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rate-text {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.stats-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.expand-content {
  padding: 16px;
  background: #fafafa;
}

.expand-content >>> .el-tabs__header {
  margin-bottom: 12px;
}

/* 进度条自定义颜色 */
.el-progress--line {
  margin-bottom: 4px;
}

/* 空状态优化 */
.el-empty {
  padding: 40px 0;
}
</style>
