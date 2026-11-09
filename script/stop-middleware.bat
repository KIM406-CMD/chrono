@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem Stop Docker middleware.
rem Usage: script\stop-middleware.bat [--volumes]
rem --volumes also deletes data volumes (wipes MySQL / Redis / ES data).

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "DOCKER_DIR=%ROOT_DIR%\docker"
set "REMOVE_VOLUMES=0"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--volumes" (
  set "REMOVE_VOLUMES=1"
  shift
  goto parse_args
)
if /I "%~1"=="-v" (
  set "REMOVE_VOLUMES=1"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo 未知参数: %~1
exit /b 1

:usage
echo 用法: stop-middleware.bat [--volumes]
echo.
echo   --volumes  停止并删除数据卷（下次启动会重新执行 initial\ 下的初始化脚本）
exit /b 0

:args_done
where docker >nul 2>nul
if errorlevel 1 (
  echo [错误] 未找到 docker。
  exit /b 1
)

pushd "%DOCKER_DIR%"
if "%REMOVE_VOLUMES%"=="1" (
  echo [信息] 停止中间件并删除数据卷...
  docker compose down -v
) else (
  echo [信息] 停止中间件（保留数据卷）...
  docker compose down
)
popd
