@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem Start frontend (Vite dev server).
rem Runs npm install when node_modules is missing.
rem --install forces a fresh npm install.
rem Usage: script\start-frontend.bat [--install]

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "CLIENT_DIR=%ROOT_DIR%\chrono-bbs-client"
set "FORCE_INSTALL=0"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--install" (
  set "FORCE_INSTALL=1"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo 未知参数: %~1
exit /b 1

:usage
echo 用法: start-frontend.bat [--install]
echo.
echo   --install  强制重新执行 npm install
exit /b 0

:args_done
where npm >nul 2>nul
if errorlevel 1 (
  echo [错误] 未找到 npm，请安装 Node.js 18+ 并配置 PATH。
  exit /b 1
)

pushd "%CLIENT_DIR%"
if "%FORCE_INSTALL%"=="1" goto install
if not exist "node_modules" goto install
goto run

:install
echo [1/2] 安装前端依赖（npm install）...
call npm install
if errorlevel 1 (
  popd
  exit /b 1
)

:run
echo [2/2] 启动前端 http://localhost:5173
call npm run dev
popd
