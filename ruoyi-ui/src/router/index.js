import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/proj_myx',
    component: Layout,
    redirect: '/proj_myx/RandomPick',
    name: 'proj_myx',
    meta: { title: '移动课堂', icon: 'education' },
    children: [
      {
        path: 'RandomPick',
        component: () => import('@/views/proj_myx/RandomPick.vue'),
        name: 'RandomPick',
        meta: { title: '随机抽人', icon: 'education' }
      },
      {
        path: 'Attendance',
        component: () => import('@/views/proj_myx/Attendance.vue'),
        name: 'Attendance',
        meta: { title: '课堂签到', icon: 'peoples', roles: ['admin','common'] }
      },
      // Admin-only menu entries for detailed pages (they can also be accessed directly)
      {
        path: 'AttendanceCreate',
        component: () => import('@/views/proj_myx/CreateAttendance.vue'),
        name: 'AttendanceCreate',
        meta: { title: '创建签到', icon: 'edit', roles: ['admin'] }
      },
      {
        path: 'AttendanceStats',
        component: () => import('@/views/proj_myx/AttendanceStats.vue'),
        name: 'AttendanceStats',
        meta: { title: '签到统计', icon: 'chart', roles: ['admin'] }
      },
      {
        path: 'Vote',
        component: () => import('@/views/proj_myx/Vote.vue'),
        name: 'Vote',
        meta: { title: '课堂投票', icon: 'vote', roles: ['admin','common'] }
      }
    ]
  },
  // 在constantRoutes数组中添加
  {
    path: '/proj_lw',
    component: Layout,
    redirect: '/proj_lw/course',
    name: 'proj_lw',
    meta: { title: '课程资源', icon: 'education' },
    children: [
      {
        path: 'course',
        component: () => import('@/views/proj_lw/course/index.vue'),
        name: 'course',
        meta: { title: '课程管理', icon: 'list' }
      },
      // 在proj_lw的children中添加
      {
        path: 'session',
        component: () => import('@/views/proj_lw/session/index.vue'),
        name: 'Session',
        meta: { title: '课堂详情', icon: 'education' },
        hidden: true
      },
      {
        path: 'classroom',
        component: () => import('@/views/proj_lw/classroom/index.vue'),
        name: 'Classroom',
        meta: { title: '正在上课', icon: 'video' },
        hidden: true
      }
    ]
  },
  // 新增作业管理路由
  {
    path: '/proj_lwj',
    component: Layout,
    redirect: '/proj_lwj/homework_publish',
    name: 'proj_lwj',
    meta: { title: '作业管理', icon: 'education' },
    children: [
      {
        path: 'homework_publish',
        component: () => import('@/views/proj_lwj/homework_publish/index.vue'),
        name: 'HomeworkPublish',
        meta: { title: '作业发布', icon: 'list' }
      },
      {
        path: 'homework_upload',
        component: () => import('@/views/proj_lwj/homework-upload/index.vue'),
        name: 'HomeworkUpload',
        meta: { title: '作业上传', icon: 'upload' }
      },
      {
        path: 'homework_grading',
        component: () => import('@/views/proj_lwj/homework_grading/index.vue'),
        name: 'HomeworkGrading',
        meta: { title: '作业批改', icon: 'edit' }
      },
      // 新增：在作业批改下可见的“提交列表”菜单（展示某个作业的所有学生提交）
      {
        path: 'homework_grading/list/:homeworkId',
        component: () => import('@/views/proj_lwj/homework_grading/index.vue'),
        name: 'HomeworkGradingList',
        meta: { title: '提交列表', icon: 'list' }
      },
      // 支持通过路由 params 直接打开某个作业的批改详情页面（不在侧边栏显示）
      {
        path: 'homework_grading/:homeworkId',
        component: () => import('@/views/proj_lwj/homework_grading/index.vue'),
        name: 'HomeworkGradingDetail',
        meta: { title: '作业批改详情', icon: 'edit' },
        hidden: true
      }
    ]
  },
  //作业看板
  {
    path: '/proj_fz',
    component: Layout,
    redirect: '/proj_fz/homework_dashboard',
    name: 'proj_fz',
    meta: { title: '学情数据', icon: 'chart' },
    children: [
      {
        path: 'homework_dashboard',
        component: () => import('@/views/proj_fz/homeworkDashboard/index.vue'),
        name: 'HomeworkDashboard',
        meta: { title: '作业看板', icon: 'dashboard' }
      }
    ]
  },
  {
    path: '/proj_qhy',
    component: Layout,
    redirect: '/forum/article',
    name: 'proj_qhy',
    meta: { title: '学习社区', icon: 'peoples' },
    alwaysShow: true,
    children: [
      {
        path: 'article',
        component: () => import('@/views/proj_qhy/article/index.vue'),
        name: 'Article',
        meta: { title: '文章管理', icon: 'documentation' }
      },
      {
        path: 'article/detail/:id',
        component: () => import('@/views/proj_qhy/article/ArticleDetail.vue'),
        name: 'ArticleDetail',
        meta: { title: '文章详情' },
        hidden: true  // 确保这个路由在菜单中隐藏
      },
      {
        path: 'forum',
        component: () => import('@/views/proj_qhy/forum/index.vue'),
        name: 'Forum',
        meta: { title: '论坛', icon: 'message' }
      }
    ]
  },
  {
    path: '/proj_cyq',
    component: Layout,
    hidden: false,
    redirect: '/proj_cyq/index',
    name: 'ProjCyq',
    meta: { title: '个人中心', icon: 'user' },
    children: [
      {
        path: 'index',
        component: () => import('@/views/proj_cyq/personal/index'),
        name: 'PersonalHome',
        meta: { title: '个人主页', icon: 'dashboard' }
      },
      {
        path: 'todo',
        component: () => import('@/views/proj_cyq/todo/index'),
        name: 'Todo',
        meta: { title: '待办事项', icon: 'list' }
      },
      {
        path: 'message',
        component: () => import('@/views/proj_cyq/message/index'),
        name: 'Message',
        meta: { title: '消息中心', icon: 'message' }
      },
      {
        path: 'operlog',
        component: () => import('@/views/proj_cyq/operlog/index'),
        name: 'Operlog',
        meta: { title: '操作日志', icon: 'log' }
      },
      {
        path: 'loginlog',
        component: () => import('@/views/proj_cyq/loginlog/index'),
        name: 'Loginlog',
        meta: { title: '登录日志', icon: 'logininfor' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  },

]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
// push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
