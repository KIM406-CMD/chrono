@echo off
setlocal EnableExtensions

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
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: start-all.bat [--local] [--skip-build]
echo.
echo   --local       Use local middleware instead of Docker
echo   --skip-build  Skip backend Maven build
exit /b 0

:args_done
rem Step 1: middleware (foreground, so health check is visible).
call "%SCRIPT_DIR%start-middleware.bat" %MODE_ARG%
if errorlevel 1 exit /b 1

rem Step 2: backend in a new window.
echo [INFO] Starting backend in a new window...
start "Chrono BBS Backend" cmd /k ""%SCRIPT_DIR%start-backend.bat" %MODE_ARG% %BUILD_ARG%"

rem Step 3: frontend in a new window.
echo [INFO] Starting frontend in a new window...
start "Chrono BBS Frontend" cmd /k ""%SCRIPT_DIR%start-frontend.bat""

echo.
echo Backend and frontend have been started in separate windows:
echo   Backend  http://localhost:8080/doc.html
echo   Frontend http://localhost:5173
echo Close the corresponding window to stop the service.
exit /b 0
