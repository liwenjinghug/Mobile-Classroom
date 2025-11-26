<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <div class="app-title" v-if="!topNav">Mobile Classroom</div>

    <top-nav v-if="topNav" id="topmenu-container" class="topmenu-container" />

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <search id="header-search" class="right-menu-item" />

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <div class="right-menu-item hover-effect message-item-container" @click="goToMessageCenter">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="message-badge">
            <i class="el-icon-bell" style="font-size: 20px;"></i>
          </el-badge>
        </div>
      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>Profile</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setLayout" v-if="setting">
            <span>Settings</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>Log Out</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'
import { getUnreadCount } from "@/api/proj_cyq/message";

export default {
  emits: ['setLayout'],
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    RuoYiGit,
    RuoYiDoc
  },
  data() {
    return {
      unreadCount: 0,
      timer: null
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device',
      'nickName'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    }
  },
  // 监听路由变化，跳转时自动刷新
  watch: {
    $route(to, from) {
      this.fetchUnreadCount();
    }
  },
  mounted() {
    this.fetchUnreadCount();
    // 每10秒刷新一次
    this.timer = setInterval(this.fetchUnreadCount, 10000);
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    fetchUnreadCount() {
      getUnreadCount().then(response => {
        this.unreadCount = response.data || 0;
      }).catch(error => {});
    },
    goToMessageCenter() {
      this.$router.push('/proj_cyq/message');
      setTimeout(() => { this.fetchUnreadCount() }, 500);
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    setLayout(event) {
      this.$emit('setLayout')
    },
    logout() {
      this.$confirm('Are you sure you want to log out?', 'Confirm', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index';
        })
      }).catch(() => {});
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .app-title {
    float: left;
    line-height: 50px;
    font-size: 16px;
    font-weight: 600;
    color: #343541;
    margin-left: 10px;
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    /* 控制铃铛图标容器 */
    .message-item-container {
      display: flex !important;
      align-items: center;
      justify-content: center;
      height: 50px;
    }

    /* 【核心修复】调整红点位置到右上角 */
    .message-badge {
      line-height: 1;

      ::v-deep .el-badge__content {
        /* top: -2px 往上提，贴合图标顶部 */
        top: -2px !important;
        /* right: 0px 往右移，悬挂在右上角 */
        right: 0px !important;

        height: 16px;
        line-height: 16px;
        padding: 0 4px;
        border: none;
        font-size: 10px;
      }
    }

    .avatar-container {
      margin-right: 30px;

      display: flex !important;
      align-items: center;

      .avatar-wrapper {
        position: relative;
        margin-top: 0;

        .user-avatar {
          cursor: pointer;
          width: 32px;
          height: 32px;
          border-radius: 4px;
          vertical-align: middle;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 50%;
          transform: translateY(-50%);
          font-size: 12px;
        }
      }
    }
  }
}
</style>
