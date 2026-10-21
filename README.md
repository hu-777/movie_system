# 电影购票管理系统

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
- H2 控制台：`http://localhost:9090/h2-console`，连接 URL 参见 `application.yml`，默认用户名 `sa`、密码为空，仅供本地开发。
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
- 开发和本地生产预览的 `VITE_BASE_URL` 默认均为 `http://localhost:9090`。正式部署前，在被忽略的 `vue/.env.production.local` 中设置实际后端地址并重新构建。Vite 环境变量会进入浏览器代码，不可用于存放密钥。
- 后端生成文件下载 URL 使用 `fileBaseUrl`；远程部署需同步调整该值以及演示数据中的本地海报地址。
- 可选 MySQL 配置通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量提供连接信息，使用 `mysql` profile 激活。未配置时使用默认 H2。MySQL 模式需独立验证建表及 SQL 兼容性。

## 演示数据与敏感信息

`data.sql` 仅用于本地演示，管理员 `admin / admin` 是公开测试账号，不是生产凭据。普通用户请通过注册页面创建。部署到可公开访问的环境前必须替换演示账号和完善访问控制。

**默认 `spring.sql.init.mode=always`，且 `schema.sql` 包含删表语句：每次启动后端都会重建业务表并重新插入演示数据。** 需要保留数据时，首次初始化完成后将 `SPRING_SQL_INIT_MODE` 设置为 `never`，并在服务停止后备份数据库。MySQL profile 也会继承初始化配置，连接已有数据库前必须关闭自动初始化。

Git 忽略本地数据库、上传文件（仅保留三个指定示例海报）、备份、日志、依赖目录、构建产物、IDE 配置、私有环境配置及证书密钥。真实用户资料、数据库密码、令牌不得写入源码或演示 SQL。

## 常见问题

- PowerShell 阻止 npm 脚本：使用文中的 `npm.cmd`。
- 海报不显示：检查后端端口、`files/` 下对应文件、数据库中的下载地址和启动目录。
- 后端启动失败：检查 Java/Maven 环境、9090 端口，以及日志中的第一个 `Caused by`。
- 生产预览接口失败：检查构建时 `VITE_BASE_URL`，修改环境文件后需重新构建。
- 示例场次采用固定日期；日期过期后，需在管理页面或演示 SQL 中调整场次时间以体验购票。
