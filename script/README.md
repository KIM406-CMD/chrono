# 开发启动脚本

用于本地开发的一键启动脚本，Bash（`.sh`）与 Windows 批处理（`.bat`）各一套。

## 中间件模式

脚本支持两种中间件模式：

| 模式 | 参数 | MySQL | Redis | Elasticsearch |
|------|------|-------|-------|---------------|
| Docker | 默认（不加参数） | `localhost:3307` | `localhost:6379` | `localhost:9200` |
| 本地 | `--local` | `localhost:3306` | `localhost:6379` | `localhost:9200` |

> Docker 模式下 MySQL 映射到宿主机 **3307**（避免与本机 3306 冲突），
> 后端脚本会通过 `SPRING_DATASOURCE_URL` 自动覆盖 `application-dev.yml` 中的 3306。
>
> 若旧数据卷中的中文显示为乱码（历史版本初始化时客户端字符集为 latin1），
> 执行 `./script/stop-middleware.sh --volumes`（Windows：`script\stop-middleware.bat --volumes`）
> 删除数据卷后重新启动，即可按修复后的 `initial/*.sql` 重建数据。

## 快速开始

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

## 分步启动

| 步骤 | Bash | Windows |
|------|------|---------|
| 启动中间件 | `./script/start-middleware.sh` | `script\start-middleware.bat` |
| 启动后端 | `./script/start-backend.sh` | `script\start-backend.bat` |
| 启动前端 | `./script/start-frontend.sh` | `script\start-frontend.bat` |
| 停止中间件 | `./script/stop-middleware.sh` | `script\stop-middleware.bat` |

常用参数：

- `--local`：使用本机中间件（默认 Docker）
- `--skip-build`：跳过后端 `mvn clean install`，适用于已构建过、只改了少量代码的场景
- `--docker-frontend`：`start-all` 使用 Docker Nginx 容器（`http://localhost:3000`）代替 Vite 开发服务器
- `--install`：前端强制重新执行 `npm install`
- `--volumes`：停止中间件时同时删除数据卷（会清空数据库，下次启动重新执行 `initial/` 初始化脚本）

推荐（前端使用 Nginx 容器，不启动 Vite）：

```bat
script\start-all.bat --docker-frontend --skip-build
```

```bash
./script/start-all.sh --docker-frontend --skip-build
```

## 环境变量（OSS / JWT / 生产数据库）

开发环境默认把图片写入本地 `uploads/` 目录，无需 OSS。需要对象存储或生产部署时，将 `script/.env.example` 复制为仓库根目录的 `.env` 并填写：

```bash
cp script/.env.example .env
```

后端脚本启动时会自动加载根目录 `.env`。可配置：

- `ALIYUN_OSS_ACCESS_KEY_ID` / `ALIYUN_OSS_ACCESS_KEY_SECRET`：OSS 密钥（设置 `UPLOAD_MODE=oss` 后启用）；
- `JWT_SECRET`：JWT 签名密钥，至少 32 字节，生产环境必须配置；
- `DB_PASSWORD`：生产数据库密码（`application-prod.yml` 不再提供默认值）。

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | <http://localhost:5173> |
| 后端 | <http://localhost:8080> |
| Knife4j 文档 | <http://localhost:8080/doc.html> |

默认管理员账号：`admin` / `admin123`。
