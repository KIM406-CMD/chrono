@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem Start dev middleware (MySQL / Redis / Elasticsearch).
rem Default: docker compose. Use --local to only check local ports.
rem Usage: script\start-middleware.bat [--local]

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "DOCKER_DIR=%ROOT_DIR%\docker"
set "MODE=docker"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--local" (
  set "MODE=local"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo 未知参数: %~1
exit /b 1

:usage
echo 用法: start-middleware.bat [--local]
echo.
echo   无参数    使用 Docker Compose 启动 MySQL / Redis / Elasticsearch
echo   --local   不使用 Docker，检查本机 3306 / 6379 / 9200 端口是否可用
exit /b 0

:args_done
if /I "%MODE%"=="local" goto local_mode

where docker >nul 2>nul
if errorlevel 1 (
  echo [错误] 未找到 docker，请安装 Docker Desktop，或使用 --local 模式。
  exit /b 1
)
docker compose version >nul 2>nul
if errorlevel 1 (
  echo [错误] docker compose 不可用，请升级 Docker Desktop。
  exit /b 1
)

echo [1/2] 启动 Docker 中间件（MySQL 3307 / Redis 6379 / ES 9200）...
pushd "%DOCKER_DIR%"
docker compose up -d --wait
if errorlevel 1 (
  echo [提示] 健康检查未通过或当前 Compose 不支持 --wait，改用后台启动。
  docker compose up -d
)
docker compose ps
popd
echo [2/2] 中间件已启动。停止：script\stop-middleware.bat
exit /b 0

:local_mode
echo [1/2] 使用本机中间件（不启动 Docker），检查端口...
set "MISSING=0"
call :check_port 3306 MySQL
call :check_port 6379 Redis
call :check_port 9200 Elasticsearch
if "%MISSING%"=="1" (
  echo [警告] 部分端口不可用，请先启动本机中间件，或改用 Docker 模式。
  exit /b 1
)
echo [2/2] 本机中间件检查通过
exit /b 0

:check_port
powershell -NoProfile -Command "if ((Test-NetConnection -ComputerName localhost -Port %1 -WarningAction SilentlyContinue).TcpTestSucceeded) { exit 0 } else { exit 1 }" >nul 2>nul
if errorlevel 1 (
  echo   [FAIL] %2  localhost:%1
  set "MISSING=1"
) else (
  echo   [OK]   %2  localhost:%1
)
exit /b 0
