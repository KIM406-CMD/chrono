@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem Start middleware, backend and frontend.
rem Default: Docker middleware. --local: use local middleware.
rem Backend and frontend start in separate console windows.
rem Usage: script\start-all.bat [--local] [--skip-build]

set "SCRIPT_DIR=%~dp0"
set "MODE_ARG="
set "BUILD_ARG="

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--local" (
  set "MODE_ARG=--local"
  shift
  goto parse_args
)
if /I "%~1"=="--skip-build" (
  set "BUILD_ARG=--skip-build"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo 未知参数: %~1
exit /b 1

:usage
echo 用法: start-all.bat [--local] [--skip-build]
echo.
echo   --local       使用本机中间件（默认使用 Docker 中间件）
echo   --skip-build  跳过后端 Maven 构建
exit /b 0

:args_done
rem Step 1: middleware (foreground, so health check is visible).
call "%SCRIPT_DIR%start-middleware.bat" %MODE_ARG%
if errorlevel 1 exit /b 1

rem Step 2: backend in a new window.
echo [信息] 在新窗口启动后端...
start "Chrono BBS 后端" cmd /k ""%SCRIPT_DIR%start-backend.bat" %MODE_ARG% %BUILD_ARG%"

rem Step 3: frontend in a new window.
echo [信息] 在新窗口启动前端...
start "Chrono BBS 前端" cmd /k ""%SCRIPT_DIR%start-frontend.bat""

echo.
echo 已在新窗口启动后端与前端：
echo   后端 http://localhost:8080/doc.html
echo   前端 http://localhost:5173
echo 关闭对应窗口即可停止服务。
exit /b 0
