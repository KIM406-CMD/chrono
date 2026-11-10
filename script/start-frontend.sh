#!/usr/bin/env bash
# ============================================================
# 启动前端（Vite 开发服务器）
#
# node_modules 不存在时自动执行 npm install；
# 传入 --install 可强制重新安装依赖。
#
# 用法：./script/start-frontend.sh [--install]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CLIENT_DIR="$ROOT_DIR/chrono-bbs-client"

FORCE_INSTALL=0

usage() {
  cat <<'EOF'
用法: start-frontend.sh [--install]

  --install  强制重新执行 npm install
EOF
}

for arg in "$@"; do
  case "$arg" in
    --install) FORCE_INSTALL=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

if ! command -v npm >/dev/null 2>&1; then
  echo "错误：未找到 npm，请安装 Node.js 18+ 并配置 PATH。" >&2
  exit 1
fi

cd "$CLIENT_DIR"

if [ "$FORCE_INSTALL" -eq 1 ] || [ ! -d node_modules ]; then
  echo "==> 安装前端依赖（npm install）..."
  npm install
fi

echo "==> 启动前端 http://localhost:5173"
exec npm run dev
