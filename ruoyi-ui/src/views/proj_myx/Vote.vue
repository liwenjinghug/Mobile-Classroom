<template>
  <div class="vote-page">
    <div class="vote-container">
      <h2 class="vote-title">投票管理</h2>
      <div class="vote-info">当前 sessionId: {{ sessionId }}</div>
      
      <div class="vote-controls">
        <el-input v-model.number="sessionId" placeholder="请输入 Session ID" style="width: 220px;" @keyup.enter.native="loadVotes" />
        <el-button type="primary" @click="loadVotes">加载投票</el-button>
        <el-button v-if="isAdmin" type="success" @click="handleCreate">新建投票</el-button>
        <el-button v-if="isAdmin" icon="el-icon-download" @click="handleExport">导出列表</el-button>
        <el-button v-if="isAdmin" icon="el-icon-printer" @click="handlePrint">打印</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column label="ID" prop="voteId" align="center" width="80" />
        <el-table-column label="标题" prop="title" min-width="200" />
        <el-table-column label="类型" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag>{{ row.type === '1' ? '单选' : '多选' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{row}">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="260" align="center">
          <template slot-scope="{row}">
            <div v-if="row.startTime" style="font-size:12px">起: {{ parseTime(row.startTime) }}</div>
            <div v-if="row.endTime" style="font-size:12px">止: {{ parseTime(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="300" class-name="ops-col">
          <template slot-scope="{row}">
            <div style="display: flex; align-items: center; justify-content: center;">
               <el-button size="mini" type="primary" @click="handleStats(row)">统计</el-button>
               
               <el-button v-if="isAdmin && row.status === '0'" size="mini" type="success" @click="handleStart(row)">开始</el-button>
               <el-button v-if="isAdmin && row.status === '1'" size="mini" type="warning" @click="handleClose(row)">结束</el-button>
               
               <el-button v-if="isAdmin" size="mini" type="text" icon="el-icon-delete" style="color: #C0C4CC; margin-left: 10px;" @click="handleDelete(row)"></el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Create Dialog -->
      <el-dialog title="新建投票" :visible.sync="dialogCreateVisible" width="600px">
        <create-vote :session-id="sessionId" @created="onCreated" v-if="dialogCreateVisible" />
      </el-dialog>

      <!-- Stats Dialog -->
      <el-dialog title="投票统计" :visible.sync="dialogStatsVisible" width="600px">
        <vote-stats :vote-id="currentVoteId" v-if="dialogStatsVisible" />
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { listVotes, startVote, closeVote, deleteVote } from '@/api/proj_myx/vote'
import CreateVote from './CreateVote'
import VoteStats from './VoteStats'
import { parseTime } from '@/utils/ruoyi'

export default {
  name: 'Vote',
  components: { CreateVote, VoteStats },
  data() {
    return {
      sessionId: null,
      list: [],
      loading: false,
      dialogCreateVisible: false,
      dialogStatsVisible: false,
      currentVoteId: null
    }
  },
  computed: {
    isAdmin() {
      const roles = this.$store.getters.roles
      return roles && roles.includes('admin')
    }
  },
  methods: {
    parseTime,
    async loadVotes() {
      if (!this.sessionId) return this.$message.warning('请输入 Session ID');
      this.loading = true;
      try {
        const res = await listVotes(this.sessionId);
        let list = res.data || [];
        
        // Sorting logic: In Progress (1) > Not Started (0) > Ended (2)
        // Secondary sort: End Time (Descending)
        list.sort((a, b) => {
          const statusOrder = { '1': 0, '0': 1, '2': 2 };
          const sa = statusOrder[a.status] !== undefined ? statusOrder[a.status] : 99;
          const sb = statusOrder[b.status] !== undefined ? statusOrder[b.status] : 99;
          
          if (sa !== sb) {
            return sa - sb;
          }
          
          // Same status, sort by endTime descending
          const ta = a.endTime ? new Date(a.endTime).getTime() : 0;
          const tb = b.endTime ? new Date(b.endTime).getTime() : 0;
          return tb - ta;
        });
        
        this.list = list;
      } catch (e) {
        this.$message.error('加载失败');
      } finally {
        this.loading = false;
      }
    },
    handleCreate() {
      if (!this.sessionId) return this.$message.warning('请输入 Session ID');
      this.dialogCreateVisible = true;
    },
    onCreated() {
      this.dialogCreateVisible = false;
      this.loadVotes();
    },
    handleExport() {
      const tHeader = ['ID', '标题', '类型', '状态', '开始时间', '结束时间'];
      const filterVal = ['voteId', 'title', 'type', 'status', 'startTime', 'endTime'];
      
      const list = this.list.map(item => ({
        voteId: item.voteId,
        title: item.title,
        type: item.type === '1' ? '单选' : '多选',
        status: this.statusText(item.status),
        startTime: this.parseTime(item.startTime),
        endTime: this.parseTime(item.endTime)
      }));
      
      const data = list.map(v => filterVal.map(j => v[j]));
      data.unshift(tHeader);
      
      const csvContent = data.map(row => 
        row.map(item => {
          let str = String(item === null || item === undefined ? '' : item);
          if (str.includes(',') || str.includes('"') || str.includes('\n')) {
            str = '"' + str.replace(/"/g, '""') + '"';
          }
          return str;
        }).join(',')
      ).join('\n');
      
      const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement("a");
      const url = URL.createObjectURL(blob);
      link.setAttribute("href", url);
      link.setAttribute("download", '投票列表_' + new Date().getTime() + '.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    handlePrint() {
      window.print();
    },
    handleStats(row) {
      this.currentVoteId = row.voteId;
      this.dialogStatsVisible = true;
    },
    async handleStart(row) {
      try {
        await startVote(row.voteId);
        this.$message.success('已开始');
        row.status = '1'; // Update local state immediately
        setTimeout(() => this.loadVotes(), 200); // Reload with slight delay to ensure DB consistency
      } catch (e) {}
    },
    async handleClose(row) {
      try {
        await closeVote(row.voteId);
        this.$message.success('已结束');
        row.status = '2'; // Update local state immediately
        setTimeout(() => this.loadVotes(), 200); // Reload with slight delay to ensure DB consistency
      } catch (e) {}
    },
    handleDelete(row) {
      this.$confirm('确认删除该投票吗？', '提示', { type: 'warning' }).then(async () => {
        await deleteVote(row.voteId);
        this.$message.success('删除成功');
        this.loadVotes();
      });
    },
    statusType(status) {
      const map = { '0': 'info', '1': 'success', '2': 'danger' };
      return map[status] || '';
    },
    statusText(status) {
      const map = { '0': '未开始', '1': '进行中', '2': '已结束' };
      return map[status] || '未知';
    }
  }
}
</script>

<style scoped>
.vote-page {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 40px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: #1d1d1f;
}

.vote-container {
  max-width: 1200px;
  margin: 0 auto;
}

.vote-title {
  font-size: 40px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 10px;
  color: #1d1d1f;
}

.vote-info {
  text-align: center;
  color: #86868b;
  margin-bottom: 40px;
  font-size: 14px;
}

.vote-controls {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  padding: 20px;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

/* Buttons */
.vote-page >>> .el-button {
  border-radius: 980px;
  font-weight: 500;
  border: none;
  padding: 10px 20px;
  transition: all 0.2s ease;
}

.vote-page >>> .el-button--primary {
  background-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}
.vote-page >>> .el-button--primary:hover {
  background-color: #0077ed;
  transform: translateY(-1px);
}

.vote-page >>> .el-button--success {
  background-color: #34c759;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.2);
}
.vote-page >>> .el-button--success:hover {
  background-color: #32d74b;
  transform: translateY(-1px);
}

.vote-page >>> .el-button--warning {
  background-color: #ff9500;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.2);
}

.vote-page >>> .el-button--danger {
  background-color: #ff3b30;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.2);
}

/* Table Styling */
.vote-page >>> .el-table {
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  background-color: #ffffff;
}

.vote-page >>> .el-table th {
  background-color: #ffffff;
  color: #86868b;
  font-weight: 600;
  border-bottom: 1px solid #f5f5f7;
  padding: 16px 0;
}

.vote-page >>> .el-table td {
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
}

.vote-page >>> .el-table--border, 
.vote-page >>> .el-table--group {
  border: none;
}

.vote-page >>> .el-table::before {
  display: none;
}

/* Tags */
.vote-page >>> .el-tag {
  border-radius: 6px;
  border: none;
  font-weight: 500;
  padding: 0 10px;
  height: 28px;
  line-height: 28px;
}

.vote-page >>> .el-tag--success {
  background-color: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.vote-page >>> .el-tag--warning {
  background-color: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.vote-page >>> .el-tag--info {
  background-color: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.vote-page >>> .el-tag--danger {
  background-color: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

/* Dialog Styling */
.vote-page >>> .el-dialog {
  border-radius: 18px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}

.vote-page >>> .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.vote-page >>> .el-dialog__title {
  font-weight: 600;
  font-size: 18px;
  color: #1d1d1f;
}

.vote-page >>> .el-dialog__body {
  padding: 24px;
}

.vote-page >>> .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f5f5f7;
}

/* Input Styling */
.vote-page >>> .el-input__inner {
  border-radius: 10px;
  border: 1px solid #d2d2d7;
  height: 40px;
  transition: all 0.2s ease;
}

.vote-page >>> .el-input__inner:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

/* Operation Buttons in Table */
.ops-col >>> .el-button {
  margin: 0 4px;
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
}

@media print {
  .vote-controls, .v-modal, .sidebar-container, .navbar, .tags-view-container {
    display: none !important;
  }
  .vote-page {
    padding: 0;
    background: white;
    min-height: auto;
  }
  .vote-container {
    max-width: 100%;
    margin: 0;
  }
  .vote-title {
    margin-top: 0;
    font-size: 24px;
  }
  .vote-info {
    margin-bottom: 20px;
  }
  .el-table {
    width: 100% !important;
    border: 1px solid #ebeef5;
  }
  .el-table th {
    background-color: #f5f7fa !important;
    color: #000 !important;
  }
  /* Hide operations column in print */
  .ops-col {
    display: none !important;
  }
  /* Hide table header for operations column if possible, or just accept it's there */
  .el-table__header colgroup col:last-child, .el-table__body colgroup col:last-child {
     display: none;
  }
  .el-table th:last-child, .el-table td:last-child {
    display: none;
  }
  
  /* Ensure dialogs are printable if open */
  .el-dialog__wrapper {
    position: absolute !important;
    top: 0 !important;
    left: 0 !important;
    margin: 0 !important;
    width: 100% !important;
    height: auto !important;
  }
  .el-dialog {
    box-shadow: none !important;
    margin-top: 0 !important;
    width: 100% !important;
    border: none !important;
  }
  /* If dialog is open, hide the main page content to avoid clutter */
  /* This is hard to do with just CSS without a parent class. 
     But usually the dialog covers the page. 
     We can try to hide .vote-container if .el-dialog__wrapper is visible? No, CSS can't do that.
     We'll rely on the dialog covering the content or the user printing what they see.
  */
}
</style>

