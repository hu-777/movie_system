# 湖叶影院

基于 Vue 3 和 Spring Boot 的电影购票与影院管理项目，包含用户注册登录、电影浏览、场次查询、选座购票、订单管理及后台统计。

仓库：https://github.com/hu-777/movie_system

## 功能与结构

- 用户：注册、登录、密保找回密码、电影浏览、选座购票、订单、个人资料与头像。
- 管理员：电影及分类维护、场次与座位管理、用户与管理员管理、公告、订单和统计图表。
- 公共页面布局为 `vue/src/views/Manager.vue`，通过角色显示菜单；页面路由见 `vue/src/router/index.js`。
- 发票相关数据结构仍保留，独立发票页面路由目前未启用。

```text
movie_system/
├── files/                       # 示例海报及本地上传文件
├── springboot/
│   ├── src/main/java/com/example/
│   │   ├── controller/          # HTTP 接口
│   │   ├── service/             # 业务逻辑
│   │   ├── mapper/              # 数据访问接口
│   │   ├── entity/              # 数据实体
│   │   └── common/              # 公共配置
│   ├── src/main/resources/
│   │   ├── application.yml      # 默认 H2 配置
│   │   ├── application-mysql.yml
│   │   ├── schema.sql           # 开发数据库建表脚本
│   │   ├── data.sql             # 公开演示数据
│   │   └── mapper/              # MyBatis SQL 映射
│   ├── data/                    # 本地数据库，不提交
│   └── pom.xml
├── vue/
│   ├── src/views/               # 登录页及业务页面
│   ├── src/router/              # Vue Router 路由
│   ├── src/utils/request.js     # Axios 封装
│   ├── .env.development         # 开发 API 地址（非密钥）
│   ├── .env.production          # 本地生产预览 API 地址
│   └── package.json
├── package.json                 # 现有前端共享依赖
└── .gitignore
```

## 技术栈与环境

前端使用 Vue 3、Vite 4、Element Plus、Vue Router、Axios、ECharts、Sass；后端使用 Spring Boot 2.7.15、Spring Security、MyBatis / MyBatis-Plus、PageHelper、Hutool。

建议 JDK 11、Maven 3.8+、Node.js 18+、npm 9+。Java 编译目标为 8。默认数据库为 H2 文件数据库，采用 MySQL 兼容模式，无需额外安装 MySQL。

## 本地启动

当前工作区位于 `C:\Users\hfq18\Desktop\code\movie_system`。以下命令适用于 Windows PowerShell；其他开发者替换项目根目录即可。业务代码保留相对路径，避免绑定个人电脑目录。

首次安装依赖：

```powershell
Set-Location 'C:\Users\hfq18\Desktop\code\movie_system'
npm.cmd ci
Set-Location vue
npm.cmd ci
```

根目录依赖包含 `dayjs`、`html2canvas` 等页面使用的库，因此两个目录均需安装。

启动后端（一个终端）：

```powershell
Set-Location 'C:\Users\hfq18\Desktop\code\movie_system\springboot'
mvn.cmd spring-boot:run
```

启动前端（另一个终端）：

```powershell
Set-Location 'C:\Users\hfq18\Desktop\code\movie_system\vue'
npm.cmd run dev -- --host 127.0.0.1
```

- 前端：`http://127.0.0.1:5173/`，如端口占用以终端输出为准。
- 后端：`http://localhost:9090`；`GET /` 为健康检查接口。
- H2 控制台默认关闭。需要本机调试时临时设置 `H2_CONSOLE_ENABLED=true`，仅允许回环地址访问；不要在公开部署中开启。
- 停止服务：在各自终端按 `Ctrl+C`。

## 构建

后端打包与运行：

```powershell
Set-Location 'C:\Users\hfq18\Desktop\code\movie_system\springboot'
mvn.cmd clean package -DskipTests
java -jar target\springboot-0.0.1-SNAPSHOT.jar
```

前端构建与本地预览：

```powershell
Set-Location 'C:\Users\hfq18\Desktop\code\movie_system\vue'
npm.cmd run build
npm.cmd run preview -- --host 127.0.0.1
```

前端产物为 `vue/dist/`，后端产物为 `springboot/target/springboot-0.0.1-SNAPSHOT.jar`。跳过测试的打包不代表业务流程已经验证。前端使用 history 路由，正式托管时需为页面路由配置回退到 `index.html`。

## 配置与路径

- 请从 `springboot` 目录启动后端：数据库相对路径 `./data/movie_system` 对应 `springboot/data/`，上传目录 `../files/` 对应项目根目录 `files/`。
- 可通过 `FILE_STORAGE_PATH` 环境变量覆盖文件目录，例如 `C:\Users\hfq18\Desktop\code\movie_system\files\`。
- 前端请求统一使用同源 `/api`；Vite 将其代理到 `127.0.0.1:9090` 并移除 `/api` 前缀。正式部署也需配置同样的反向代理，不能仅托管静态页面。Vite 环境变量不可存放密钥。
- 新上传图片返回 `/api/files/download/...`。旧演示数据中的 `localhost:9090` 海报地址部署时需改为同源地址。
- 可选 MySQL 配置通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量提供连接信息，使用 `mysql` profile 激活。未配置时使用默认 H2。MySQL 模式需独立验证建表及 SQL 兼容性。

## 演示数据与敏感信息

`data.sql` 仅用于本地演示，管理员 `admin / admin` 是公开测试账号，不是生产凭据。普通用户请通过注册页面创建。部署到可公开访问的环境前必须修改演示账号密码。

**默认禁止初始化（`DB_INIT_MODE=never`），保留已有数据。** 全新数据库第一次启动可使用 `java -jar target\springboot-0.0.1-SNAPSHOT.jar --spring.sql.init.mode=always`；初始化完成后停止服务，后续正常启动不再传该参数。`schema.sql` 包含删表语句，已有数据时绝不能使用 `always`。连接已有 MySQL 数据库时同样保持初始化关闭。

## 登录与权限

- 服务端 HttpSession 保存登录身份，浏览器仅保存 HttpOnly、SameSite=Lax 的会话 Cookie。连续 **24 小时无请求**后会话失效；正常访问会续期，后台不设置保活轮询。后端重启后需重新登录。
- 用户与管理员身份由服务端核验；订单归属取自会话，不接受前端传入的用户 ID 或角色。管理接口仅管理员可用，用户只能操作自己的订单，退票审批由管理员执行。
- 所有写请求校验 CSRF 凭据；退出登录、修改密码或删除账户后旧会话不可继续使用。前端刷新或跳转会从 `/auth/me` 恢复身份，不再将登录资料作为授权依据存入 localStorage。
- 密保找回保留。答案使用 BCrypt 保存，启动时自动迁移旧明文答案；验证有尝试次数限制，成功后签发绑定账户和当前会话、5 分钟有效、只能使用一次的重置凭据。迁移前应在服务停止后备份数据库。
- 新密码至少 8 位，包含大小写字母、数字、特殊字符，UTF-8 长度最多 72 字节。接口响应不包含密码或密保答案。注册后可在个人资料中上传头像，未登录不允许上传文件。
- HTTPS 部署设置 `SESSION_COOKIE_SECURE=true`。CORS 默认仅允许本地 5173 两个来源，可通过 `APP_ALLOWED_ORIGINS` 设置逗号分隔的明确来源，禁止通配。会话及尝试次数计数保存在单进程内存中，多实例部署需另行接入共享会话和限流存储。

生产代理示例（置于同一个 HTTPS server 中，静态根目录指向 `vue/dist`）：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:9090/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
}
location / { try_files $uri $uri/ /index.html; }
```

## 回归验证

在 `springboot` 目录执行 `./verify-auth.ps1` 和 `./verify-orders.ps1`。两者均使用独立 H2 内存数据库，不连接现有文件数据库；首次先执行 `mvn.cmd package -DskipTests`。

认证回归覆盖登录、会话轮换、CSRF、跨用户越权、资料字段白名单、密码变更与删除后的会话失效、密保迁移及一次性凭据、上传校验；订单回归覆盖并发抢座、失败回滚、取消与退款。它们是独立可执行测试，`mvn package` 不会自动执行其断言。

Git 忽略本地数据库、上传文件（仅保留三个指定示例海报）、备份、日志、依赖目录、构建产物、IDE 配置、私有环境配置及证书密钥。真实用户资料、数据库密码、令牌不得写入源码或演示 SQL。

## 常见问题

- PowerShell 阻止 npm 脚本：使用文中的 `npm.cmd`。
- 海报不显示：检查后端端口、`files/` 下对应文件、数据库中的下载地址和启动目录。
- 后端启动失败：检查 Java/Maven 环境、9090 端口，以及日志中的第一个 `Caused by`。
- 生产接口失败：检查 `/api` 同源代理、后端运行状态和 Cookie 的 HTTPS 配置。
- 示例场次采用固定日期；日期过期后，需在管理页面或演示 SQL 中调整场次时间以体验购票。
