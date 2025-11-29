移动课堂小程序

如何运行：
1. 打开微信开发者工具，选择“导入项目”，选择本仓库的 `wx` 目录（需要填写 AppID，可使用测试 AppID）。
2. 运行后会默认跳转到登录页。当前示例的后端 base URL 为 `http://localhost:8080`，你可以在 `app.js` 或 `utils/api.js` 中调整。
3. 后端接口示例（需按实际后端实现）：
   - POST /api/auth/login  { studentNo, password } -> { token, user }
   - GET /api/courses/my -> { listening: [], teaching: [] }
   - GET /api/courses/{id} -> course object

开发调试提示（解决“域名不在合法域名列表”错误）：
- 推荐（安全）：在后端使用 HTTPS，并在微信公众平台小程序配置中添加你的域名（上线前必须）。
- 快速调试方法（仅在本地开发用）：
  1) 在微信开发者工具中，打开“详情 > 本地设置”，将「不校验合法域名、web-view（业务域名）及 TLS 版本」或项目的 `urlCheck` 选项关闭（或在 `project.private.config.json` 中将 `urlCheck` 设为 false，已为你修改）。
  2) 另一个常用方法是使用 ngrok/localhost.run 将本地服务暴露为 HTTPS 公网地址，然后把该 HTTPS 地址加入小程序后台合法域名。
  3) 或者在开发者工具内手动设置 localStorage（Storage）加入一个假的 `token` 值以跳过登录验证（注意：这只是前端绕过，后端接口仍会被拒绝）。

测试账号（默认可尝试）：
- 管理员：admin / admin123
- 测试用户：ry / admin123
- 测试学生：student1 / admin123

下一步建议：
- 根据后端接口调整 `utils/api.js` 的路径与响应解析。
- 实现加入课堂/创建课堂页面与表单。
- 实现作业/考试/签到等子页面及实时刷新逻辑（WebSocket 或定期轮询）。

如需我直接在 `wx` 目录里继续生成加入课堂 / 创建课堂 / 作业列表 / 作业提交页的完整实现（含文件上传、附件下载和鉴权提示），请回复确认，我会继续生成更完整的页面代码并集成当前后端接口契约。
