#!/usr/bin/env bash
# ============================================================
# 一键启动：中间件 -> 后端 -> 前端
#
# 默认使用 Docker 中间件；--local 使用本机中间件。
# 后端在后台运行，日志写入 logs/backend.log；
# 前端在前台运行，Ctrl+C 会同时停止前后端。
#
# 用法：./script/start-all.sh [--local] [--skip-build]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

MODE="docker"
SKIP_BUILD=0

usage() {
  cat <<'EOF'
用法: start-all.sh [--local] [--skip-build]

  --local       使用本机中间件（默认使用 Docker 中间件）
  --skip-build  跳过后端 Maven 构建
EOF
}

for arg in "$@"; do
  case "$arg" in
    --local) MODE="local" ;;
    --skip-build) SKIP_BUILD=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

MODE_ARG=""
BACKEND_ARGS=""
if [ "$MODE" = "local" ]; then
  MODE_ARG="--local"
  BACKEND_ARGS="--local"
fi
if [ "$SKIP_BUILD" -eq 1 ]; then
  BACKEND_ARGS="$BACKEND_ARGS --skip-build"
fi

# 1. 中间件
# shellcheck disable=SC2086
"$SCRIPT_DIR/start-middleware.sh" $MODE_ARG

# 2. 后端（后台）
mkdir -p "$ROOT_DIR/logs"
BACKEND_LOG="$ROOT_DIR/logs/backend.log"
echo "==> 启动后端（日志：$BACKEND_LOG）"
# shellcheck disable=SC2086
"$SCRIPT_DIR/start-backend.sh" $BACKEND_ARGS >"$BACKEND_LOG" 2>&1 &
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

# 3. 前端（前台）
echo "==> 后端启动中，请查看 $BACKEND_LOG"
echo "==> 启动前端，按 Ctrl+C 同时停止前端与后端"
"$SCRIPT_DIR/start-frontend.sh"
