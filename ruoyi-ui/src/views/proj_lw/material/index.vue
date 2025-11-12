<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <span class="page-title">资料推送管理</span>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-upload" @click="handleUpload" :disabled="!currentSession">
          上传资料
        </el-button>
      </div>
    </div>

    <!-- 课程和课堂选择 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="选择课程" prop="courseId">
          <el-select
            v-model="queryParams.courseId"
            placeholder="请选择课程"
            clearable
            size="small"
            @change="handleCourseChange"
            style="width: 200px"
          >
            <el-option
              v-for="course in courseList"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择课堂" prop="sessionId">
          <el-select
            v-model="queryParams.sessionId"
            placeholder="请先选择课程"
            clearable
            size="small"
            @change="handleSessionChange"
            style="width: 200px"
            :disabled="!queryParams.courseId"
          >
            <el-option
              v-for="session in sessionList"
              :key="session.sessionId"
              :label="`${session.className} (ID: ${session.sessionId})`"
              :value="session.sessionId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery" :disabled="!queryParams.sessionId">查询</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资料列表 -->
    <el-card>
      <div slot="header" class="table-header">
        <span>资料列表</span>
        <span class="header-info" v-if="currentSession">
          当前课堂：{{ currentSession.className }} (ID: {{ currentSession.sessionId }})
        </span>
      </div>

      <el-table v-loading="loading" :data="materialList" :empty-text="emptyText">
        <el-table-column label="资料名称" align="center" prop="materialName" min-width="200">
          <template slot-scope="scope">
            <div class="material-name-cell">
              <i :class="getFileIcon(scope.row.fileType)" class="file-icon"></i>
              <span class="material-name">{{ scope.row.materialName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属课堂ID" align="center" prop="sessionId" width="120">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.sessionId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件类型" align="center" prop="fileType" width="100" />
        <el-table-column label="文件大小" align="center" prop="fileSize" width="100">
          <template slot-scope="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="推送状态" align="center" prop="pushStatus" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.pushStatus === '1' ? 'success' : 'info'">
              {{ scope.row.pushStatus === '1' ? '已推送' : '未推送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推送时间" align="center" prop="pushTime" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.pushTime ? parseTime(scope.row.pushTime, '{y}-{m}-{d} {h}:{i}') : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-download"
              @click="handleDownload(scope.row)"
            >下载</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-s-promotion"
              @click="handlePush(scope.row)"
              :disabled="scope.row.pushStatus === '1'"
            >推送</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 上传资料对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="500px" append-to-body>
      <el-form ref="uploadForm" :model="upload.form" :rules="upload.rules" label-width="100px">
        <el-form-item label="当前课堂" prop="sessionName">
          <el-input :value="currentSession ? `${currentSession.className} (ID: ${currentSession.sessionId})` : ''" disabled />
        </el-form-item>
        <el-form-item label="上传文件" prop="file" required>
          <el-upload
            ref="upload"
            :limit="1"
            accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,.jpg,.png"
            :action="upload.url"
            :headers="upload.headers"
            :data="upload.data"
            :disabled="upload.isUploading"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :on-error="handleFileError"
            :before-upload="beforeUpload"
            :auto-upload="false"
            drag
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip">
              <div>支持 pdf、doc、ppt、xls、图片、压缩包等格式文件</div>
              <div>文件大小不超过 50MB</div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm" :loading="upload.isUploading">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMaterial, delMaterial, pushMaterial, addMaterial } from "@/api/proj_lw/material";
import { listCourse } from "@/api/proj_lw/course";
import { listSession } from "@/api/proj_lw/session";
import { getToken } from "@/utils/auth";

export default {
  name: "Material",
  data() {
    return {
      loading: false,
      total: 0,
      materialList: [],
      courseList: [],
      sessionList: [],
      currentSession: null,
      emptyText: '请先选择课程和课堂，然后点击查询',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseId: null,
        sessionId: null
      },
      upload: {
        open: false,
        title: "上传资料",
        isUploading: false,
        url: process.env.VUE_APP_BASE_API + "/proj_lw/material/upload",
        headers: {
          Authorization: "Bearer " + getToken()
        },
        data: {
          sessionId: null
        },
        form: {
          sessionId: null
        },
        rules: {
          file: [
            { required: true, message: "请选择要上传的文件", trigger: "change" }
          ]
        }
      }
    };
  },
  created() {
    this.getCourseList();
  },
  methods: {
    /** 获取课程列表 */
    getCourseList() {
      listCourse({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.courseList = response.rows || [];
      });
    },
    /** 课程变化时获取课堂列表 */
    handleCourseChange(courseId) {
      this.queryParams.sessionId = null;
      this.sessionList = [];
      this.currentSession = null;
      this.materialList = [];
      this.emptyText = '请先选择课堂，然后点击查询';

      if (courseId) {
        this.getSessionList(courseId);
      }
    },
    /** 获取课堂列表 */
    getSessionList(courseId) {
      listSession({ courseId: courseId, pageNum: 1, pageSize: 1000 }).then(response => {
        this.sessionList = response.rows || [];
      });
    },
    /** 课堂变化时重置数据 */
    handleSessionChange(sessionId) {
      if (sessionId) {
        this.currentSession = this.sessionList.find(session => session.sessionId === sessionId);
        this.materialList = [];
        this.emptyText = '请点击查询按钮加载资料列表';
      } else {
        this.currentSession = null;
        this.materialList = [];
        this.emptyText = '请先选择课堂，然后点击查询';
      }
    },
    /** 查询资料列表 */
    getList() {
      if (!this.queryParams.sessionId) {
        this.$modal.msgWarning("请先选择课堂");
        return;
      }

      this.loading = true;
      this.emptyText = '加载中...';

      listMaterial({
        sessionId: this.queryParams.sessionId,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize
      }).then(response => {
        this.materialList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
        this.emptyText = '暂无资料数据';
      }).catch(() => {
        this.loading = false;
        this.emptyText = '加载失败，请重试';
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.courseId = null;
      this.queryParams.sessionId = null;
      this.sessionList = [];
      this.currentSession = null;
      this.materialList = [];
      this.emptyText = '请先选择课程和课堂，然后点击查询';
    },
    /** 上传按钮操作 */
    handleUpload() {
      if (!this.currentSession) {
        this.$modal.msgWarning("请先选择课堂");
        return;
      }

      this.upload.form.sessionId = this.currentSession.sessionId;
      this.upload.data.sessionId = this.currentSession.sessionId;
      this.upload.open = true;
      this.$nextTick(() => {
        this.$refs.upload.clearFiles();
      });
    },
    // 文件上传前校验
    beforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50;
      if (!isLt50M) {
        this.$modal.msgError("上传文件大小不能超过 50MB!");
        return false;
      }
      return true;
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;

      if (response.code === 200) {
        const materialData = {
          sessionId: this.currentSession.sessionId,
          materialName: file.name,
          fileType: this.getFileExtension(file.name),
          fileSize: file.size,
          filePath: response.url,
          pushStatus: "0"
        };

        addMaterial(materialData).then(response => {
          this.$modal.msgSuccess("上传成功");
          this.upload.open = false;
          this.getList(); // 重新加载列表
        }).catch(error => {
          this.$modal.msgError("保存资料信息失败");
        });
      } else {
        this.$modal.msgError(response.msg || "上传失败");
      }
    },
    // 文件上传失败处理
    handleFileError(error, file, fileList) {
      this.upload.isUploading = false;
      this.$modal.msgError("上传失败：" + (error.message || "未知错误"));
    },
    // 提交上传文件
    submitFileForm() {
      if (this.$refs.upload.uploadFiles.length === 0) {
        this.$modal.msgWarning("请选择要上传的文件");
        return;
      }
      this.$refs.upload.submit();
    },

    /** 下载按钮操作 */
    handleDownload(row) {
      console.log('开始下载文件:', row.materialName);

      // 方案1：直接使用后端地址（你测试成功的）
      const downloadUrl = `http://localhost:8080/proj_lw/material/download/${row.materialId}`;
      console.log('下载URL:', downloadUrl);

      // 使用a标签下载（最可靠的方式）
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.target = '_blank';
      link.download = row.materialName || 'download';
      link.style.display = 'none';

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      this.$modal.msgSuccess(`开始下载: ${row.materialName}`);
    },

    /** 推送按钮操作 */
    handlePush(row) {
      this.$modal.confirm('是否确认推送资料"' + row.materialName + '"给学生？').then(() => {
        return pushMaterial(row.materialId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("推送成功");
      }).catch(() => {});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除资料"' + row.materialName + '"?').then(() => {
        return delMaterial(row.materialId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 获取文件图标 */
    getFileIcon(fileType) {
      const iconMap = {
        'pdf': 'el-icon-document',
        'doc': 'el-icon-document', 'docx': 'el-icon-document',
        'ppt': 'el-icon-data-board', 'pptx': 'el-icon-data-board',
        'xls': 'el-icon-s-data', 'xlsx': 'el-icon-s-data',
        'txt': 'el-icon-document',
        'zip': 'el-icon-folder-opened', 'rar': 'el-icon-folder-opened',
        'jpg': 'el-icon-picture', 'png': 'el-icon-picture', 'gif': 'el-icon-picture'
      };
      return iconMap[fileType] || 'el-icon-document';
    },
    /** 获取文件扩展名 */
    getFileExtension(filename) {
      return filename.slice((filename.lastIndexOf(".") - 1 >>> 0) + 2).toLowerCase();
    },
    /** 格式化文件大小 */
    formatFileSize(bytes) {
      if (!bytes || bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  font-size: 14px;
  color: #606266;
}

.material-name-cell {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #409EFF;
}

.material-name {
  flex: 1;
}
</style>
