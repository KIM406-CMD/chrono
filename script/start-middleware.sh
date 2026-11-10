#!/usr/bin/env bash
# ============================================================
# 启动开发中间件（MySQL / Redis / Elasticsearch）
#
# 默认使用 docker/docker-compose.yml 启动；
# 传入 --local 表示使用本机已安装的中间件，仅做端口检查。
#
# 用法：./script/start-middleware.sh [--local]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$ROOT_DIR/docker"

MODE="docker"

usage() {
  cat <<'EOF'
用法: start-middleware.sh [--local]

  无参数    使用 Docker Compose 启动 MySQL / Redis / Elasticsearch
  --local   不使用 Docker，检查本机 3306 / 6379 / 9200 端口是否可用
EOF
}

for arg in "$@"; do
  case "$arg" in
    --local) MODE="local" ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

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

if [ "$MODE" = "local" ]; then
  echo "==> 使用本机中间件（不启动 Docker），检查端口..."
  check_port localhost 3306 "MySQL"
  check_port localhost 6379 "Redis"
  check_port localhost 9200 "Elasticsearch"
  if [ "$missing" -ne 0 ]; then
    echo "警告：部分端口不可用，请先启动本机中间件，或改用 Docker 模式。" >&2
    exit 1
  fi
  echo "==> 本机中间件检查通过"
  exit 0
fi

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
