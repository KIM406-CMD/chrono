#!/usr/bin/env bash
# ============================================================
# 停止 Docker 中间件
#
# 用法：./script/stop-middleware.sh [--volumes]
#   --volumes  同时删除数据卷（会清空 MySQL / Redis / ES 数据）
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$ROOT_DIR/docker"

REMOVE_VOLUMES=0

usage() {
  cat <<'EOF'
用法: stop-middleware.sh [--volumes]

  --volumes  停止并删除数据卷（下次启动会重新执行 initial/ 下的初始化脚本）
EOF
}

for arg in "$@"; do
  case "$arg" in
    --volumes|-v) REMOVE_VOLUMES=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未找到 docker。" >&2
  exit 1
fi

cd "$DOCKER_DIR"
if [ "$REMOVE_VOLUMES" -eq 1 ]; then
  echo "==> 停止中间件并删除数据卷..."
  docker compose --profile frontend down -v
else
  echo "==> 停止中间件（保留数据卷）..."
  docker compose --profile frontend down
fi
