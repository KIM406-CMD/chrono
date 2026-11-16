#!/usr/bin/env bash
# ============================================================
# Chrono BBS - 首次启动（macOS / Linux / Git Bash）
#
# 检查 JDK 21+、Maven 3.8+、Node.js 18+、Docker（及 Compose v2），
# 缺失项在确认后通过 Homebrew / apt / dnf / pacman 安装，
# 最后调用 start-all.sh 启动中间件、后端与前端。
#
# 用法：
#   ./script/first-start.sh [--yes] [--check] [--local]
#                           [--skip-build] [--docker-frontend]
#
# Windows 用户请直接运行 script\first-start.bat；
# 在 Git Bash 中运行本脚本会自动转交给 first-start.bat。
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OS="$(uname -s)"

ASSUME_YES=0
CHECK_ONLY=0
MODE="docker"
FORWARD_ARGS=()

usage() {
  cat <<'EOF'
用法: first-start.sh [--yes] [--check] [--local] [--skip-build] [--docker-frontend]

  --yes              缺失依赖直接安装，不再逐项确认
  --check            只检查环境，不安装、不启动
  --local            使用本机中间件（默认使用 Docker 中间件）
  --skip-build       跳过后端 Maven 构建
  --docker-frontend  启动 Docker Nginx 容器，不启动 Vite 开发服务器
EOF
}

for arg in "$@"; do
  case "$arg" in
    --yes|-y) ASSUME_YES=1 ;;
    --check) CHECK_ONLY=1 ;;
    --local) MODE="local"; FORWARD_ARGS+=("--local") ;;
    --skip-build) FORWARD_ARGS+=("--skip-build") ;;
    --docker-frontend) FORWARD_ARGS+=("--docker-frontend") ;;
    -h|--help) usage; exit 0 ;;
    *) echo "未知参数: $arg" >&2; usage >&2; exit 1 ;;
  esac
done

# Git Bash / MSYS / Cygwin：交给 Windows 批处理脚本处理 winget 安装与启动
case "$OS" in
  MINGW*|MSYS*|CYGWIN*)
    BAT_WIN="$(cygpath -w "$SCRIPT_DIR/first-start.bat" 2>/dev/null || printf '%s' "$SCRIPT_DIR/first-start.bat")"
    exec cmd //c "$BAT_WIN" "$@"
    ;;
esac

if [ -t 1 ]; then
  C_RESET=$'\033[0m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'; C_RED=$'\033[31m'; C_BLUE=$'\033[36m'
else
  C_RESET=""; C_GREEN=""; C_YELLOW=""; C_RED=""; C_BLUE=""
fi
info() { printf '%s[INFO]%s    %s\n' "$C_BLUE" "$C_RESET" "$*"; }
ok()   { printf '%s[OK]%s      %s\n' "$C_GREEN" "$C_RESET" "$*"; }
warn() { printf '%s[WARN]%s    %s\n' "$C_YELLOW" "$C_RESET" "$*"; }
err()  { printf '%s[ERROR]%s   %s\n' "$C_RED" "$C_RESET" "$*" >&2; }

have() { command -v "$1" >/dev/null 2>&1; }

java_ok() {
  have java || return 1
  local version major
  version="$(java -version 2>&1 | awk -F'"' '/version/ { print $2; exit }')"
  [ -n "$version" ] || return 1
  major="${version%%.*}"
  [ "$major" = "1" ] && major="$(printf '%s' "$version" | cut -d. -f2)"
  case "$major" in ''|*[!0-9]*) return 1 ;; esac
  [ "$major" -ge 21 ]
}

maven_ok() {
  have mvn || return 1
  local version major minor
  version="$(mvn -v 2>/dev/null | awk '/Apache Maven/ { print $3; exit }')"
  [ -n "$version" ] || return 1
  major="${version%%.*}"
  minor="${version#*.}"
  minor="${minor%%.*}"
  case "$major" in ''|*[!0-9]*) return 1 ;; esac
  case "$minor" in ''|*[!0-9]*) minor=0 ;; esac
  [ "$major" -gt 3 ] && return 0
  [ "$major" -eq 3 ] && [ "$minor" -ge 8 ] && return 0
  return 1
}

node_ok() {
  have node || return 1
  have npm || return 1
  local version major
  version="$(node -v 2>/dev/null)"
  version="${version#v}"
  major="${version%%.*}"
  case "$major" in ''|*[!0-9]*) return 1 ;; esac
  [ "$major" -ge 18 ]
}

docker_ok() {
  have docker || return 1
  docker compose version >/dev/null 2>&1
}

docker_running() { docker info >/dev/null 2>&1; }

ask() {
  [ "$ASSUME_YES" -eq 1 ] && return 0
  printf '          是否安装 %s？[Y/n] ' "$1"
  read -r answer || answer=""
  case "$answer" in
    n|N|no|NO) return 1 ;;
    *) return 0 ;;
  esac
}

detect_pm() {
  local pm
  if [ "$OS" = "Darwin" ]; then
    have brew && printf '%s' "brew"
    return 0
  fi
  for pm in apt-get dnf pacman; do
    if have "$pm"; then
      printf '%s' "$pm"
      return 0
    fi
  done
  return 1
}

PM="$(detect_pm || true)"

install_java() {
  case "$OS:$PM" in
    Darwin:brew) brew install --cask temurin@21 ;;
    Linux:apt-get) sudo apt-get update && sudo apt-get install -y openjdk-21-jdk ;;
    Linux:dnf) sudo dnf install -y java-21-openjdk-devel ;;
    Linux:pacman) sudo pacman -S --needed --noconfirm jdk21-openjdk ;;
    *) err "无法自动安装 JDK，请手动安装 OpenJDK 21 后重试。"; return 1 ;;
  esac
}

install_maven() {
  case "$OS:$PM" in
    Darwin:brew) brew install maven ;;
    Linux:apt-get) sudo apt-get update && sudo apt-get install -y maven ;;
    Linux:dnf) sudo dnf install -y maven ;;
    Linux:pacman) sudo pacman -S --needed --noconfirm maven ;;
    *) err "无法自动安装 Maven，请手动安装 Maven 3.8+ 后重试。"; return 1 ;;
  esac
}

install_node() {
  case "$OS:$PM" in
    Darwin:brew) brew install node ;;
    Linux:apt-get)
      # 发行版仓库的 Node 版本可能低于 18，使用 NodeSource 20.x 源
      curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
      sudo apt-get install -y nodejs
      ;;
    Linux:dnf) sudo dnf install -y nodejs npm ;;
    Linux:pacman) sudo pacman -S --needed --noconfirm nodejs npm ;;
    *) err "无法自动安装 Node.js，请手动安装 Node.js 18+ 后重试。"; return 1 ;;
  esac
}

install_docker() {
  case "$OS:$PM" in
    Darwin:brew)
      brew install --cask docker
      info "Docker Desktop 已安装，首次启动需要打开应用并完成授权。"
      ;;
    Linux:apt-get)
      sudo apt-get update
      sudo apt-get install -y docker.io
      # Ubuntu 24.04 使用 docker-compose-v2；部分发行版使用 docker-compose-plugin
      sudo apt-get install -y docker-compose-v2 2>/dev/null \
        || sudo apt-get install -y docker-compose-plugin 2>/dev/null \
        || warn "未找到 Compose v2 包，请安装 docker-compose-plugin。"
      sudo systemctl enable --now docker 2>/dev/null || true
      sudo usermod -aG docker "$USER" 2>/dev/null || true
      ;;
    Linux:dnf)
      sudo dnf install -y docker docker-compose-plugin
      sudo systemctl enable --now docker 2>/dev/null || true
      sudo usermod -aG docker "$USER" 2>/dev/null || true
      ;;
    Linux:pacman)
      sudo pacman -S --needed --noconfirm docker docker-compose
      sudo systemctl enable --now docker 2>/dev/null || true
      sudo usermod -aG docker "$USER" 2>/dev/null || true
      ;;
    *) err "无法自动安装 Docker，请手动安装 Docker Engine / Docker Desktop 后重试。"; return 1 ;;
  esac
}

start_docker() {
  docker_running && return 0
  case "$OS" in
    Darwin)
      if [ -d "/Applications/Docker.app" ]; then
        info "正在打开 Docker Desktop..."
        open -a Docker
      fi
      ;;
    Linux)
      sudo systemctl start docker 2>/dev/null || true
      ;;
  esac
  local i
  for i in $(seq 1 60); do
    if docker_running; then
      return 0
    fi
    sleep 2
  done
  return 1
}

port_open() {
  (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null && exec 3>&- && return 0
  return 1
}

echo "============================================================"
echo " Chrono BBS - 首次启动环境检查"
echo "============================================================"
echo
echo " 必需：JDK 21+、Maven 3.8+、Node.js 18+，Docker 模式另需 Docker。"
echo " 缺失项会在确认后自动安装。"
echo

MISSING=0

if java_ok; then
  ok "Java 21+（$(java -version 2>&1 | head -1 | sed 's/^[^"]*"//; s/".*//')）"
elif [ "$CHECK_ONLY" -eq 1 ]; then
  warn "未安装 JDK 21+"; MISSING=1
elif ask "JDK 21"; then
  install_java || MISSING=1
else
  warn "跳过 JDK 21"; MISSING=1
fi

if maven_ok; then
  ok "Maven 3.8+（$(mvn -v 2>/dev/null | head -1 | awk '{ print $3 }')）"
elif [ "$CHECK_ONLY" -eq 1 ]; then
  warn "未安装 Maven 3.8+"; MISSING=1
elif ask "Maven 3.8+"; then
  install_maven || MISSING=1
else
  warn "跳过 Maven 3.8+"; MISSING=1
fi

if node_ok; then
  ok "Node.js 18+（$(node -v 2>/dev/null)）"
elif [ "$CHECK_ONLY" -eq 1 ]; then
  warn "未安装 Node.js 18+"; MISSING=1
elif ask "Node.js 18+"; then
  install_node || MISSING=1
else
  warn "跳过 Node.js 18+"; MISSING=1
fi

if [ "$MODE" = "docker" ]; then
  if docker_ok; then
    ok "Docker Compose"
  elif [ "$CHECK_ONLY" -eq 1 ]; then
    warn "未安装 Docker / Compose v2"; MISSING=1
  elif ask "Docker"; then
    install_docker || MISSING=1
  else
    warn "跳过 Docker"; MISSING=1
  fi
else
  info "--local 模式：不检查 Docker，改为检查本机中间件端口。"
  for entry in "3306:MySQL" "6379:Redis" "9200:Elasticsearch"; do
    port="${entry%%:*}"; name="${entry#*:}"
    if port_open "$port"; then
      ok "$name localhost:$port"
    else
      warn "$name localhost:$port 未监听"
      MISSING=1
    fi
  done
fi

# 安装完成后重新检查，避免因安装失败或版本不达标而继续启动
if ! java_ok; then MISSING=1; fi
if ! maven_ok; then MISSING=1; fi
if ! node_ok; then MISSING=1; fi
if [ "$MODE" = "docker" ] && ! docker_ok; then MISSING=1; fi

if [ "$MISSING" -ne 0 ]; then
  echo
  err "环境尚未就绪。请根据上方提示安装缺失依赖后重新运行："
  echo "        ./script/first-start.sh"
  echo "      若刚安装完成，请先关闭并重新打开终端（Linux 上加入 docker 组后需要重新登录）。"
  echo "      已有本机 MySQL / Redis / Elasticsearch 时，可改用 --local。"
  exit 1
fi

if [ "$CHECK_ONLY" -eq 1 ]; then
  echo
  ok "环境检查通过。"
  exit 0
fi

if [ "$MODE" = "docker" ]; then
  if ! start_docker; then
    echo
    docker_error="$(docker info 2>&1 || true)"
    if printf '%s' "$docker_error" | grep -qi "permission denied"; then
      err "Docker 已运行，但当前用户无权访问。请注销并重新登录（或执行 newgrp docker）后重试。"
    else
      err "Docker 未启动。请启动 Docker Desktop / Docker 服务后重新运行本脚本。"
    fi
    exit 1
  fi
  ok "Docker daemon 运行中"
fi

echo
echo "============================================================"
echo " 环境就绪，开始启动中间件、后端与前端..."
echo "============================================================"
echo
exec "$SCRIPT_DIR/start-all.sh" "${FORWARD_ARGS[@]}"
