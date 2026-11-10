@echo off
setlocal EnableExtensions

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
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: start-middleware.bat [--local]
echo.
echo   (no option)  Start MySQL / Redis / Elasticsearch with Docker Compose
echo   --local      Do not use Docker; check local ports 3306 / 6379 / 9200
exit /b 0

:args_done
if /I "%MODE%"=="local" goto local_mode

where docker >nul 2>nul
if errorlevel 1 (
  echo [ERROR] docker not found. Install Docker Desktop or use --local.
  exit /b 1
)
docker compose version >nul 2>nul
if errorlevel 1 (
  echo [ERROR] docker compose is unavailable. Upgrade Docker Desktop.
  exit /b 1
)

echo [1/2] Starting Docker middleware ^(MySQL 3307 / Redis 6379 / ES 9200^)...
pushd "%DOCKER_DIR%"
docker compose up -d --wait
if errorlevel 1 (
  echo [INFO] Health check failed or --wait is unsupported; falling back to detached start.
  docker compose up -d
)
docker compose ps
popd
echo [2/2] Middleware started. Stop with: script\stop-middleware.bat
exit /b 0

:local_mode
echo [1/2] Using local middleware, checking ports...
set "MISSING=0"
call :check_port 3306 MySQL
call :check_port 6379 Redis
call :check_port 9200 Elasticsearch
if "%MISSING%"=="1" (
  echo [WARN] Some ports are unavailable. Start local middleware or use Docker mode.
  exit /b 1
)
echo [2/2] Local middleware check passed
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
