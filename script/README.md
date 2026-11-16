# 开发启动脚本

本目录提供两套启动入口，请按场景选择：

| 场景 | Windows | Bash / Git Bash / macOS / Linux |
|------|---------|----------------------------------|
| **首次启动**（新机器、未安装 JDK / Maven / Node / Docker） | `script\first-start.bat` | `./script/first-start.sh` |
| **日常启动**（环境已就绪） | `script\start-all.bat` | `./script/start-all.sh` |
| 停止中间件 | `script\stop-middleware.bat` | `./script/stop-middleware.sh` |

> 两个启动脚本都会依次启动中间件、后端与前端。首次启动脚本在检查/安装依赖后，会直接调用日常启动脚本。

---

## 一、首次启动（新环境）

```bat
:: Windows（cmd / PowerShell）
script\first-start.bat
```

```bash
# Git Bash / macOS / Linux
./script/first-start.sh
```

脚本执行流程：

1. 检查 JDK 21+、Maven 3.8+、Node.js 18+、Docker（含 Compose v2）；
2. 对缺失项逐项询问，确认后调用系统包管理器安装；
3. 刷新 PATH / JAVA_HOME，重新校验版本；
4. Docker 模式下拉起 Docker Desktop / Docker 服务，等待 daemon 就绪；
5. 调用 `start-all` 启动中间件、后端与前端。

### 各系统安装方式

| 系统 | 包管理器 | 安装内容 |
|------|----------|----------|
| Windows | `winget` | `EclipseAdoptium.Temurin.21.JDK`、`Apache.Maven`、`OpenJS.NodeJS.LTS`、`Docker.DockerDesktop` |
| macOS | Homebrew | `temurin@21`、`maven`、`node`、`docker`（Docker Desktop） |
| Debian / Ubuntu | apt | `openjdk-21-jdk`、`maven`、NodeSource 20.x、`docker.io` + Compose v2 |
| Fedora / RHEL | dnf | `java-21-openjdk-devel`、`maven`、`nodejs`、`docker` + Compose 插件 |
| Arch / Manjaro | pacman | `jdk21-openjdk`、`maven`、`nodejs`、`docker` + `docker-compose` |

### 常用参数

| 参数 | 说明 |
|------|------|
| `--yes` / `-y` | 缺失依赖直接安装，不再逐项确认 |
| `--check` | 只检查环境，不安装、不启动；缺少依赖时退出码为 1 |
| `--local` | 使用本机 MySQL / Redis / Elasticsearch，不检查 Docker |
| `--skip-build` | 跳过后端 `mvn clean install`（会原样传给 `start-all`） |
| `--docker-frontend` | 使用 Docker Nginx 容器代替 Vite（会原样传给 `start-all`） |

示例：

```bat
script\first-start.bat --yes
script\first-start.bat --check
script\first-start.bat --local --skip-build
```

```bash
./script/first-start.sh --yes
./script/first-start.sh --check
./script/first-start.sh --local --skip-build
```

### 注意事项

- **Windows + WSL2**：Docker Desktop 依赖 WSL2。未安装时脚本会提示以管理员身份执行 `wsl --install`，完成后需重启再运行。
- **PATH 未刷新**：刚安装完 JDK / Maven / Node 时，脚本会尝试从注册表刷新 PATH；若仍提示找不到命令，请关闭并重新打开终端后重试。
- **Docker 权限（Linux）**：脚本会把当前用户加入 `docker` 组，需要注销并重新登录（或执行 `newgrp docker`）后才能直接使用 `docker`。
- **已有本机中间件**：不需要 Docker 时可加 `--local`，但需自行启动 MySQL 3306、Redis 6379、Elasticsearch 9200。
- **仅检查环境**：CI 或排障时使用 `--check`，不会修改系统。

---

## 二、日常启动

### Windows（双击或在 cmd 中运行）

```bat
script\start-all.bat            :: Docker 中间件 + 后端 + 前端
script\start-all.bat --local    :: 本机中间件 + 后端 + 前端
```

后端与前端会分别在新的命令行窗口中启动，关闭窗口即可停止服务。

### Bash / Git Bash / macOS / Linux

```bash
./script/start-all.sh           # Docker 中间件 + 后端 + 前端
./script/start-all.sh --local   # 本机中间件 + 后端 + 前端
```

后端在后台运行，日志写入 `logs/backend.log`；前端在前台运行，`Ctrl+C` 会同时停止前后端。

### 中间件模式

| 模式 | 参数 | MySQL | Redis | Elasticsearch |
|------|------|-------|-------|---------------|
| Docker | 默认（不加参数） | `localhost:3307` | `localhost:6379` | `localhost:9200` |
| 本地 | `--local` | `localhost:3306` | `localhost:6379` | `localhost:9200` |

> Docker 模式下 MySQL 映射到宿主机 **3307**（避免与本机 3306 冲突），
> 启动后端时会通过 `SPRING_DATASOURCE_URL` 自动覆盖 `application-dev.yml` 中的 3306。
>
> 若旧数据卷中的中文显示为乱码（历史版本初始化时客户端字符集为 latin1），
> 执行 `./script/stop-middleware.sh --volumes`（Windows：`script\stop-middleware.bat --volumes`）
> 删除数据卷后重新启动，即可按修复后的 `initial/*.sql` 重建数据。

### 常用参数（start-all）

- `--local`：使用本机中间件（默认 Docker）
- `--skip-build`：跳过后端 `mvn clean install`，适用于已构建过、只改了少量代码的场景
- `--docker-frontend`：使用 Docker Nginx 容器（`http://localhost:3000`）代替 Vite 开发服务器，启动时会重新构建前端镜像以加载最新代码
- `--volumes`：停止中间件时同时删除数据卷（会清空数据库，下次启动重新执行 `initial/` 初始化脚本）

推荐（前端使用 Nginx 容器，不启动 Vite）：

```bat
script\start-all.bat --docker-frontend --skip-build
```

```bash
./script/start-all.sh --docker-frontend --skip-build
```

---

## 三、停止中间件

| 操作 | Bash | Windows |
|------|------|---------|
| 停止中间件 | `./script/stop-middleware.sh` | `script\stop-middleware.bat` |
| 停止并删除数据卷 | `./script/stop-middleware.sh --volumes` | `script\stop-middleware.bat --volumes` |

---

## 四、环境变量（OSS / JWT / 生产数据库）

开发环境默认把图片写入本地 `uploads/` 目录，无需 OSS。需要对象存储或生产部署时，将 `script/.env.example` 复制为仓库根目录的 `.env` 并填写：

```bash
cp script/.env.example .env
```

后端启动时会自动加载根目录 `.env`。可配置：

- `ALIYUN_OSS_ACCESS_KEY_ID` / `ALIYUN_OSS_ACCESS_KEY_SECRET`：OSS 密钥（设置 `UPLOAD_MODE=oss` 后启用）；
- `JWT_SECRET`：JWT 签名密钥，至少 32 字节，生产环境必须配置；
- `DB_PASSWORD`：生产数据库密码（`application-prod.yml` 不再提供默认值）。

---

## 五、访问地址

| 服务 | 地址 |
|------|------|
| 前端（Vite） | <http://localhost:5173> |
| 前端（Docker Nginx） | <http://localhost:3000> |
| 后端 | <http://localhost:8080> |
| Knife4j 文档 | <http://localhost:8080/doc.html> |

默认管理员账号：`admin` / `admin123`。

---

## 六、常见问题

| 现象 | 处理 |
|------|------|
| `docker: command not found` | 运行首次启动脚本，或安装 Docker Desktop / Docker Engine |
| `docker compose` 不可用 | 升级 Docker Desktop，或安装 `docker-compose-plugin` |
| `Cannot connect to the Docker daemon` | 启动 Docker Desktop / `sudo systemctl start docker`，等待 Engine running |
| `permission denied` 访问 Docker（Linux） | 重新登录使 `docker` 组生效，或临时使用 `sudo` |
| 后端连错数据库（3306 / 3307） | Docker 模式必须让后端连接 3307；IDE 启动时显式设置 `SPRING_DATASOURCE_URL`，或改用 `--local` |
| 前端依赖安装失败 | 确认 Node.js ≥ 18、npm 可用，删除 `node_modules` 后重试 |
| 端口被占用 | 停止占用 5173 / 8080 / 3307 / 6379 / 9200 的进程，或修改对应配置 |
| Elasticsearch 首次启动慢 | 首次拉取镜像与初始化索引需要数十秒，等待健康检查通过即可 |
