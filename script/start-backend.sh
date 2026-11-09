#!/usr/bin/env bash
# ============================================================
# 启动后端（Spring Boot）
#
# 默认对接 Docker 中间件（MySQL localhost:3307）；
# 传入 --local 表示对接本机中间件（按 application-dev.yml，MySQL 3306）。
#
# 用法：./script/start-backend.sh [--local] [--skip-build]
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SERVER_DIR="$ROOT_DIR/chrono-bbs-server"

MODE="docker"
SKIP_BUILD=0

usage() {
  cat <<'EOF'
用法: start-backend.sh [--local] [--skip-build]

  --local       对接本机中间件（MySQL 3306），默认对接 Docker 中间件（MySQL 3307）
  --skip-build  跳过 mvn clean install，直接运行已构建的模块
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

# 可选：从仓库根目录 .env 加载 OSS 等环境变量（.env 已被 .gitignore 忽略）
if [ -f "$ROOT_DIR/.env" ]; then
  echo "==> 加载环境变量: $ROOT_DIR/.env"
  set -a
  # shellcheck disable=SC1090
  . "$ROOT_DIR/.env"
  set +a
fi

# 未配置 OSS 密钥时以占位值启动，避免配置绑定失败；图片上传功能将不可用
if [ -z "${ALIYUN_OSS_ACCESS_KEY_ID:-}" ] || [ -z "${ALIYUN_OSS_ACCESS_KEY_SECRET:-}" ]; then
  echo "提示：未配置 ALIYUN_OSS_ACCESS_KEY_ID / ALIYUN_OSS_ACCESS_KEY_SECRET，"
  echo "      图片上传功能不可用。可在仓库根目录创建 .env 后重新启动。"
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
