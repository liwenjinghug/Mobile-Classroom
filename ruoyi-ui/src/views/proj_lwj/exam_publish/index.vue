<template>
  <div class="app-container">
    <!-- 顶部卡片：考试发布配置 -->
    <el-card class="hero-card" shadow="hover">
      <div slot="header" class="card-header">
        <div class="title-wrap">
          <i class="el-icon-edit-outline" />
          <span class="title">考试发布</span>
          <span class="sub">请选择课程与课堂，配置考试时间、规则与题目策略</span>
        </div>
        <div class="header-actions">
           <el-button type="primary" icon="el-icon-check" @click="onSave(false)" :loading="btnLoading">保存草稿</el-button>
           <el-button type="success" icon="el-icon-upload" @click="onSave(true)" :loading="btnLoading">发布考试</el-button>
        </div>
      </div>

      <el-form :model="form" ref="form" label-width="100px" size="small" class="form-grid">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="课程选择" prop="courseId">
              <el-select v-model="form.courseId" placeholder="请选择课程" filterable class="w-100">
                <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="课堂选择" prop="sessionId">
              <el-select v-model="form.sessionId" placeholder="请先选择课程" @change="onSessionChange" class="w-100">
                <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className ? `${s.className} (ID:${s.sessionId})` : String(s.sessionId))" :value="s.sessionId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="12">
            <el-form-item label="考试名称" prop="examName">
              <el-input v-model="form.examName" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="考试时间" required>
              <el-date-picker v-model="timeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" class="w-100" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="考试时长">
              <el-input-number v-model="form.examDuration" :min="1" :max="1000" label="分钟" controls-position="right" class="w-100" />
            </el-form-item>
          </el-col>
           <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="考试类型">
              <el-select v-model="form.examType" class="w-100">
                <el-option :value="3" label="测验"/>
                <el-option :value="1" label="期中"/>
                <el-option :value="2" label="期末"/>
                <el-option :value="5" label="课堂测验"/>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="总分">
              <el-input-number v-model="form.totalScore" :precision="2" :step="1" :min="0" :max="1000" controls-position="right" class="w-100" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="及格分">
              <el-input-number v-model="form.passScore" :precision="2" :step="1" :min="0" :max="1000" controls-position="right" class="w-100" />
            </el-form-item>
          </el-col>
           <el-col :xs="24" :sm="24" :md="12">
            <el-form-item label="其他选项">
               <div style="display:flex;align-items:center;">
                  <el-checkbox v-model="bool.antiCheat" style="margin-right:16px">防作弊</el-checkbox>
                  <el-checkbox v-model="bool.autoSubmit" style="margin-right:16px">自动交卷</el-checkbox>
                  <el-checkbox v-model="bool.lateSubmit">允许迟交</el-checkbox>
                  <div v-if="bool.lateSubmit" style="margin-left:12px;display:flex;align-items:center">
                     <el-input-number v-model="form.lateTime" :min="0" :max="1440" size="mini" style="width:100px" />
                     <span style="color:#666;font-size:12px;margin-left:4px">分钟</span>
                  </div>
               </div>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="考试模式">
              <el-radio-group v-model="form.examMode">
                <el-radio :label="1">定时考试</el-radio>
                <el-radio :label="2">随到随考</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="题目顺序">
              <el-radio-group v-model="form.questionOrder">
                <el-radio :label="0">正常</el-radio>
                <el-radio :label="1">随机</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-form-item label="答案显示">
              <el-select v-model="form.showAnswer" placeholder="" class="w-100">
                <el-option :label="'不显示'" :value="0"/>
                <el-option :label="'立即显示'" :value="1"/>
                <el-option :label="'考试结束后'" :value="2"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
         <div class="form-hint">
          <el-alert type="info" :closable="false" show-icon title="说明：保存后可在下方列表继续配置题目，配置完成后方可发布。" />
        </div>
      </el-form>
    </el-card>

    <!-- 已创建考试列表 -->
    <el-card class="list-card" shadow="hover">
      <div slot="header" class="card-header">
        <span class="card-title">已创建考试（当前课堂）</span>
        <el-input v-model="query.examName" placeholder="按名称过滤" size="small" class="search-input" @input="loadList" />
        <el-button size="small" type="primary" icon="el-icon-refresh" :loading="listLoading" @click="loadList" style="margin-left:8px">刷新</el-button>
        <div style="display:flex;gap:8px;align-items:center;margin-left:auto">
          <el-button size="small" icon="el-icon-download" @click="exportExamList">导出列表</el-button>
          <el-button size="small" icon="el-icon-printer" @click="printExamList">打印列表</el-button>
        </div>
      </div>
      <el-alert
        title="发布流程：1) 题目配置 → 2) 列表中点击发布考试 → 3) 开始考试"
        type="info" show-icon class="process-alert"/>
      <!-- 在考试列表上方增加当前所选考试的得分汇总提示 -->
      <el-alert v-if="scoreSummary" :title="scoreSummaryTitle" type="warning" show-icon class="score-alert" />
      <el-table :data="sortedList" v-loading="listLoading" style="width:100%" :row-key="rowKeyField" :row-class-name="rowClassName" @selection-change="onSelectionChange" class="beautified-table" @expand-change="handleExpandChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="42" reserve-selection />
        <!-- 新增：展开列显示提交统计 -->
        <el-table-column type="expand">
          <template #default="scope">
            <div class="exam-submission-panel" v-loading="submissionStats[scope.row.id] && submissionStats[scope.row.id].loading">
              <template v-if="submissionStats[scope.row.id] && !submissionStats[scope.row.id].loading">
                <div class="submission-summary">
                  <div class="ring-wrapper">
                    <div class="ring" :style="{ background: 'conic-gradient(#67c23a ' + (submissionStats[scope.row.id].submittedRate || 0) + '%, #ebeef5 0)'}">
                      <div class="ring-inner">
                        <strong>{{ (submissionStats[scope.row.id].submittedRate || 0).toFixed(1) }}%</strong>
                        <div class="ring-text">提交率</div>
                      </div>
                    </div>
                  </div>
                  <div class="stats-cards">
                    <el-card shadow="hover" class="mini-stat" body-style="{padding:'12px'}">
                      <div class="stat-title">总人数</div>
                      <div class="stat-value">{{ submissionStats[scope.row.id].participantsCount }}</div>
                    </el-card>
                    <el-card shadow="hover" class="mini-stat" body-style="{padding:'12px'}">
                      <div class="stat-title">已提交</div>
                      <div class="stat-value success-text">{{ submissionStats[scope.row.id].submittedCount }}</div>
                    </el-card>
                    <el-card shadow="hover" class="mini-stat" body-style="{padding:'12px'}">
                      <div class="stat-title">未提交</div>
                      <div class="stat-value warning-text">{{ submissionStats[scope.row.id].unsubmittedCount }}</div>
                    </el-card>
                    <el-card shadow="hover" class="mini-stat" body-style="{padding:'12px'}">
                      <div class="stat-title">平均分</div>
                      <div class="stat-value">{{ submissionStats[scope.row.id].averageScore != null ? submissionStats[scope.row.id].averageScore.toFixed(1) : '—' }}</div>
                    </el-card>
                    <el-card shadow="hover" class="mini-stat" body-style="{padding:'12px'}">
                      <div class="stat-title">及格率</div>
                      <div class="stat-value">{{ submissionStats[scope.row.id].passRate != null ? (submissionStats[scope.row.id].passRate.toFixed(1) + '%') : '—' }}</div>
                    </el-card>
                  </div>
                </div>
                <div class="submission-actions">
                  <el-button type="primary" size="mini" icon="el-icon-refresh" @click="forceRefresh(scope.row)">刷新</el-button>
                  <el-button size="mini" type="info" @click="stopAuto(scope.row)" v-if="submissionStats[scope.row.id].timer">停止自动刷新</el-button>
                  <el-button size="mini" type="success" @click="startAuto(scope.row)" v-else>开启自动刷新</el-button>
                  <el-button size="mini" type="warning" icon="el-icon-download" @click="exportSubmissionStats(scope.row)">导出统计</el-button>
                  <el-button size="mini" type="info" icon="el-icon-printer" @click="printSubmissionStats(scope.row)">打印统计</el-button>
                  <span class="last-fetch" v-if="submissionStats[scope.row.id].lastFetch">上次更新：{{ fmtTime(submissionStats[scope.row.id].lastFetch) }}</span>
                  <span class="error-tip" v-if="submissionStats[scope.row.id].error">{{ submissionStats[scope.row.id].error }}</span>
                </div>
                <el-table :data="submissionStats[scope.row.id].records" size="mini" class="submission-table" max-height="300" v-if="submissionStats[scope.row.id].records && submissionStats[scope.row.id].records.length">
                  <el-table-column type="index" width="50" label="#" />
                  <el-table-column prop="studentNo" label="学号" width="140" />
                  <el-table-column prop="submitted" label="状态" width="90">
                    <template #default="r">
                      <el-tag :type="r.row.submitted ? 'success' : 'info'">{{ r.row.submitted ? '已提交' : '未提交' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="90">
                    <template #default="r">{{ r.row.score == null ? '—' : r.row.score }}</template>
                  </el-table-column>
                  <el-table-column prop="passed" label="及格" width="70">
                    <template #default="r">
                      <el-tag size="mini" :type="r.row.passed ? 'success' : 'danger'">{{ r.row.passed ? '是' : '否' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="answerCount" label="答题数" width="80" />
                </el-table>
                <el-empty v-else description="暂无提交记录" :image-size="80" />
              </template>
              <el-empty v-else description="加载中..." :image-size="60" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="examName" label="名称" min-width="140" sortable="custom" />
        <el-table-column label="时间" min-width="220" sortable="custom" prop="startTime">
          <template #default="scope">{{ fmt(scope.row.startTime) }} ~ {{ fmt(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="examDuration" label="时长" width="74" sortable="custom" />
        <el-table-column prop="totalScore" label="总分" width="74" sortable="custom" />
        <el-table-column prop="questionCount" label="题目数" width="80" sortable="custom">
          <template #default="scope">
            <span :style="{color: scope.row.questionCount ? '#409EFF' : '#F56C6C'}">{{ scope.row.questionCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96" sortable="custom" prop="status">
          <template #default="scope">
            <el-tag :type="statusType(scope.row)" class="status-tag">{{ statusDisplay(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="310" fixed="right">
          <template #default="scope">
            <div class="op-actions-row compact-actions">
              <el-tooltip content="编辑配置" placement="top" :enterable="false">
                <el-button size="mini" icon="el-icon-edit" @click="openExamEdit(scope.row)" circle />
              </el-tooltip>
              <template v-if="isEndedOrExpired(scope.row)">
                <el-tooltip content="已结束/已截止，题目不可再配置" placement="top" :enterable="false">
                  <el-button size="mini" icon="el-icon-tickets" circle disabled />
                </el-tooltip>
              </template>
              <el-tooltip v-else content="题目配置" placement="top" :enterable="false">
                <el-button size="mini" icon="el-icon-tickets" @click="gotoQuestions(scope.row)" circle />
              </el-tooltip>
              <el-tooltip v-if="[1,2,3].includes(Number(scope.row.status))" content="去批改" placement="top" :enterable="false">
                <el-button size="mini" icon="el-icon-document-checked" type="info" @click="goGrade(scope.row)" circle />
              </el-tooltip>
              <template v-if="publishDisabledReason(scope.row)">
                <el-tooltip :content="publishDisabledReason(scope.row)" placement="top" :enterable="false">
                  <el-button size="mini" icon="el-icon-upload" type="success" :disabled="!!publishDisabledReason(scope.row)" @click="publish(scope.row)" circle />
                </el-tooltip>
              </template>
              <el-button v-else size="mini" icon="el-icon-upload" type="success" @click="publish(scope.row)" circle />
              <el-tooltip :content="isRunning(scope.row)?'结束考试':(Number(scope.row.status)===3?'已结束':'未开始')" placement="top" :enterable="false">
                <el-button size="mini" icon="el-icon-switch-button" type="info" @click="end(scope.row)" :disabled="!isRunning(scope.row)" circle />
              </el-tooltip>
              <template v-if="removeDisabled(scope.row)">
                <el-tooltip :content="removeDisabled(scope.row)" placement="top" :enterable="false">
                  <el-button size="mini" icon="el-icon-delete" type="danger" :disabled="!!removeDisabled(scope.row)" @click="remove(scope.row)" circle />
                </el-tooltip>
              </template>
              <el-button v-else size="mini" icon="el-icon-delete" type="danger" @click="remove(scope.row)" circle />
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="selection.length" class="batch-actions">
        <el-button size="mini" type="danger" :disabled="!canBulkDelete" @click="bulkDelete" :loading="bulkDeleting">批量删除 ({{ selection.length }})</el-button>
        <span class="batch-tip" v-if="!canBulkDelete">含进行中考试，无法批量删除</span>
      </div>
    </el-card>

    <!-- 批量发布对话框 -->
    <el-dialog title="批量发布考试" :visible.sync="batchVisible" width="680px" class="beautified-dialog" :modal="false" :lock-scroll="false" :close-on-click-modal="false">
      <div v-if="batchVisible" class="batch-dialog-body">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
          title="在左侧选择要发布到的课堂；右侧填写考试的基础信息与时间范围" />
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form label-width="90px" size="small">
              <el-form-item label="课堂选择">
                <el-select v-model="batch.sessionIds" multiple filterable collapse-tags placeholder="请选择课堂" style="width:100%">
                  <el-option v-for="s in sessions" :key="s.sessionId" :label="(s.className || ('课堂'+s.sessionId))" :value="s.sessionId" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围">
                <el-date-picker v-model="batch.timeRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束" style="width:100%" />
              </el-form-item>
              <el-form-item label="考试模式">
                <el-radio-group v-model="batch.exam.examMode">
                  <el-radio :label="1">定时</el-radio>
                  <el-radio :label="2">随到随考</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="显示答案">
                <el-select v-model="batch.exam.showAnswer" placeholder="策略" style="width:100%">
                  <el-option :value="0" label="不显示" />
                  <el-option :value="1" label="立即显示" />
                  <el-option :value="2" label="结束后显示" />
                </el-select>
              </el-form-item>
              <el-form-item label="防作弊">
                <el-switch v-model="batch.exam.antiCheat" />
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="90px" size="small">
              <el-form-item label="考试名称">
                <el-input v-model="batch.exam.examName" maxlength="50" placeholder="例如：第3次测验" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="batch.exam.examType" style="width:100%">
                  <el-option :value="1" label="期中" />
                  <el-option :value="2" label="期末" />
                  <el-option :value="3" label="测验" />
                  <el-option :value="4" label="模拟考" />
                  <el-option :value="5" label="课堂测验" />
                </el-select>
              </el-form-item>
              <el-form-item label="时长(分)">
                <el-input-number v-model="batch.exam.examDuration" :min="1" :max="1000" />
              </el-form-item>
              <el-form-item label="总分">
                <el-input-number v-model="batch.exam.totalScore" :min="1" :max="10000" />
              </el-form-item>
              <el-form-item label="及格分">
                <el-input-number v-model="batch.exam.passScore" :min="0" :max="10000" />
              </el-form-item>
              <el-form-item label="题序">
                <el-radio-group v-model="batch.exam.questionOrder">
                  <el-radio :label="0">正常</el-radio>
                  <el-radio :label="1">随机</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="自动交卷">
                <el-switch v-model="batch.exam.autoSubmit" />
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        <el-divider />
        <div class="batch-summary">
          <el-tag type="info">已选课堂：{{ batch.sessionIds.length }}</el-tag>
          <el-tag v-if="batch.timeRange && batch.timeRange.length===2" type="success" style="margin-left:8px">
            {{ formatDateTimeSimple(batch.timeRange[0]) }} ~ {{ formatDateTimeSimple(batch.timeRange[1]) }}
          </el-tag>
          <el-tag v-else type="warning" style="margin-left:8px">未选择时间范围</el-tag>
          <el-tag v-if="batch.exam.totalScore && batch.exam.passScore" style="margin-left:8px" :type="Number(batch.exam.passScore) <= Number(batch.exam.totalScore)?'success':'danger'">
            总分 {{ batch.exam.totalScore }} / 及格 {{ batch.exam.passScore }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <div style="text-align:right">
          <el-button @click="closeBatchDialog">取 消</el-button>
          <el-button type="primary" :disabled="!batchCanSubmit" :loading="batchLoading" @click="submitBatch">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑考试配置弹窗 -->
    <el-dialog title="编辑考试配置" :visible.sync="examEditVisible" width="760px" class="beautified-dialog" :modal="false" :lock-scroll="false" :close-on-click-modal="false">
      <el-form :model="examEditForm" label-width="100px" class="beautified-form" size="small">
        <el-form-item label="考试名称">
          <el-input v-model="examEditForm.examName" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="examEditTimeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="examEditForm.examDuration" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="总分/及格">
          <div class="score-container">
            <el-input-number v-model="examEditForm.totalScore" :precision="2" :step="1" :min="0" :max="1000" />
            <span class="score-divider">/</span>
            <el-input-number v-model="examEditForm.passScore" :precision="2" :step="1" :min="0" :max="1000" />
          </div>
        </el-form-item>
        <el-form-item label="考试模式">
          <el-radio-group v-model="examEditForm.examMode">
            <el-radio :label="1">定时考试</el-radio>
            <el-radio :label="2">随到随考</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目顺序">
          <el-radio-group v-model="examEditForm.questionOrder">
            <el-radio :label="0">正常顺序</el-radio>
            <el-radio :label="1">随机排序</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答案显示">
          <el-select v-model="examEditForm.showAnswer" style="width:260px">
            <el-option :value="0" label="不显示" />
            <el-option :value="1" label="立即显示" />
            <el-option :value="2" label="考试结束后" />
          </el-select>
        </el-form-item>
        <el-form-item label="防作弊">
          <el-switch v-model="examEditBool.antiCheat" />
        </el-form-item>
        <el-form-item label="其他设置">
          <el-checkbox v-model="examEditBool.autoSubmit">自动交卷</el-checkbox>
          <el-checkbox v-model="examEditBool.lateSubmit">允许迟交</el-checkbox>
          <el-input-number v-model="examEditForm.lateTime" :min="0" :max="1440" />
          <span class="time-unit">分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right">
          <el-button @click="examEditVisible=false">取 消</el-button>
          <el-button type="primary" :loading="examEditLoading" @click="confirmExamEdit">保存修改</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 右上角入口按钮 -->
    <el-button type="primary" icon="el-icon-s-operation" class="batch-float-btn" @click="openBatch" :disabled="!form.courseId || !sessions.length">批量发布</el-button>
  </div>
</template>

<script>
import { listExam, addExam, updateExam, publishExam, delExam, endExam, batchAddExam } from '@/api/proj_lwj/exam'
import { listCourse } from '@/api/proj_lw/course'
import request from '@/utils/request'

export default {
  name: 'ExamPublish',
  data() {
    return {
      courses: [],
      sessions: [],
      form: {
        id: null,
        courseId: null,
        sessionId: null,
        examName: '',
        examType: 3,
        startTime: null,
        endTime: null,
        examDuration: 60,
        totalScore: 100,
        passScore: 60,
        examMode: 1,
        questionOrder: 0,
        showAnswer: 0,
        lateTime: 0
      },
      bool: { antiCheat: false, autoSubmit: true, lateSubmit: false },
      timeRange: [],
      btnLoading: false,
      list: [],
      listLoading: false,
      query: { examName: '' },
      batchVisible: false,
      batchLoading: false,
      batch: {
        sessionIds: [],
        timeRange: [],
        exam: {
          courseId: null,
          examName: '',
          examType: 3,
          examDuration: 60,
          totalScore: 100,
          passScore: 60,
          examMode: 1,
          status: 0
        }
      },
      _pendingRestoreSessionId: null,
      rowKeyField: 'id',
      examEditVisible: false,
      examEditLoading: false,
      examEditForm: { id:null, examName:'', examType:3, examDuration:60, totalScore:100, passScore:60, examMode:1, questionOrder:0, showAnswer:0, lateTime:0, courseId:null, sessionId:null, questionCount:0, status:0, startTime:null, endTime:null },
      examEditTimeRange: [],
      examEditBool: { antiCheat:false, autoSubmit:true, lateSubmit:false },
      highlightedExamId: null, // 最近更新的考试行ID
      scoreSummary: null,
      selection: [],
      bulkDeleting: false,
      submissionStats: {}, // examId -> { loading, participantsCount, submittedCount, unsubmittedCount, submittedRate, averageScore, passRate, records:[], lastFetch, timer, error }
      expandedExamIds: [],
      // 排序相关
      sortField: null,
      sortOrder: null,
    }
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        // 记住上次选择的课程
        try { localStorage.setItem('exam_publish_lastCourseId', String(val)) } catch (e) {}
        this.fetchSessionsByCourseId(val)
      } else {
        this.sessions = []
        this.form.sessionId = null
        try { localStorage.removeItem('exam_publish_lastCourseId') } catch (e) {}
      }
    },
    'form.sessionId'(val) {
      if (val) {
        try { localStorage.setItem('exam_publish_lastSessionId', String(val)) } catch (e) {}
        this.loadList()
      } else {
        this.list = []
        try { localStorage.removeItem('exam_publish_lastSessionId') } catch (e) {}
      }
    }
  },
  created() {
    // 加载课程后尝试恢复上次选择
    this.fetchCourses().then(() => {
      this.restoreSelections()
    })
  },
  computed:{
    // 排序后的列表
    sortedList() {
      const rows = Array.isArray(this.list) ? this.list.slice() : []
      if (!this.sortField || !this.sortOrder) return rows

      const field = this.sortField
      const desc = this.sortOrder === 'descending'

      return rows.sort((a, b) => {
        let av, bv
        if (field === 'examName') {
          av = (a.examName || '').toLowerCase()
          bv = (b.examName || '').toLowerCase()
        } else if (field === 'startTime') {
          av = a.startTime ? new Date(a.startTime).getTime() : 0
          bv = b.startTime ? new Date(b.startTime).getTime() : 0
        } else if (field === 'examDuration') {
          av = Number(a.examDuration) || 0
          bv = Number(b.examDuration) || 0
        } else if (field === 'totalScore') {
          av = Number(a.totalScore) || 0
          bv = Number(b.totalScore) || 0
        } else if (field === 'questionCount') {
          av = Number(a.questionCount) || 0
          bv = Number(b.questionCount) || 0
        } else if (field === 'status') {
          av = Number(a.status) || 0
          bv = Number(b.status) || 0
        } else {
          return 0
        }
        if (av === bv) return 0
        return desc ? (av > bv ? -1 : 1) : (av > bv ? 1 : -1)
      })
    },
    // 用实际进行中判断禁止批量删除
    canBulkDelete(){ return this.selection.length>0 && this.selection.every(r => !this.isRunning(r)) },
    batchCanSubmit(){
      const b = this.batch
      if(!b) return false
      if(!b.sessionIds || !b.sessionIds.length) return false
      if(!b.exam.examName || !String(b.exam.examName).trim()) return false
      if(!b.timeRange || b.timeRange.length!==2) return false
      if(!b.exam.totalScore || Number(b.exam.totalScore) <= 0) return false
      if(b.exam.passScore == null || Number(b.exam.passScore) < 0) return false
      if(Number(b.exam.passScore) > Number(b.exam.totalScore)) return false
      return true
    }
  },
  methods: {
    // 处理表格排序变化
    handleSortChange({ prop, order }) {
      this.sortField = order ? prop : null
      this.sortOrder = order || null
    },
    // 所有methods方法都保持不变
    fetchCourses() {
      return listCourse({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.courses = res && res.rows ? res.rows : (res.data || [])
      })
    },
    // 恢复上次的课程/课堂选择
    restoreSelections() {
      try {
        const lastCourseId = localStorage.getItem('exam_publish_lastCourseId')
        const lastSessionId = localStorage.getItem('exam_publish_lastSessionId')
        if (lastCourseId && this.courses && this.courses.some(c => String(c.courseId) === String(lastCourseId))) {
          // 设置 courseId 会触发 watcher，进而加载 sessions
          this.form.courseId = Number(lastCourseId)
          // 暂存期望恢复的 sessionId，待 sessions 加载后再设置
          this._pendingRestoreSessionId = lastSessionId ? Number(lastSessionId) : null
        }
      } catch (e) {
        // 忽略本地存储错误
      }
    },
    fetchSessionsByCourseId(courseId) {
      const api = require('@/api/proj_lw/session')
      return api.getSessionsByCourseId(courseId).then(res => {
        this.sessions = res && res.rows ? res.rows : (res.data || [])
        // 优先恢复上次 sessionId（需属于当前课程的课堂列表）
        const sid = this._pendingRestoreSessionId
        if (sid && this.sessions.some(s => String(s.sessionId) === String(sid))) {
          this.form.sessionId = Number(sid)
        } else {
          // 如果当前未选择则置空
          if (!this.form.sessionId || !this.sessions.some(s => String(s.sessionId) === String(this.form.sessionId))) {
            this.form.sessionId = null
          }
        }
        // 清除待恢复标记
        this._pendingRestoreSessionId = null
        // 不在这里调用 loadList，交给 sessionId 的 watcher 统一处理
      })
    },
    onSessionChange(val) {
      this.form.sessionId = val
      // 选择课堂后，自动加载该课堂的考试列表
      this.loadList()
    },
    fmt(v) {
      if (!v) return '—'
      const d = new Date(v)
      if (isNaN(d.getTime())) return v
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    // 新增：根据当前时间动态计算状态文案
    statusDisplay(row) {
      if (!row) return '未知'
      const status = Number(row.status || 0)
      const start = this.parseDateTime(row.startTime)
      const end = this.parseDateTime(row.endTime)
      const now = Date.now()
      if (status === 3) return '已结束'
      if (!start || !end) {
        return {0:'草稿',1:'已发布',2:'进行中',3:'已结束'}[status] || '未知'
      }
      if (now < start) return status === 0 ? '草稿' : '已发布'
      if (now >= start && now < end) return status === 0 ? '草稿' : '进行中'
      return status === 3 ? '已结束' : '已截止'
    },
    statusType(row) {
      if (!row) return 'info'
      const status = Number(row.status || 0)
      const start = this.parseDateTime(row.startTime)
      const end = this.parseDateTime(row.endTime)
      const now = Date.now()
      if (status === 3) return 'danger'
      if (!start || !end) {
        return {0:'info',1:'',2:'success',3:'danger'}[status] || 'info'
      }
      if (now < start) return status === 0 ? 'info' : ''
      if (now >= start && now < end) return status === 0 ? 'info' : 'success'
      return status === 3 ? 'danger' : 'warning'
    },
    // ================= 新增：统一的安全时间字符串解析（避免不同浏览器对 'YYYY-MM-DD HH:mm:ss' 解析差异） =================
    parseDateTime(value) {
      if (!value) return null
      if (value instanceof Date) return isNaN(value.getTime()) ? null : value.getTime()
      if (typeof value === 'number') return value
      if (typeof value !== 'string') return null
      // 替换 T 为 space，裁剪
      const str = value.trim().replace('T', ' ').replace(/\..*/, '')
      // 允许格式: YYYY-MM-DD HH:mm:ss 或 YYYY-MM-DD HH:mm 或 YYYY-MM-DD
      const m = str.match(/^(\d{4})-(\d{2})-(\d{2})(?:[\s]+(\d{2}):(\d{2})(?::(\d{2}))?)?$/)
      if (!m) return null
      const [_, Y, M, D, h='00', mnt='00', s='00'] = m
      const date = new Date(Number(Y), Number(M)-1, Number(D), Number(h), Number(mnt), Number(s))
      const t = date.getTime()
      return isNaN(t) ? null : t
    },
    // 判断是否处于实际进行中（基于时间窗口 + 已发布或后端标记进行中）
    isRunning(row) {
      if (!row) return false
      // 只检查status是否为2（进行中），与后端删除逻辑保持一致
      // 只要不是进行中状态，都可以删除（草稿/已发布/已结束均可删除）
      return Number(row.status) === 2
    },
    buildPayload(publishNow) {
      const payload = Object.assign({}, this.form)
      payload.antiCheat = this.bool.antiCheat ? 1 : 0
      payload.autoSubmit = this.bool.autoSubmit ? 1 : 0
      payload.lateSubmit = this.bool.lateSubmit ? 1 : 0
      if (Array.isArray(this.timeRange) && this.timeRange.length === 2) {
        payload.startTime = this.formatDateTime(this.timeRange[0])
        payload.endTime = this.formatDateTime(this.timeRange[1])
      }
      if (publishNow) payload.status = 1
      return payload
    },
    formatDateTime(value) {
      const d = value instanceof Date ? value : new Date(value)
      if (isNaN(d.getTime())) return null
      const Y = d.getFullYear(), M = String(d.getMonth()+1).padStart(2,'0'), D = String(d.getDate()).padStart(2,'0')
      const h = String(d.getHours()).padStart(2,'0'), m = String(d.getMinutes()).padStart(2,'0'), s = String(d.getSeconds()).padStart(2,'0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },
    // 新增: 简单日期时间格式化(编辑弹窗使用)
    formatDateTimeSimple(d) {
      if (!d) return null
      if (!(d instanceof Date)) d = new Date(d)
      if (isNaN(d.getTime())) return null
      const Y=d.getFullYear(),M=String(d.getMonth()+1).padStart(2,'0'),D=String(d.getDate()).padStart(2,'0'),h=String(d.getHours()).padStart(2,'0'),m=String(d.getMinutes()).padStart(2,'0'),s=String(d.getSeconds()).padStart(2,'0')
      return `${Y}-${M}-${D} ${h}:${m}:${s}`
    },
    validate() {
      if (!this.form.courseId) { this.$message.error('请选择课程'); return false }
      if (!this.form.sessionId) { this.$message.error('请选择课堂'); return false }
      if (!this.form.examName) { this.$message.error('请输入考试名称'); return false }
      if (!this.timeRange || this.timeRange.length !== 2) { this.$message.error('请选择考试时间段'); return false }
      return true
    },
    // 新增：从基本配置直接进入题目配置
    async goToQuestionsFromForm() {
      if (!this.validate()) return
      this.btnLoading = true
      try {
        let examId = this.form.id
        // 如果还没有保存过考试，先插入一条草稿记录
        if (!examId) {
          const payload = this.buildPayload(false)
          payload.status = 0 // 强制草稿
          const res = await addExam(payload)
          if (!res || !(res.code === 200 || res.code === 0)) {
            this.$message.error((res && (res.msg || res.message)) || '创建考试失败')
            this.btnLoading = false
            return
          }
          // 尝试多种方式从响应中获取新建考试ID
          examId = (res.data && (res.data.id || res.data.examId)) || res.id || res.examId || payload.id

          // 如果仍然没拿到ID，则尝试通过重新加载列表、按名称匹配找到最新一条考试
          if (!examId) {
            try {
              const listRes = await listExam({
                courseId: this.form.courseId,
                sessionId: this.form.sessionId,
                examName: this.form.examName,
                pageNum: 1,
                pageSize: 20
              })
              const rows = listRes && (listRes.rows || res.data) ? (listRes.rows || res.data) : []
              const candidates = rows
                .filter(e => e.examName === this.form.examName)
                .sort((a, b) => new Date(b.startTime || 0) - new Date(a.startTime || 0))
              if (candidates.length > 0) {
                examId = candidates[0].id
              }
            } catch (e) {
              console.warn('fallback listExam to resolve examId failed', e)
            }
          }

          if (!examId) {
            this.$message.error('未获取到考试ID，无法进入题目配置')
            this.btnLoading = false
            return
          }
          this.form.id = examId
        }
        this.btnLoading = false
        const path = `/proj_lwj_exam/exam_questions/${examId}`
        this.$router.push({ path }).catch(() => {})
      } catch (e) {
        console.error('goToQuestionsFromForm error', e)
        this.btnLoading = false
        this.$message.error('进入题目配置失败，请重试')
      }
    },
    // 保留 onSave 以兼容后续可能在别处复用，但当前表单不再直接调用 onSave
    onSave(publishNow) {
      if (!this.validate()) return
      const payload = this.buildPayload(publishNow)
      this.btnLoading = true
      const req = !payload.id ? addExam(payload) : updateExam(payload)
      req.then(res => {
        this.btnLoading = false
        if (res && (res.code === 200 || res.code === 0)) {
          if (publishNow && payload.id) {
            publishExam(payload.id)
              .then(() => {
                this.loadList()
                this.$message.success('发布成功')
              })
              .catch(err => {
                console.error('发布失败:', err)
                let errorMsg = '发布失败'
                if (err && err.response && err.response.data) {
                  const data = err.response.data
                  errorMsg = data.msg || data.message || errorMsg
                } else if (err && err.message) {
                  errorMsg = err.message
                }
                this.$message.error(errorMsg)
                this.loadList()
              })
          } else {
            this.loadList()
            this.$message.success('保存成功')
          }
          if (!payload.id) this.resetForm()
        } else {
          this.$message.error((res && (res.msg || res.message)) || '保存失败')
        }
      }).catch(err => { this.btnLoading = false; console.error(err); this.$message.error('操作失败') })
    },
    resetForm() {
      this.form = { id:null, courseId:this.form.courseId, sessionId:this.form.sessionId, examName:'', examType:3, examDuration:60, totalScore:100, passScore:60, examMode:1, questionOrder:0, showAnswer:0, lateTime:0 }
      this.bool = { antiCheat: false, autoSubmit:false, lateSubmit:false }
      this.timeRange = []
    },
    rowClassName({ row }) {
      if (this.highlightedExamId && Number(row.id) === Number(this.highlightedExamId)) {
        return 'row-updated'
      }
      return ''
    },
    validateExamEdit() {
      const f = this.examEditForm
      if (!f.examName || !String(f.examName).trim()) { this.$message.error('请输入考试名称'); return false }
      if (!this.examEditTimeRange || this.examEditTimeRange.length !== 2) { this.$message.error('请选择时间范围'); return false }
      const start = this.examEditTimeRange[0]
      const end = this.examEditTimeRange[1]
      const startMs = start instanceof Date ? start.getTime() : new Date(start).getTime()
      const endMs = end instanceof Date ? end.getTime() : new Date(end).getTime()
      const now = Date.now()
      if (isNaN(startMs) || isNaN(endMs)) { this.$message.error('时间格式不正确'); return false }
      if (startMs >= endMs) { this.$message.error('开始时间必须早于结束时间'); return false }
      const status = Number(f.status || 0)
      // 状态相关约束：进行中 / 已结束 时间不可与当前实际冲突
      if (status === 2) { // 进行中
        if (!(startMs <= now && now < endMs)) {
          this.$message.error('进行中考试的时间范围必须包含现在时间'); return false }
      } else if (status === 3) { // 已结束
        if (!(endMs <= now)) { this.$message.error('已结束考试的结束时间必须早于当前时间'); return false }
      }
      // 可选：防止草稿/已发布设置过期结束时间（提示但不阻止）
      if ((status === 0 || status === 1) && endMs < now) {
        this.$message.warning('注意：结束时间已在过去，发布后将立即视为已截止')
      }
      if (!f.totalScore || Number(f.totalScore) <= 0) { this.$message.error('总分需大于0'); return false }
      if (Number(f.passScore) > Number(f.totalScore)) { this.$message.error('及格分不能大于总分'); return false }
      return true
    },
    confirmExamEdit() {
      if (!this.validateExamEdit()) return
      this.examEditLoading = true
      const f = this.examEditForm
      const [start, end] = this.examEditTimeRange
      const payload = Object.assign({}, f, {
        startTime: this.formatDateTimeSimple(start),
        endTime: this.formatDateTimeSimple(end),
        antiCheat: this.examEditBool.antiCheat ? 1 : 0,
        autoSubmit: this.examEditBool.autoSubmit ? 1 : 0,
        lateSubmit: this.examEditBool.lateSubmit ? 1 : 0
      })
      updateExam(payload).then(res => {
        this.examEditLoading = false
        if (res && (res.code === 200 || res.code === 0)) {
          this.$message.success('已更新考试配置')
          this.examEditVisible = false
          this.highlightedExamId = payload.id
          this.loadList()
          // 5 秒后取消高亮
          setTimeout(()=>{ this.highlightedExamId = null }, 5000)
        } else {
          this.$message.error((res && (res.msg || res.message)) || '更新失败')
        }
      }).catch(err => { this.examEditLoading = false; console.error(err); this.$message.error('更新失败') })
    },
    // ================= 新增缺失方法，解决模板引用 undefined 警告 =================
    // 加载当前课堂的考试列表
    loadList() {
      if (!this.form.sessionId) { this.list = []; return }
      this.listLoading = true
      const params = {
        courseId: this.form.courseId,
        sessionId: this.form.sessionId,
        examName: this.query.examName || undefined,
        pageNum: 1,
        pageSize: 200
      }
      listExam(params).then(res => {
        this.listLoading = false
        const rows = res && (res.rows || res.data) ? (res.rows || res.data) : []
        // 规范字段：确保有 id / questionCount
        this.list = rows.map(r => ({
          id: r.id || r.examId,
          examName: r.examName,
          examType: r.examType,
          startTime: r.startTime,
          endTime: r.endTime,
          examDuration: r.examDuration,
          totalScore: r.totalScore,
          passScore: r.passScore,
          examMode: r.examMode,
          questionOrder: r.questionOrder,
          showAnswer: r.showAnswer,
          lateTime: r.lateTime,
          antiCheat: r.antiCheat,
          autoSubmit: r.autoSubmit,
          lateSubmit: r.lateSubmit,
          status: r.status,
          courseId: r.courseId,
          sessionId: r.sessionId,
          questionCount: r.questionCount || r.questionsCount || 0
        }))
        if (this.form.id){ this.fetchScoreSummary(this.form.id) }
      }).catch(err => { this.listLoading = false; console.error('loadList error', err) })
    },
    // 进入题目配置（表格行内）
    gotoQuestions(row) {
      if (!row || !row.id) return
      if (this.isEndedOrExpired(row)) {
        return this.$message.warning('该考试已结束/已截止，不能再配置题目')
      }
      this.$router.push({ path: `/proj_lwj_exam/exam_questions/${row.id}` }).catch(()=>{})
      this.fetchScoreSummary(row.id)
    },
    // 编辑配置（表格行内）
    openExamEdit(row) {
      if(!row || !row.id) return
      const st = Number(row.status)
      if(st===2) { return this.$message.error('进行中考试不可编辑') }
      if(st===3) { return this.$message.error('已结束考试不可编辑') }
      this.handleEdit(row)
    },
    // 打开编辑对话框并填充数据
    handleEdit(row) {
      this.examEditForm = {
        id: row.id,
        examName: row.examName,
        examType: row.examType,
        examDuration: row.examDuration,
        totalScore: row.totalScore,
        passScore: row.passScore,
        examMode: row.examMode,
        questionOrder: row.questionOrder,
        showAnswer: row.showAnswer,
        lateTime: row.lateTime,
        courseId: row.courseId,
        sessionId: row.sessionId,
        questionCount: row.questionCount,
        status: row.status,
        startTime: row.startTime,
        endTime: row.endTime
      }
      this.examEditTimeRange = [
        row.startTime ? new Date(row.startTime) : null,
        row.endTime ? new Date(row.endTime) : null
      ]
      this.examEditBool = {
        antiCheat: row.antiCheat === 1,
        autoSubmit: row.autoSubmit === 1,
        lateSubmit: row.lateSubmit === 1
      }
      this.examEditVisible = true
    },
    // 发布考试
    publish(row) {
      if (!row || !row.id) return
      if (!row.questionCount || row.questionCount <= 0) { return this.$message.error('请先配置题目后再发布') }
      publishExam(row.id).then(res => {
        if (res && (res.code === 200 || res.code === 0)) {
          this.$message.success('已发布')
          this.loadList()
        } else {
          // 显示后端返回的详细错误信息
          const errorMsg = (res && (res.msg || res.message)) || '发布失败'
          this.$message.error(errorMsg)
        }
      }).catch(e => {
        console.error('发布考试失败:', e)
        // 提取详细错误信息
        let errorMsg = '发布失败'
        if (e && e.response && e.response.data) {
          const data = e.response.data
          errorMsg = data.msg || data.message || errorMsg
        } else if (e && e.message) {
          errorMsg = e.message
        }
        this.$message.error(errorMsg)
      })
    },
    // 删除考试（草稿或已发布但未开始）
    remove(row) {
      if (!row || !row.id) return
      // 运行中保护（双保险，按钮应已禁用）
      if (this.isRunning(row)) {
        this.$message.error('进行中的考试不可删除')
        return
      }
      this.$confirm('确定删除该考试？删除后不可恢复', '提示', { type:'warning' }).then(()=>{
        delExam(row.id).then(res => {
          if (res && (res.code===200 || res.code===0)) { this.$message.success('已删除'); this.loadList() } else { this.$message.error((res && (res.msg||res.message)) || '删除失败') }
        }).catch(e => { console.error(e); this.$message.error('删除失败') })
      }).catch(()=>{})
    },
    // 批量删除
    onSelectionChange(rows){ this.selection = rows || [] },
    bulkDelete(){
      if(!this.canBulkDelete) return this.$message.error('包含进行中考试，不能批量删除')
      const ids = this.selection.map(r=>r.id)
      this.$confirm(`确定删除选中的 ${ids.length} 个考试？此操作不可恢复`, '提示', { type:'warning' }).then(()=>{
        this.bulkDeleting = true
        // 后端批量删除接口使用 DELETE /proj_lwj/exam/{ids}
        request({ url:`/proj_lwj/exam/${ids.join(',')}`, method:'delete'}).then(res=>{
          this.bulkDeleting=false
          if(res && (res.code===200 || res.code===0)) { this.$message.success('批量删除成功'); this.loadList(); this.selection=[] } else { this.$message.error((res && (res.msg||res.message)) || '批量删除失败') }
        }).catch(e=>{ this.bulkDeleting=false; console.error(e); this.$message.error('批量删除失败') })
      }).catch(()=>{})
    },
    // 发布按钮禁用原因
    publishDisabledReason(row) {
      if (!row) return '数据异常'
      if (Number(row.status) === 1 || Number(row.status) === 2 || Number(row.status) === 3) return '已发布'
      if (!row.questionCount || row.questionCount <= 0) return '未配置题目'
      if (!row.totalScore || Number(row.totalScore) <= 0) return '总分未设置'
      return ''
    },
    // 删除禁用原因
    removeDisabled(row) {
      if (!row) return '数据异常'
      // 使用实际进行中判断，而不是仅依赖后端 status===2
      if (this.isRunning(row)) return '进行中不可删除'
      return ''
    },
    // 打开批量创建对话框
    openBatch() {
      if (!this.form.courseId) { return this.$message.error('请先选择课程') }
      if (!this.sessions.length) { return this.$message.error('当前课程暂无课堂') }
      // 初始化批量参数
      this.batch.sessionIds = []
      this.batch.exam.courseId = this.form.courseId
      this.batch.exam.examName = this.form.examName || ''
      this.batch.exam.examType = this.form.examType
      this.batch.exam.examDuration = this.form.examDuration
      this.batch.exam.totalScore = this.form.totalScore
      this.batch.exam.passScore = this.form.passScore
      this.batch.exam.examMode = this.form.examMode
      this.batch.exam.status = 0
      this.batch.timeRange = this.timeRange.slice()
      this.batchVisible = true
    },
    // 关闭批量发布对话框
    closeBatchDialog(){ this.batchVisible=false },
    // 提交批量创建
    submitBatch() {
      if (!this.batch.sessionIds || !this.batch.sessionIds.length) { return this.$message.error('请选择至少一个课堂') }
      if (!this.batch.exam.examName) { return this.$message.error('请输入考试名称') }
      if (!this.batch.timeRange || this.batch.timeRange.length !== 2) { return this.$message.error('请选择时间范围') }
      const [start, end] = this.batch.timeRange
      const payload = {
        sessionIds: this.batch.sessionIds,
        exam: Object.assign({}, this.batch.exam, {
          startTime: this.formatDateTimeSimple(start),
          endTime: this.formatDateTimeSimple(end)
        })
      }
      this.batchLoading = true
      batchAddExam(payload).then(res => {
        this.batchLoading = false
        if (res && (res.code===200 || res.code===0)) {
          this.$message.success('批量创建成功')
          this.batchVisible = false
          this.loadList()
        } else {
          this.$message.error((res && (res.msg||res.message)) || '批量创建失败')
        }
      }).catch(e => { this.batchLoading=false; console.error(e); this.$message.error('批量创建失败') })
    },
    fetchScoreSummary(examId){
      if (!examId) { this.scoreSummary = null; return }
      request({ url: `/proj_lwj/exam/question/${examId}/score-summary`, method: 'get'}).then(res => {
        if (res && res.code === 200) this.scoreSummary = res.data
        else this.scoreSummary = null
      }).catch(()=>{ this.scoreSummary=null })
    },
    scoreSummaryTitle(){
      if (!this.scoreSummary) return ''
      const { actualTotalScore, configuredTotalScore, remainingToPublish, canPublish, questionCount } = this.scoreSummary
      if (!configuredTotalScore || configuredTotalScore === 0){
        return `当前题目总分 ${actualTotalScore}，尚未设置考试总分，不能发布`
      }
      if (canPublish){
        return `题目总分 ${actualTotalScore} / 设置总分 ${configuredTotalScore}，已满足发布条件（题目数：${questionCount || 0}）`
      }
      return `题目总分 ${actualTotalScore} / 设置总分 ${configuredTotalScore}，距离可发布还差 ${remainingToPublish} 分（题目数：${questionCount || 0}）`
    },
    // 新增：跳转到批改页面
    goGrade(row) {
      // 跳转到考试批改详情路由
      this.$router.push({ path: `/proj_lwj_exam/exam_grading/${row.id}` })
    },
    isEndedOrExpired(row){
      if(!row) return false
      // 显式结束
      if(Number(row.status) === 3) return true
      const end = this.parseDateTime && this.parseDateTime(row.endTime)
      return !!end && Date.now() >= end
    },
    end(row){
      if(!row || !row.id) return
      // Allow ending when the exam is actually running (based on time window + status not ended),
      // because UI may display "进行中" derived from time even if backend status isn't updated to 2.
      if(!this.isRunning(row)){
        return this.$message.warning('仅进行中的考试可以结束')
      }
      this.$confirm('确定结束该考试？结束后学生不可继续作答','提示',{type:'warning'}).then(()=>{
        endExam(row.id).then(res=>{
          if(res && (res.code===200 || res.code===0)){
            this.$message.success('考试已结束')
            this.loadList()
          }else{
            this.$message.error((res && (res.msg||res.message)) || '结束失败')
          }
        }).catch(e=>{ console.error(e); this.$message.error('结束失败') })
      }).catch(()=>{})
    },
    fmtTime(ts){
      const d = new Date(ts); const pad=n=>String(n).padStart(2,'0');
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    handleExpandChange(row, expandedRows){
      this.expandedExamIds = expandedRows.map(r=> r.id || r.examId)
      const isExpanded = this.expandedExamIds.includes(row.id)
      if(isExpanded){
        this.ensureSubmissionStats(row)
      } else {
        // 折叠时停止自动刷新
        const stat = this.submissionStats[row.id]
        if(stat && stat.timer){ clearInterval(stat.timer); stat.timer=null }
      }
    },
    ensureSubmissionStats(row){
      if(!row || !row.id) return
      const stat = this.submissionStats[row.id]
      const now = Date.now()
      if(!stat || !stat.lastFetch || (now - stat.lastFetch > 15000)){
        this.fetchSubmissionStats(row)
      }
      // 若无定时器，则开启自动刷新
      if(!this.submissionStats[row.id].timer){
        this.startAuto(row)
      }
    },
    startAuto(row){
      if(!row || !row.id) return
      const stat = this.submissionStats[row.id] || {}
      if(stat.timer) return
      stat.timer = setInterval(()=> this.fetchSubmissionStats(row, true), 30000)
      this.$set(this.submissionStats, row.id, stat)
    },
    stopAuto(row){
      if(!row || !row.id) return
      const stat = this.submissionStats[row.id]
      if(stat && stat.timer){ clearInterval(stat.timer); stat.timer=null }
    },
    forceRefresh(row){ this.fetchSubmissionStats(row, false, true) },
    async fetchSubmissionStats(row, silent=false, force=false){
      if(!row || !row.id) return
      const examId = row.id
      if(!this.submissionStats[examId]){ this.$set(this.submissionStats, examId, { loading:false, records:[], submittedCount:0, participantsCount:0, unsubmittedCount:0, submittedRate:0, averageScore:null, passRate:null, lastFetch:null, timer:null, error:null }) }
      const stat = this.submissionStats[examId]
      if(stat.loading) return
      stat.loading = true
      stat.error = null
      try {
        // 获取参与人数等概要
        const summaryResp = await request({ url: `/proj_lwj/exam/${examId}/questionCorrectSummary`, method:'get' })
        const summaryData = summaryResp && (summaryResp.data || summaryResp)
        const participantsCount = Number(summaryData.participantsCount || 0)
        // 获取所有答案
        const ansResp = await request({ url:'/proj_lwj/exam/answer/list', method:'get', params:{ examId } })
        const answersRaw = ansResp && (ansResp.rows || ansResp.data || ansResp.list || [])
        const answers = Array.isArray(answersRaw) ? answersRaw : []
        const byStudent = {}
        answers.forEach(a => {
          const no = a.studentNo || a.student_no
          if(!no) return
          if(!byStudent[no]) byStudent[no] = { studentNo: no, answers: [], totalScore:0, scoreCount:0 }
          byStudent[no].answers.push(a)
          const s = a.score
          if(s!=null && s!=='' && !isNaN(Number(s))){ byStudent[no].totalScore += Number(s); byStudent[no].scoreCount += 1 }
        })
        const submittedStudents = Object.keys(byStudent)
        const submittedCount = submittedStudents.length
        const passScore = Number(row.passScore || row.pass_score || 0)
        const records = submittedStudents.map(no => {
          const info = byStudent[no]
          const score = info.scoreCount > 0 ? info.totalScore : null
          return {
            studentNo: no,
            submitted: true,
            score: score,
            passed: score != null && passScore>0 ? (score >= passScore) : false,
            answerCount: info.answers.length
          }
        })
        // 若无法获取未提交学生名单，仅统计数量
        const unsubmittedCount = Math.max(participantsCount - submittedCount, 0)
        // 统计平均分与及格率（仅对已提交学生且有得分）
        const scored = records.filter(r => r.score != null && !isNaN(r.score))
        const averageScore = scored.length ? (scored.reduce((sum,r)=>sum+r.score,0)/scored.length) : null
        const passedCount = records.filter(r => r.passed).length
        const passRate = submittedCount ? (passedCount / submittedCount * 100) : null
        const submittedRate = participantsCount ? (submittedCount / participantsCount * 100) : 0
        Object.assign(stat, { participantsCount, submittedCount, unsubmittedCount, submittedRate, averageScore, passRate, records, lastFetch: Date.now() })
      } catch(e){
        console.warn('fetchSubmissionStats error', e)
        stat.error = silent ? null : ('统计加载失败: ' + (e.message || '网络错误'))
      } finally { stat.loading = false }
    },
    // Export submission statistics CSV for a given exam row
    exportSubmissionStats(row){
      if(!row || !row.id){ this.$message.error('请选择考试'); return }
      const stat = this.submissionStats[row.id]
      if(!stat || stat.loading){ this.$message.warning('统计尚未加载完成'); return }
      const headers = ['学号','提交状态','得分','及格','答题数']
      const lines = [headers.join(',')]
      const recs = Array.isArray(stat.records) ? stat.records : []
      recs.forEach(r=>{
        const submitted = r.submitted ? '已提交' : '未提交'
        const score = r.score == null ? '' : r.score
        const passed = r.passed ? '是' : '否'
        const rowArr = [r.studentNo||'', submitted, score, passed, r.answerCount||0]
        lines.push(rowArr.map(v => '"' + String(v).replace(/"/g,'""') + '"').join(','))
      })
      const summary = [
        '',
        `总人数: ${stat.participantsCount||0}`,
        `已提交: ${stat.submittedCount||0}`,
        `未提交: ${stat.unsubmittedCount||0}`,
        `平均分: ${stat.averageScore==null?'—':stat.averageScore.toFixed(1)}`,
        `及格率: ${stat.passRate==null?'—':(stat.passRate.toFixed(1)+'%')}`
      ].join(',')
      lines.push('')
      lines.push(summary)
      const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `考试提交统计_${row.examName || '考试'}_${this.fmtTime(Date.now()).replace(/[:\s]/g,'')}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
    },
    // Print submission statistics in a formatted page
    printSubmissionStats(row){
      if(!row || !row.id){ this.$message.error('请选择考试'); return }
      const stat = this.submissionStats[row.id]
      if(!stat || stat.loading){ this.$message.warning('统计尚未加载完成'); return }
      const title = `${row.examName || '考试'} - 提交统计`
      const cols = ['学号','提交状态','得分','及格','答题数']
      const rowsHtml = (stat.records||[]).map(r=>{
        const submitted = r.submitted ? '已提交' : '未提交'
        const score = r.score == null ? '—' : r.score
        const passed = r.passed ? '是' : '否'
        return `<tr><td>${r.studentNo||''}</td><td>${submitted}</td><td>${score}</td><td>${passed}</td><td>${r.answerCount||0}</td></tr>`
      }).join('')
      const summaryHtml = `
        <div class="stats">
          <div class="stat">总人数：${stat.participantsCount||0}</div>
          <div class="stat">已提交：${stat.submittedCount||0}</div>
          <div class="stat">未提交：${stat.unsubmittedCount||0}</div>
          <div class="stat">平均分：${stat.averageScore==null?'—':stat.averageScore.toFixed(1)}</div>
          <div class="stat">及格率：${stat.passRate==null?'—':(stat.passRate.toFixed(1)+'%')}</div>
        </div>`
      const html = `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>${title}</title>
        <style>
          body{font-family:Segoe UI,Arial,Helvetica,sans-serif;padding:20px;color:#303133}
          h1{font-size:20px;margin:0 0 12px}
          .stats{display:flex;gap:12px;margin-bottom:12px}
          .stat{border:1px solid #ddd;border-radius:6px;padding:8px 12px}
          .table{width:100%;border-collapse:collapse}
          .table th,.table td{border:1px solid #ddd;padding:6px 8px;font-size:12px}
          .table th{background:#f5f7fa;text-align:left}
          @media print{button{display:none}}
        </style>
      </head><body>
        <h1>${title}</h1>
        ${summaryHtml}
        <table class="table"><thead><tr>${cols.map(c=>`<th>${c}</th>`).join('')}</tr></thead><tbody>${rowsHtml}</tbody></table>
        <button onclick="window.print()" style="margin-top:12px">打印</button>
      </body></html>`
      const win = window.open('', '_blank')
      if(win){ win.document.open(); win.document.write(html); win.document.close(); win.focus(); }
    },
    // Export the entire exam list as CSV
    exportExamList(){
      const rows = Array.isArray(this.sortedList) ? this.sortedList : []
      if(!rows.length){ this.$message.warning('暂无考试数据'); return }
      const headers = ['考试ID','名称','类型','开始时间','结束时间','时长(分钟)','总分','及格分','题目数','状态']
      const lines = [headers.join(',')]
      const typeMap = {1:'期中',2:'期末',3:'测验',4:'模拟考',5:'课堂测验'}
      rows.forEach(r=>{
        const id = r.id || r.examId || ''
        const name = r.examName || ''
        const type = typeMap[Number(r.examType)||3] || '测验'
        const start = r.startTime ? this.fmt(r.startTime) : ''
        const end = r.endTime ? this.fmt(r.endTime) : ''
        const duration = r.examDuration!=null ? r.examDuration : ''
        const total = r.totalScore!=null ? r.totalScore : ''
        const pass = r.passScore!=null ? r.passScore : ''
        const qcnt = r.questionCount!=null ? r.questionCount : ''
        const status = this.statusDisplay(r)
        const arr = [id, name, type, start, end, duration, total, pass, qcnt, status]
        lines.push(arr.map(v => '"' + String(v).replace(/"/g,'""') + '"').join(','))
      })
      const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `考试列表_${this.fmtTime(Date.now()).replace(/[:\s]/g,'')}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
    },
    // Print the entire exam list in a formatted page
    printExamList(){
      const rows = Array.isArray(this.sortedList) ? this.sortedList : []
      if(!rows.length){ this.$message.warning('暂无考试数据'); return }
      const title = '考试列表'
      const cols = ['考试ID','名称','类型','开始时间','结束时间','时长(分钟)','总分','及格分','题目数','状态']
      const typeMap = {1:'期中',2:'期末',3:'测验',4:'模拟考',5:'课堂测验'}
      const rowsHtml = rows.map(r=>{
        const id = r.id || r.examId || ''
        const name = r.examName || ''
        const type = typeMap[Number(r.examType)||3] || '测验'
        const start = r.startTime ? this.fmt(r.startTime) : ''
        const end = r.endTime ? this.fmt(r.endTime) : ''
        const duration = r.examDuration!=null ? r.examDuration : ''
        const total = r.totalScore!=null ? r.totalScore : ''
        const pass = r.passScore!=null ? r.passScore : ''
        const qcnt = r.questionCount!=null ? r.questionCount : ''
        const status = this.statusDisplay(r)
        return `<tr>
          <td>${id}</td>
          <td>${name}</td>
          <td>${type}</td>
          <td>${start}</td>
          <td>${end}</td>
          <td>${duration}</td>
          <td>${total}</td>
          <td>${pass}</td>
          <td>${qcnt}</td>
          <td>${status}</td>
        </tr>`
      }).join('')
      const html = `<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"utf-8\"><title>${title}</title>
        <style>
          body{font-family:Segoe UI,Arial,Helvetica,sans-serif;padding:20px;color:#303133}
          h1{font-size:20px;margin:0 0 12px}
          .table{width:100%;border-collapse:collapse}
          .table th,.table td{border:1px solid #ddd;padding:6px 8px;font-size:12px}
          .table th{background:#f5f7fa;text-align:left}
          @media print{button{display:none}}
        </style>
      </head><body>
        <h1>${title}</h1>
        <table class=\"table\"><thead><tr>${cols.map(c=>`<th>${c}</th>`).join('')}</tr></thead><tbody>${rowsHtml}</tbody></table>
        <button onclick=\"window.print()\" style=\"margin-top:12px\">打印</button>
      </body></html>`
      const win = window.open('', '_blank')
      if(win){ win.document.open(); win.document.write(html); win.document.close(); win.focus(); }
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 16px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.hero-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 8px; /* el-card header has padding usually, body padding handled by form-grid */
  margin-bottom: 16px;
}

.hero-card ::v-deep .el-card__header {
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-wrap {
  display: flex;
  align-items: center;
}

.title-wrap i {
  font-size: 24px;
  color: #409eff;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.sub {
  font-size: 14px;
  color: #909399;
  margin-left: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-actions .el-button {
  margin-left: 12px;
}

.form-grid {
  padding: 16px 0;
}

.w-100 {
  width: 100%;
}

.form-hint {
  margin-top: 8px;
}

.list-card {
  background-color: #ffffff;
  border-radius: 8px;
}

/* Reusing existing styles for table/list actions if needed, or overriding */
.search-input {
  width: 240px;
}
.batch-float-btn {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 1000;
}

/* Keeping necessary list styles */
.op-actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.status-tag {
  border: none;
  font-weight: 600;
}

.batch-actions {
    margin-top: 16px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 4px;
}

/* Submission panel styles needed for the expand row */
.exam-submission-panel { background:#fafafa; padding:12px 16px; border:1px solid #ebeef5; border-radius:8px; }
.submission-summary { display:flex; flex-wrap:wrap; gap:16px; align-items:flex-start; }
.ring-wrapper { display:flex; align-items:center; justify-content:center; }
.ring { width:120px; height:120px; border-radius:50%; position:relative; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 6px rgba(0,0,0,.08); }
.ring-inner { position:absolute; width:84px; height:84px; background:#fff; border-radius:50%; display:flex; flex-direction:column; align-items:center; justify-content:center; font-size:14px; box-shadow:0 0 0 4px rgba(255,255,255,.6); }
.ring-text { font-size:12px; color:#909399; margin-top:2px; }
.stats-cards { display:grid; grid-template-columns:repeat(auto-fill, minmax(110px,1fr)); gap:8px; flex:1; }
.mini-stat { text-align:center; }
.mini-stat .stat-title { font-size:12px; color:#909399; }
.mini-stat .stat-value { font-size:18px; font-weight:600; margin-top:4px; }
.success-text { color:#67c23a; }
.warning-text { color:#e6a23c; }
.submission-actions { margin-top:12px; display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.last-fetch { font-size:12px; color:#909399; }
.error-tip { font-size:12px; color:#f56c6c; }
.process-alert, .score-alert { margin-bottom: 16px; }

</style>
