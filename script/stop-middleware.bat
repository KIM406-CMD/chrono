@echo off
setlocal EnableExtensions

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
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: stop-middleware.bat [--volumes]
echo.
echo   --volumes  Stop and delete data volumes (re-runs initial scripts next start)
exit /b 0

:args_done
where docker >nul 2>nul
if errorlevel 1 (
  echo [ERROR] docker not found.
  exit /b 1
)

pushd "%DOCKER_DIR%"
if "%REMOVE_VOLUMES%"=="1" (
  echo [INFO] Stopping middleware and deleting data volumes...
  docker compose down -v
) else (
  echo [INFO] Stopping middleware ^(keeping data volumes^)...
  docker compose down
)
popd
