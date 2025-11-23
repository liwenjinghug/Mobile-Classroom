<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
      <el-form-item label="辩题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入辩题" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-plus" type="primary" @click="handleAdd" v-hasPermi="['proj_qhy:debate:add']">新建辩论</el-button>
      </el-form-item>

    </el-form>

    <div class="debate-list">
      <div v-for="item in debateList" :key="item.id" class="debate-item" @click="handleEnter(item)">
        <div class="debate-header">
          <div class="debate-title">{{ item.title }}</div>
          <el-tag :type="statusType(item.status)">{{ statusLabel(item.status) }}</el-tag>
        </div>
        <div class="debate-body">
          <div class="viewpoint pro">正方：{{ item.proViewpoint }}</div>
          <div class="vs">VS</div>
          <div class="viewpoint con">反方：{{ item.conViewpoint }}</div>
        </div>
        <div class="debate-footer">
          <span>邀请码: <strong style="color: #0071e3">{{ item.inviteCode }}</strong></span>
          <span class="create-time">{{ parseTime(item.createTime) }}</span>
        </div>
      </div>
    </div>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="辩题" prop="title">
          <el-input v-model="form.title" placeholder="请输入辩题" />
        </el-form-item>
        <el-form-item label="正方观点" prop="proViewpoint">
          <el-input v-model="form.proViewpoint" placeholder="请输入正方观点" />
        </el-form-item>
        <el-form-item label="反方观点" prop="conViewpoint">
          <el-input v-model="form.conViewpoint" placeholder="请输入反方观点" />
        </el-form-item>
        <el-form-item label="单次发言" prop="speechLimit">
          <el-input-number v-model="form.speechLimit" :min="10" :step="10" label="秒"></el-input-number>
          <span style="margin-left: 10px">秒 (每方5次，共10回合)</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="加入辩论" :visible.sync="joinOpen" width="400px" append-to-body>
      <el-form :model="joinForm" label-width="80px">
        <el-form-item label="邀请码">
          <el-input v-model="joinForm.inviteCode" placeholder="请输入邀请码" />
        </el-form-item>
        <el-form-item label="选择身份">
          <el-radio-group v-model="joinForm.role">
            <el-radio label="1">正方辩手</el-radio>
            <el-radio label="2">反方辩手</el-radio>
            <el-radio label="3">观众</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitJoin">进 入</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDebate, addDebate, joinDebate } from "@/api/proj_qhy/debate";

export default {
  name: "DebateList",
  data() {
    return {
      showSearch: true,
      debateList: [],
      open: false,
      joinOpen: false,
      title: "",
      queryParams: { pageNum: 1, pageSize: 10, title: null },
      form: {},
      joinForm: { debateId: null, inviteCode: '', role: '3' },
      rules: {
        title: [{ required: true, message: "不能为空", trigger: "blur" }],
        proViewpoint: [{ required: true, message: "不能为空", trigger: "blur" }],
        conViewpoint: [{ required: true, message: "不能为空", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      listDebate(this.queryParams).then(res => {
        this.debateList = res.rows;
      });
    },
    statusType(status) {
      if (status === '0') return 'info';
      if (status === '1') return 'success';
      return 'danger';
    },
    statusLabel(status) {
      if (status === '0') return '未开始';
      if (status === '1') return '进行中';
      return '已结束';
    },
    handleAdd() {
      this.resetForm("form");
      this.open = true;
      this.title = "新建辩论";
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addDebate(this.form).then(res => {
            this.$modal.msgSuccess("创建成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    cancel() { this.open = false; },
    handleEnter(item) {
      // 如果已加入或你是创建者，直接进入房间
      if (item.joined) {
        this.$router.push(`/proj_qhy/debate/room/${item.id}`);
      } else {
        // 否则弹出输入邀请码
        this.joinForm.debateId = item.id;
        this.joinForm.inviteCode = '';
        this.joinForm.role = '3';
        this.joinOpen = true;
      }
    },
    submitJoin() {
      if (!this.joinForm.inviteCode) {
        this.$modal.msgError("请输入邀请码");
        return;
      }
      joinDebate(this.joinForm).then(res => {
        this.joinOpen = false;
        this.$router.push(`/proj_qhy/debate/room/${this.joinForm.debateId}`);
      });
    },
    reset() {
      this.form = {
        id: null,
        title: null,
        proViewpoint: null,
        conViewpoint: null,
        speechLimit: 60 // 默认60秒
      };
      this.resetForm("form");
    },
    handleQuery() { this.getList(); }
  }
};
</script>

<style scoped>
/* 复用之前的Mac Style */
.app-container { background-color: #f5f5f7; min-height: 100vh; padding: 20px; }
.debate-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px; }
.debate-item {
  background: white; border-radius: 18px; padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05); cursor: pointer; transition: all 0.3s;
}
.debate-item:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.1); }
.debate-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.debate-title { font-size: 18px; font-weight: 700; color: #1d1d1f; }
.debate-body { display: flex; justify-content: space-between; align-items: center; background: #f5f5f7; padding: 12px; border-radius: 10px; margin-bottom: 16px; }
.viewpoint { font-size: 14px; color: #1d1d1f; font-weight: 500; width: 40%; text-align: center; }
.pro { color: #0071e3; }
.con { color: #ff3b30; }
.vs { font-weight: 900; color: #86868b; font-style: italic; }
.debate-footer { display: flex; justify-content: space-between; font-size: 13px; color: #86868b; }
</style>
