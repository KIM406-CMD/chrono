@echo off
setlocal EnableExtensions

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
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: start-frontend.bat [--install]
echo.
echo   --install  Force a fresh npm install
exit /b 0

:args_done
where npm >nul 2>nul
if errorlevel 1 (
  echo [ERROR] npm not found. Install Node.js 18+ and add it to PATH.
  exit /b 1
)

pushd "%CLIENT_DIR%"
if "%FORCE_INSTALL%"=="1" goto install
if not exist "node_modules" goto install
goto run

:install
echo [1/2] Installing frontend dependencies ^(npm install^)...
call npm install
if errorlevel 1 (
  popd
  exit /b 1
)

:run
echo [2/2] Starting frontend at http://localhost:5173
call npm run dev
popd
