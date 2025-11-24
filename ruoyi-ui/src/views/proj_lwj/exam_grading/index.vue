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

      <!-- 批改筛选工具条 -->
      <div style="margin:12px 0" v-if="gradingExams && gradingExams.length">
        <el-radio-group v-model="gradingFilter" size="mini">
          <el-radio-button label="need">待批改</el-radio-button>
          <el-radio-button label="done">已批改</el-radio-button>
          <el-radio-button label="all">全部</el-radio-button>
        </el-radio-group>
        <el-tag type="warning" size="mini" style="margin-left:12px">待批改考试: {{ gradingExams.filter(e=>e.gradingNeeded).length }}</el-tag>
        <el-button size="mini" type="primary" icon="el-icon-refresh" @click="reloadAllForGrading" style="margin-left:8px">刷新批改状态</el-button>
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
                <!-- 学生名单：客观题保留原三类；主观题改为 待批改 / 已批改 / 未答 -->
                <el-tabs v-model="props.row._activeTab" type="border-card">
                  <template v-if="!props.row.isSubjective">
                    <!-- 保持原正确/错误/未答 -->
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
                  </template>
                  <template v-else>
                    <!-- 主观题: 待批改 -->
                    <el-tab-pane :label="`待批改 (${getUngradedCount(props.row.questionId)})`" name="ungraded">
                      <div v-if="getUngradedAnswers(props.row.questionId).length > 0">
                        <el-table :data="getUngradedAnswers(props.row.questionId)" size="mini" @selection-change="val => onUngradedSelection(props.row.questionId, val)">
                          <el-table-column type="selection" width="45" />
                          <el-table-column prop="studentNo" label="学号" width="120" />
                          <el-table-column prop="studentAnswer" label="学生答案" min-width="220" show-overflow-tooltip />
                          <el-table-column label="操作" width="120">
                            <template slot-scope="scope">
                              <el-button type="primary" size="mini" @click="openGrade(scope.row, props.row)">批改</el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                      <el-empty v-else description="暂无待批改" :image-size="80" />
                    </el-tab-pane>
                    <!-- 主观题: 已批改 -->
                    <el-tab-pane :label="`已批改 (${getGradedCount(props.row.questionId)})`" name="graded">
                      <div v-if="getGradedAnswers(props.row.questionId).length > 0">
                        <el-table :data="getGradedAnswers(props.row.questionId)" size="mini">
                          <el-table-column type="index" width="45" />
                          <el-table-column prop="studentNo" label="学号" width="120" />
                          <el-table-column prop="studentAnswer" label="学生答案" min-width="220" show-overflow-tooltip />
                          <el-table-column prop="score" label="得分" width="90" />
                          <el-table-column prop="correctComment" label="评语" min-width="160" show-overflow-tooltip />
                          <el-table-column label="操作" width="120">
                            <template slot-scope="scope">
                              <el-button type="text" size="mini" @click="openGrade(scope.row, props.row, true)">修改给分</el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                      <el-empty v-else description="暂无已批改" :image-size="80" />
                    </el-tab-pane>
                    <!-- 主观题: 未答 -->
                    <el-tab-pane :label="`未答 (${props.row.unansweredCount})`" name="unanswered">
                      <el-table :data="props.row.unansweredStudents" size="mini" max-height="300">
                        <el-table-column type="index" label="#" width="50" />
                        <el-table-column prop="studentNo" label="学号" width="140" />
                        <el-table-column prop="studentName" label="姓名" />
                      </el-table>
                      <el-empty v-if="!props.row.unansweredStudents || !props.row.unansweredStudents.length" description="暂无未答学生" :image-size="60" />
                    </el-tab-pane>
                  </template>
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
              <div v-if="!scope.row.isSubjective" class="rate-display">
                <el-progress
                  :text-inside="true"
                  :stroke-width="20"
                  :percentage="Number(scope.row.correctRate.toFixed(1))"
                  :color="getRateColor(scope.row.correctRate)"
                  :status="getRateStatus(scope.row.correctRate)" />
                <div class="rate-text">{{ scope.row.correctCount }}/{{ scope.row.participantsCount }}</div>
              </div>
              <div v-else style="text-align:center;color:#909399;font-size:12px">主观题无正确率</div>
            </template>
          </el-table-column>
          <el-table-column label="统计" width="260">
            <template slot-scope="scope">
              <div class="stats-tags" v-if="!scope.row.isSubjective">
                <el-tag size="small" type="success" effect="plain">正确 {{ scope.row.correctCount }}</el-tag>
                <el-tag size="small" type="danger" effect="plain">错误 {{ scope.row.incorrectCount }}</el-tag>
                <el-tag size="small" type="info" effect="plain">未答 {{ scope.row.unansweredCount }}</el-tag>
              </div>
              <div class="stats-tags" v-else>
                <el-tag size="small" type="warning" effect="plain">待批改 {{ scope.row.ungradedCount }}</el-tag>
                <el-tag size="small" type="success" effect="plain">已批改 {{ scope.row.gradedCount }}</el-tag>
                <el-tag size="small" type="info" effect="plain">未答 {{ scope.row.unansweredCount }}</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 批量批改主观题对话框 -->
      <el-dialog title="批量批改主观题" :visible.sync="batchGradeDialog" width="600px" v-if="batchGradeDialog">
        <div v-if="selectedUngraded.length===0">
          <el-empty description="请在题目展开的待批改列表中勾选需要批改的答卷" :image-size="80" />
        </div>
        <div v-else class="batch-grade-body">
          <el-alert :title="`已选择 ${selectedUngraded.length} 条待批改答卷`" type="info" show-icon :closable="false" style="margin-bottom:12px" />
          <el-form :model="batchForm" label-width="100px" size="small">
            <el-form-item label="得分方式">
              <el-radio-group v-model="batchForm.scoreMode">
                <el-radio label="full">满分</el-radio>
                <el-radio label="custom">自定义</el-radio>
                <el-radio label="zero">0分</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="batchForm.scoreMode==='custom'" label="分值">
              <el-input-number v-model="batchForm.customScore" :min="0" :max="batchMaxScore" />
              <span style="margin-left:8px;color:#909399">(最大: {{ batchMaxScore }})</span>
            </el-form-item>
            <el-form-item label="统一评语">
              <el-input type="textarea" :rows="3" v-model="batchForm.comment" placeholder="可选，留空则不写评语" />
            </el-form-item>
          </el-form>
          <el-divider content-position="left">预览</el-divider>
          <el-table :data="selectedUngraded" size="mini" height="200">
            <el-table-column type="index" width="50" />
            <el-table-column prop="studentNo" label="学号" width="140" />
            <el-table-column prop="studentAnswer" label="答案" min-width="200" show-overflow-tooltip />
            <el-table-column label="原分" width="80">
              <template slot-scope="scope">{{ scope.row.score==null? '—' : scope.row.score }}</template>
            </el-table-column>
            <el-table-column label="批改后" width="90">
              <template>{{ previewScore }}</template>
            </el-table-column>
          </el-table>
        </div>
        <template #footer>
          <div style="text-align:right">
            <el-button @click="batchGradeDialog=false">取 消</el-button>
            <el-button type="primary" :disabled="selectedUngraded.length===0" :loading="batchGrading" @click="gradeBatch">提交批改</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 主观题批改弹窗 -->
      <el-dialog title="批改主观题" :visible.sync="gradeDialogVisible" width="640px" v-if="gradeDialogVisible">
        <el-form :model="gradeForm" label-width="100px" size="small">
          <el-form-item label="题目">
            <div class="q-preview" v-html="gradeForm.questionContent"></div>
          </el-form-item>
          <el-form-item label="学生答案">
            <el-input type="textarea" :rows="4" v-model="gradeForm.studentAnswer" readonly />
          </el-form-item>
          <el-form-item label="标准答案" v-if="gradeForm.correctAnswer">
            <el-input type="textarea" :rows="3" v-model="gradeForm.correctAnswer" readonly />
          </el-form-item>
          <el-form-item label="题目满分">
            <el-tag type="info">{{ gradeForm.maxScore }}</el-tag>
          </el-form-item>
          <el-form-item label="得分">
            <el-input-number v-model="gradeForm.score" :min="0" :max="gradeForm.maxScore" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input type="textarea" :rows="3" v-model="gradeForm.comment" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div style="text-align:right">
            <el-button @click="gradeDialogVisible=false">取 消</el-button>
            <el-button type="primary" :loading="gradingOne" @click="submitGrade">提 交</el-button>
          </div>
        </template>
      </el-dialog>
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
      sessionDetailsVisible: false,
      batchForm: { scoreMode:'full', customScore:null, comment:'' },
      batchGrading: false,
      gradingFilter: 'need', // need|all|done
      teacherStudentNo: '', // 可选：若教师也有对应账号学号可用于 myExams 接口
      gradingExams: [], // 带标注的可批改考试列表
      allAnswers: [], // 全部答案（用于主观题统计）
      answersByQuestion: {}, // questionId -> 答案数组
      gradeDialogVisible: false,
      gradeForm: { id:null, questionId:null, studentNo:'', studentAnswer:'', correctAnswer:'', maxScore:0, score:0, comment:'', questionContent:'' },
      gradingOne: false
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
      const participants = Number(this.questionStats.participantsCount || 0)
      return this.questionStats.questions.map((q, index) => {
        const isSubj = this.isSubjective(q.questionType)
        let correctRate = q.correctRate
        let correctCount = q.correctCount
        let incorrectCount = q.incorrectCount
        let unansweredCount = q.unansweredCount
        let ungradedCount = q.ungradedCount
        let gradedCount = 0
        if (isSubj) {
          // 基于 answersByQuestion 重算主观题三类：未作答 / 未批改 / 已批改
          const answers = this.answersByQuestion[q.questionId] || []
          const answeredCount = answers.length
          // graded: 有 correctorId 或 score 非空
          gradedCount = answers.filter(a => a.correctorId != null || (a.score != null && a.score !== '')).length
          // ungraded: 已作答但无批改标记
          ungradedCount = answeredCount - gradedCount
          // unanswered: 参与人数 - answeredCount
          unansweredCount = Math.max(participants - answeredCount, 0)
          // 主观题不显示正确率，因此设为0或保留原值但前端不展示
          correctRate = 0
          correctCount = 0
          incorrectCount = 0
        }
        return {
          ...q,
          _index: index + 1,
          _activeTab: isSubj ? 'ungraded' : 'correct',
          isSubjective: isSubj,
          correctRate,
          correctCount,
            incorrectCount,
          unansweredCount,
          ungradedCount,
          gradedCount,
        }
      })
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
    },
    batchMaxScore(){
      // 取所选答卷所属题目最大分值（如果不同题目，取最小的一个作为上限保证安全）
      if(!this.selectedUngraded.length) return 0
      const scores = this.selectedUngraded.map(a => {
        const q = (this.questionStats && this.questionStats.questions || []).find(q => q.questionId === a.questionId)
        return q && q.score ? Number(q.score) : 0
      }).filter(s => !isNaN(s) && s>0)
      if(!scores.length) return 0
      return Math.min(...scores)
    },
    previewScore(){
      if(this.batchForm.scoreMode==='full') return this.batchMaxScore
      if(this.batchForm.scoreMode==='zero') return 0
      if(this.batchForm.scoreMode==='custom') return this.batchForm.customScore || 0
      return 0
    },
    filteredGradingExams(){
      if(!this.gradingExams) return []
      return this.gradingExams.filter(e=>{
        if(this.gradingFilter==='need') return e.gradingNeeded
        if(this.gradingFilter==='done') return e.hasSubjective && !e.gradingNeeded
        return true
      }).sort((a,b)=> new Date(b.startTime||0)-new Date(a.startTime||0))
    }
  },
  created() {
    this.restoreSelections()
    this.loadExams().then(() => {
      if (this.examId) {
        this.currentExamId = Number(this.examId)
        this.reloadData()
      } else if (this._pendingRestoreExamId) {
        this.currentExamId = this._pendingRestoreExamId
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
    },
    currentSessionId(val){
      try {
        if(val) localStorage.setItem('exam_grading_lastSessionId', String(val))
        else localStorage.removeItem('exam_grading_lastSessionId')
      } catch(e){}
    },
    currentExamId(val){
      try {
        if(val) localStorage.setItem('exam_grading_lastExamId', String(val))
        else localStorage.removeItem('exam_grading_lastExamId')
      } catch(e){}
    }
  },
  methods: {
    // 新增：刷新课堂/考试列表入口，按钮调用的 loadTeacherSessions
    loadTeacherSessions(){
      // 仅重新加载考试列表并重建可批改考试缓存
      this.loadExams().then(()=> this.buildGradingExams()).catch(()=>{})
    },
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
        this.reloadUngraded(),
        this.reloadAllAnswers()
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
        const resp = await request({ url: `/proj_lwj/exam/${this.currentExamId}/ungraded`, method: 'get' })
        let list = resp && (resp.rows || resp.data || resp.list || resp)
        if (!Array.isArray(list)) {
          console.warn('[reloadUngraded] 非数组返回，已置空：', resp)
          list = []
        }
        this.ungraded = list.map(a => ({
          ...a,
          _editScore: a.score || 0,
            _editComment: a.correctComment || '',
          _dirty: false
        }))
        // 分组
        this.ungradedMap = {}
        this.ungraded.forEach(a => {
          if (!a.questionId) return
          if (!this.ungradedMap[a.questionId]) this.ungradedMap[a.questionId] = []
          this.ungradedMap[a.questionId].push(a)
        })
      } catch (e) {
        console.error('未批改列表获取失败:', e)
        this.ungraded = []
        this.ungradedMap = {}
      }
    },
    async reloadAllAnswers(){
      if(!this.currentExamId) return
      try {
        const resp = await request({ url:'/proj_lwj/exam/answer/list', method:'get', params:{ examId: this.currentExamId } })
        const rows = resp && (resp.rows || resp.data || resp.list || [])
        this.allAnswers = Array.isArray(rows) ? rows : []
        // 构建 answersByQuestion
        const map = {}
        this.allAnswers.forEach(a => {
          if(!a.questionId) return
          if(!map[a.questionId]) map[a.questionId] = []
          map[a.questionId].push(a)
        })
        this.answersByQuestion = map
      } catch(e){ console.error('加载全部答案失败', e); this.allAnswers=[]; this.answersByQuestion={} }
    },
    getUngradedAnswers(questionId) {
      return this.ungradedMap[questionId] || []
    },
    getUngradedCount(questionId) {
      return (this.ungradedMap[questionId] || []).length
    },
    isSubjective(type) {
      return [5, 6].includes(Number(type))
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
      request({
        url: '/proj_lwj/exam/answer/grade',
        method: 'post',
        data: { id: row.id, score: row._editScore, correctComment: row._editComment }
      }).then(()=>{ this.$message.success('批改成功'); this.reloadData() }).catch(e=>{ this.$message.error('批改失败: '+(e.message||'')) })
    },
    async gradeBatch(){
      if(!this.selectedUngraded.length) return this.$message.error('未选择任何答卷')
      const targetScore = this.previewScore
      if(targetScore == null || targetScore < 0){ return this.$message.error('分值不合法') }
      // 构造批量 payload
      const payload = this.selectedUngraded.map(a => ({ id: a.id, score: targetScore, correctComment: this.batchForm.comment || '' }))
      this.batchGrading = true
      try {
        await this.$confirm(`确认批量批改 ${payload.length} 条答卷?`, '提示', { type:'warning' })
        const resp = await request({ url:'/proj_lwj/exam/answer/gradeBatch', method:'post', data: payload })
        if(resp && (resp.code===200 || resp.code===0)){
          this.$message.success('批量批改完成')
          this.batchGradeDialog=false
          this.selectedUngraded=[]
          await this.reloadData()
        } else {
          this.$message.error((resp && (resp.msg||resp.message)) || '批量批改失败')
        }
      } catch(e){ if(e!=='cancel'){ console.error(e); this.$message.error('批量批改失败') } }
      finally { this.batchGrading=false }
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
    },
    async buildGradingExams(){
      // 基于已发布或已结束的考试，标识是否有主观题未批改
      const base = this.exams || []
      const target = []
      for(const ex of base){
        if(!ex.id) continue
        // 仅处理状态>=1 的考试
        if(ex.status < 1) continue
        try {
          const summary = await request({ url:`/proj_lwj/exam/${ex.id}/questionCorrectSummary`, method:'get' })
          const hasSubjective = summary && summary.data ? summary.data.hasSubjective : summary.hasSubjective
          let ungradedTotal = 0
            ;(summary.data? summary.data.questions : summary.questions || []).forEach(q=>{ if(q.ungradedCount) ungradedTotal += q.ungradedCount })
          target.push({
            id: ex.id,
            examName: ex.examName,
            status: ex.status,
            startTime: ex.startTime,
            endTime: ex.endTime,
            hasSubjective: !!hasSubjective,
            ungradedTotal,
            gradingNeeded: hasSubjective && ungradedTotal > 0
          })
        } catch(e){ /* ignore single exam error */ }
      }
      this.gradingExams = target
    },
    async reloadAllForGrading(){
      await this.loadExams()
      await this.buildGradingExams()
    },
    getGradedAnswers(questionId){
      const all = this.answersByQuestion[questionId] || []
      return all.filter(a => (a.score!=null && a.score!=='') || a.correctorId!=null)
    },
    getGradedCount(questionId){ return this.getGradedAnswers(questionId).length },
    openGrade(answerRow, questionRow, isModify=false){
      if(!answerRow) return
      const q = questionRow || (this.questionStats && this.questionStats.questions || []).find(x=>x.questionId===answerRow.questionId) || {}
      this.gradeForm.id = answerRow.id
      this.gradeForm.questionId = answerRow.questionId
      this.gradeForm.studentNo = answerRow.studentNo
      this.gradeForm.studentAnswer = answerRow.studentAnswer
      this.gradeForm.correctAnswer = q.correctAnswer || answerRow.correctAnswer || ''
      this.gradeForm.maxScore = Number(q.score || answerRow.scoreMax || answerRow.maxScore || 0)
      this.gradeForm.score = isModify ? Number(answerRow.score||0) : this.gradeForm.maxScore
      this.gradeForm.comment = isModify ? (answerRow.correctComment||'') : ''
      this.gradeForm.questionContent = q.questionContent || ''
      this.gradeDialogVisible = true
    },
    async submitGrade(){
      if(this.gradingOne) return
      if(this.gradeForm.score==null || this.gradeForm.score<0){ return this.$message.warning('分数不合法') }
      if(this.gradeForm.score>this.gradeForm.maxScore){ return this.$message.warning('分数不能超过题目满分') }
      this.gradingOne = true
      try {
        const payload = { id:this.gradeForm.id, score:this.gradeForm.score, correctComment:this.gradeForm.comment }
        console.log('[submitGrade] 提交批改 payload', payload)
        const resp = await request({ url:'/proj_lwj/exam/answer/grade', method:'post', data: payload })
        if(resp && (resp.code===200 || resp.code===0)){
          this.$message.success('批改已提交')
        } else {
          this.$message.warning('批改接口返回异常，已尝试刷新')
        }
        this.gradeDialogVisible=false
        await this.reloadData()
      } catch(e){ console.error(e); this.$message.error('批改失败: '+(e.message||'')) }
      finally { this.gradingOne=false }
    },
    restoreSelections(){
      try {
        const sid = localStorage.getItem('exam_grading_lastSessionId')
        const eid = localStorage.getItem('exam_grading_lastExamId')
        if(sid) this.currentSessionId = Number(sid)
        if(eid) this._pendingRestoreExamId = Number(eid)
      } catch(e){ /* ignore */ }
    },
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

.stats-tags { display:flex; flex-wrap:wrap; gap:6px; }

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

.batch-grade-body { max-height: 60vh; overflow: auto; }
.q-preview { background:#fafafa; padding:8px 12px; border-radius:4px; font-size:13px; line-height:1.6; max-height:180px; overflow:auto; }
</style>
