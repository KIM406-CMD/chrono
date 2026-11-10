@echo off
setlocal EnableExtensions

rem Start middleware, backend and frontend.
rem Default: Docker middleware. --local: use local middleware.
rem Backend and frontend start in separate console windows.
rem Usage: script\start-all.bat [--local] [--skip-build] [--docker-frontend]

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "MODE_ARG="
set "BUILD_ARG="
set "DOCKER_FRONTEND=0"

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
if /I "%~1"=="--docker-frontend" (
  set "DOCKER_FRONTEND=1"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: start-all.bat [--local] [--skip-build] [--docker-frontend]
echo.
echo   --local            Use local middleware instead of Docker
echo   --skip-build       Skip backend Maven build
echo   --docker-frontend  Start Docker Nginx container instead of Vite dev server
exit /b 0

:args_done
rem Step 1: middleware (foreground, so health check is visible).
call "%SCRIPT_DIR%start-middleware.bat" %MODE_ARG%
if errorlevel 1 exit /b 1

rem Step 2: backend in a new window.
echo [INFO] Starting backend in a new window...
start "Chrono BBS Backend" cmd /k ""%SCRIPT_DIR%start-backend.bat" %MODE_ARG% %BUILD_ARG%"

rem Step 3: frontend (Docker Nginx or Vite dev server).
if "%DOCKER_FRONTEND%"=="1" (
  echo [INFO] Starting frontend Nginx container...
  pushd "%ROOT_DIR%\docker"
  docker compose --profile frontend up -d nginx
  popd
  echo.
  echo Backend has been started in a new window:
  echo   Backend  http://localhost:8080/doc.html
  echo   Frontend http://localhost:3000
  echo Stop the backend by closing its window; stop containers with script\stop-middleware.bat
) else (
  echo [INFO] Starting frontend in a new window...
  start "Chrono BBS Frontend" cmd /k ""%SCRIPT_DIR%start-frontend.bat""
  echo.
  echo Backend and frontend have been started in separate windows:
  echo   Backend  http://localhost:8080/doc.html
  echo   Frontend http://localhost:5173
  echo Close the corresponding window to stop the service.
)
exit /b 0
