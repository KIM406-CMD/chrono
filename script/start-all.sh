#!/usr/bin/env bash
# ============================================================
# 一键启动：中间件 -> 后端 -> 前端
#
# 默认使用 Docker 中间件；--local 使用本机中间件。
# 后端在后台运行，日志写入 logs/backend.log；
# 默认启动 Vite 开发服务器，--docker-frontend 改为启动 Docker Nginx 容器。
#
# 用法：./script/start-all.sh [--local] [--skip-build] [--docker-frontend]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SERVER_DIR="$ROOT_DIR/chrono-bbs-server"
CLIENT_DIR="$ROOT_DIR/chrono-bbs-client"
DOCKER_DIR="$ROOT_DIR/docker"

MODE="docker"
SKIP_BUILD=0
DOCKER_FRONTEND=0

usage() {
  cat <<'EOF'
用法: start-all.sh [--local] [--skip-build] [--docker-frontend]

  --local            使用本机中间件（默认使用 Docker 中间件）
  --skip-build       跳过后端 Maven 构建
  --docker-frontend  启动 Docker Nginx 容器，不启动 Vite 开发服务器
EOF
}

for arg in "$@"; do
  case "$arg" in
    --local) MODE="local" ;;
    --skip-build) SKIP_BUILD=1 ;;
    --docker-frontend) DOCKER_FRONTEND=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

# ------------------------------------------------------------
# 1. 中间件
# ------------------------------------------------------------
if [ "$MODE" = "local" ]; then
  echo "==> 使用本机中间件（不启动 Docker），检查端口..."
  missing=0
  check_port() {
    local host="$1" port="$2" name="$3"
    if (exec 3<>"/dev/tcp/$host/$port") 2>/dev/null; then
      exec 3>&-
      echo "  [OK]   $name  $host:$port"
    else
      echo "  [FAIL] $name  $host:$port"
      missing=1
    fi
  }
  check_port localhost 3306 "MySQL"
  check_port localhost 6379 "Redis"
  check_port localhost 9200 "Elasticsearch"
  if [ "$missing" -ne 0 ]; then
    echo "警告：部分端口不可用，请先启动本机中间件，或改用 Docker 模式。" >&2
    exit 1
  fi
  echo "==> 本机中间件检查通过"
else
  if ! command -v docker >/dev/null 2>&1; then
    echo "错误：未找到 docker，请安装 Docker Desktop，或使用 --local 模式。" >&2
    exit 1
  fi
  if ! docker compose version >/dev/null 2>&1; then
    echo "错误：docker compose 不可用，请升级 Docker Desktop。" >&2
    exit 1
  fi

  echo "==> 启动 Docker 中间件（MySQL 3307 / Redis 6379 / ES 9200）..."
  cd "$DOCKER_DIR"
  if ! docker compose up -d --wait; then
    echo "警告：健康检查未通过或当前 Compose 不支持 --wait，改用后台启动。"
    docker compose up -d
  fi
  docker compose ps
  echo "==> 中间件已启动。停止：./script/stop-middleware.sh"
fi

# ------------------------------------------------------------
# 2. 后端（后台）
# ------------------------------------------------------------
mkdir -p "$ROOT_DIR/logs"
BACKEND_LOG="$ROOT_DIR/logs/backend.log"
echo "==> 启动后端（日志：$BACKEND_LOG）"

run_backend() {
  # 可选：从仓库根目录 .env 加载 OSS 等环境变量（.env 已被 .gitignore 忽略）
  if [ -f "$ROOT_DIR/.env" ]; then
    echo "==> 加载环境变量: $ROOT_DIR/.env"
    set -a
    # shellcheck disable=SC1090
    . "$ROOT_DIR/.env"
    set +a
  fi

  # 未配置 OSS 密钥时以占位值启动，避免配置绑定失败；默认使用本地 uploads/ 存储
  if [ -z "${ALIYUN_OSS_ACCESS_KEY_ID:-}" ] || [ -z "${ALIYUN_OSS_ACCESS_KEY_SECRET:-}" ]; then
    echo "提示：未配置 OSS 密钥，图片将保存到本地 uploads/ 目录（upload.mode=local）。"
    export ALIYUN_OSS_ACCESS_KEY_ID="${ALIYUN_OSS_ACCESS_KEY_ID:-none}"
    export ALIYUN_OSS_ACCESS_KEY_SECRET="${ALIYUN_OSS_ACCESS_KEY_SECRET:-none}"
  fi

  if ! command -v mvn >/dev/null 2>&1; then
    echo "错误：未找到 mvn，请安装 Maven 3.8+ 并配置 PATH。" >&2
    exit 1
  fi
  if ! command -v java >/dev/null 2>&1; then
    echo "错误：未找到 java，请安装 JDK 21+ 并配置 JAVA_HOME。" >&2
    exit 1
  fi

  if [ "$MODE" = "docker" ]; then
    # Docker MySQL 映射在宿主机 3307，覆盖 application-dev.yml 中的 3306
    export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/chrono_bbs_dev?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
    echo "==> 中间件模式: Docker（MySQL localhost:3307）"
  else
    echo "==> 中间件模式: 本地（MySQL 按 application-dev.yml）"
  fi

  if [ "$SKIP_BUILD" -eq 1 ]; then
    echo "==> 跳过 Maven 构建"
  else
    echo "==> 构建后端（mvn clean install -DskipTests）..."
    (cd "$SERVER_DIR" && mvn clean install -DskipTests)
  fi

  echo "==> 启动后端 http://localhost:8080（Knife4j: /doc.html）"
  cd "$SERVER_DIR"
  exec mvn -pl module-start spring-boot:run
}

run_backend >"$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!

stop_backend() {
  if [ -n "${BACKEND_PID:-}" ]; then
    echo
    echo "==> 停止后端（PID $BACKEND_PID）..."
    kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
  fi
}
trap stop_backend EXIT INT TERM

# ------------------------------------------------------------
# 3. 前端
# ------------------------------------------------------------
if [ "$DOCKER_FRONTEND" -eq 1 ]; then
  echo "==> 启动前端 Nginx 容器..."
  (cd "$ROOT_DIR/docker" && docker compose --profile frontend up -d nginx)
  echo "==> 前端已启动: http://localhost:3000"
  echo "==> 后端运行中（日志：$BACKEND_LOG），按 Ctrl+C 停止后端"
  echo "==> 停止前端容器：./script/stop-middleware.sh"
  wait "$BACKEND_PID"
else
  echo "==> 后端启动中，请查看 $BACKEND_LOG"
  echo "==> 启动前端，按 Ctrl+C 同时停止前端与后端"

  if ! command -v npm >/dev/null 2>&1; then
    echo "错误：未找到 npm，请安装 Node.js 18+ 并配置 PATH。" >&2
    stop_backend
    exit 1
  fi

  cd "$CLIENT_DIR"

  if [ ! -d node_modules ]; then
    echo "==> 安装前端依赖（npm install）..."
    npm install
  fi

  echo "==> 启动前端 http://localhost:5173"
  npm run dev
fi