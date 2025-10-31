<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <span class="page-title">课堂详情</span>
        <span class="course-name">{{ courseName }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['projlw:session:add']">
          新增课堂
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 课堂列表 -->
    <div v-loading="loading" class="session-list">
      <el-card v-for="session in sessionList" :key="session.sessionId" class="session-card">
        <div class="session-header">
          <h3 class="session-name">{{ session.className }}</h3>
          <div class="session-actions">
            <el-tag :type="getStatusType(session.status)">
              {{ getStatusText(session.status) }}
            </el-tag>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(session)"
              v-hasPermi="['projlw:session:edit']"
            >编辑</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(session)"
              v-hasPermi="['projlw:session:remove']"
            >删除</el-button>
          </div>
        </div>

        <div class="session-info">
          <div class="info-item">
            <span class="label">授课老师：</span>
            <span class="value">{{ session.teacher || '未分配' }}</span>
          </div>
          <div class="info-item">
            <span class="label">上课时间：</span>
            <span class="value">{{ parseTime(session.startTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <div class="info-item">
            <span class="label">下课时间：</span>
            <span class="value">{{ parseTime(session.endTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </div>
          <div class="info-item">
            <span class="label">课堂人数：</span>
            <span class="value">{{ session.totalStudents || 0 }}人</span>
          </div>
          <div class="info-item">
            <span class="label">课堂编号：</span>
            <span class="value">{{ session.classNumber }}</span>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="sessionList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无课堂数据">
          <el-button type="primary" @click="handleAdd" v-hasPermi="['projlw:session:add']">新增课堂</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 添加或修改课堂对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课堂名称" prop="className">
          <el-input v-model="form.className" placeholder="请输入课堂名称" />
        </el-form-item>
        <el-form-item label="授课老师" prop="teacher">
          <el-input v-model="form.teacher" placeholder="请输入授课老师姓名" />
        </el-form-item>
        <el-form-item label="老师ID" prop="teacherId">
          <el-input-number v-model="form.teacherId" :min="1" placeholder="请输入老师ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上课时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择上课时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="下课时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择下课时间"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="课堂人数" prop="totalStudents">
          <el-input-number v-model="form.totalStudents" :min="1" :max="200" placeholder="请输入课堂人数" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课堂状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">未开始</el-radio>
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级编号">
          <el-input v-model="form.classNumber" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSessionsByClassNumber, getSession, addSession, updateSession, delSession } from "@/api/proj_lw/session";

export default {
  name: "ClassSession",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 提交加载状态
      submitLoading: false,
      // 课堂列表
      sessionList: [],
      // 课程名称
      courseName: '',
      // 课程编号
      classNumber: null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        className: [
          { required: true, message: "课堂名称不能为空", trigger: "blur" }
        ],
        teacher: [
          { required: true, message: "授课老师不能为空", trigger: "blur" }
        ],
        teacherId: [
          { required: true, message: "老师ID不能为空", trigger: "blur" }
        ],
        startTime: [
          { required: true, message: "请选择上课时间", trigger: "change" }
        ],
        endTime: [
          { required: true, message: "请选择下课时间", trigger: "change" }
        ],
        totalStudents: [
          { required: true, message: "请输入课堂人数", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择课堂状态", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.classNumber = parseInt(this.$route.query.classNumber);
    this.courseName = this.$route.query.courseName;
    this.getList();
  },
  methods: {
    /** 查询课堂列表 */
    getList() {
      this.loading = true;
      getSessionsByClassNumber(this.classNumber).then(response => {
        this.sessionList = response.data || [];
        this.loading = false;
      }).catch(error => {
        console.error("获取课堂列表失败:", error);
        this.sessionList = [];
        this.loading = false;
      });
    },
    /** 返回按钮操作 */
    handleBack() {
      this.$router.push('/proj_lw/course');
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        sessionId: null,
        className: null,
        teacherId: 1,
        teacher: null,
        startTime: null,
        endTime: null,
        status: 0,
        totalStudents: 30,
        classNumber: this.classNumber
      };
      this.resetForm("form");
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加课堂";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const sessionId = row.sessionId;
      getSession(sessionId).then(response => {
        this.form = response.data;
        // 确保数字字段有值
        this.form.totalStudents = this.form.totalStudents || 30;
        this.form.teacherId = this.form.teacherId || 1;
        // 转换日期格式
        if (this.form.startTime) {
          this.form.startTime = this.parseTime(this.form.startTime, '{y}-{m}-{d} {h}:{i}:{s}');
        }
        if (this.form.endTime) {
          this.form.endTime = this.parseTime(this.form.endTime, '{y}-{m}-{d} {h}:{i}:{s}');
        }
        this.open = true;
        this.title = "修改课堂";
      }).catch(error => {
        console.error("获取课堂详情失败:", error);
        this.$modal.msgError("获取课堂详情失败");
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitLoading = true;

          // 确保数字字段有值
          this.form.totalStudents = this.form.totalStudents || 30;
          this.form.teacherId = this.form.teacherId || 1;
          this.form.classNumber = this.classNumber;

          // 创建深拷贝，避免修改原始数据
          const submitData = { ...this.form };

          if (submitData.sessionId != null) {
            // 修改操作
            updateSession(submitData).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error("修改课堂失败:", error);
              this.$modal.msgError("修改失败: " + (error.message || "请检查数据格式"));
            }).finally(() => {
              this.submitLoading = false;
            });
          } else {
            // 新增操作
            addSession(submitData).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error("新增课堂失败:", error);
              this.$modal.msgError("新增失败: " + (error.message || "请检查数据格式"));
            }).finally(() => {
              this.submitLoading = false;
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const sessionId = row.sessionId;
      this.$modal.confirm('是否确认删除课堂"' + row.className + '"？').then(() => {
        return delSession(sessionId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 获取状态文本 */
    getStatusText(status) {
      const statusMap = {
        0: '未开始',
        1: '进行中',
        2: '已结束'
      };
      return statusMap[status] || '未知';
    },
    /** 获取状态类型 */
    getStatusType(status) {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning'
      };
      return typeMap[status] || 'info';
    }
  }
};
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.course-name {
  font-size: 16px;
  color: #606266;
  margin-top: 5px;
}

.session-list {
  min-height: 200px;
}

.session-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.session-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.session-name {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.session-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
}

.label {
  color: #909399;
  min-width: 80px;
  font-weight: 500;
}

.value {
  color: #606266;
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}
</style>
