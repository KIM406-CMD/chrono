@echo off
setlocal EnableExtensions

rem One-command startup: middleware -> backend -> frontend.
rem Default: Docker middleware. --local: use local middleware.
rem Backend and frontend start in separate console windows.
rem Usage: script\start-all.bat [--local] [--skip-build] [--docker-frontend]

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "DOCKER_DIR=%ROOT_DIR%\docker"
set "SERVER_DIR=%ROOT_DIR%\chrono-bbs-server"
set "CLIENT_DIR=%ROOT_DIR%\chrono-bbs-client"
set "MODE=docker"
set "SKIP_BUILD=0"
set "DOCKER_FRONTEND=0"

rem Internal entry points used to launch backend/frontend in separate windows.
if /I "%~1"=="--backend-window" (
  shift
  goto backend_window
)
if /I "%~1"=="--frontend-window" (
  shift
  goto frontend_window
)

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--local" (
  set "MODE=local"
  shift
  goto parse_args
)
if /I "%~1"=="--skip-build" (
  set "SKIP_BUILD=1"
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
rem Step 1: middleware.
if /I "%MODE%"=="local" goto local_middleware

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

echo [1/3] Starting Docker middleware ^(MySQL 3307 / Redis 6379 / ES 9200^)...
pushd "%DOCKER_DIR%"
docker compose up -d --wait
if errorlevel 1 (
  echo [INFO] Health check failed or --wait is unsupported; falling back to detached start.
  docker compose up -d
)
docker compose ps
popd
goto middleware_done

:local_middleware
echo [1/3] Using local middleware, checking ports...
set "MISSING=0"
call :check_port 3306 MySQL
call :check_port 6379 Redis
call :check_port 9200 Elasticsearch
if "%MISSING%"=="1" (
  echo [WARN] Some ports are unavailable. Start local middleware or use Docker mode.
  exit /b 1
)
echo Local middleware check passed

:middleware_done
rem Step 2: backend in a new window.
echo [2/3] Starting backend in a new window...
start "Chrono BBS Backend" cmd /k ""%SCRIPT_DIR%start-all.bat" --backend-window %MODE% %SKIP_BUILD%"

rem Step 3: frontend (Docker Nginx or Vite dev server).
if "%DOCKER_FRONTEND%"=="1" (
  echo [3/3] Starting frontend Nginx container...
  pushd "%DOCKER_DIR%"
  docker compose --profile frontend up -d nginx
  popd
  echo.
  echo Backend has been started in a new window:
  echo   Backend  http://localhost:8080/doc.html
  echo   Frontend http://localhost:3000
  echo Stop the backend by closing its window; stop containers with script\stop-middleware.bat
) else (
  echo [3/3] Starting frontend in a new window...
  start "Chrono BBS Frontend" cmd /k ""%SCRIPT_DIR%start-all.bat" --frontend-window"
  echo.
  echo Backend and frontend have been started in separate windows:
  echo   Backend  http://localhost:8080/doc.html
  echo   Frontend http://localhost:5173
  echo Close the corresponding window to stop the service.
)
exit /b 0

rem ------------------------------------------------------------------
rem Backend window entry point (internal).
rem ------------------------------------------------------------------
:backend_window
set "MODE=%~1"
set "SKIP_BUILD=%~2"
if "%MODE%"=="" set "MODE=docker"
if "%SKIP_BUILD%"=="" set "SKIP_BUILD=0"

if exist "%ROOT_DIR%\.env" (
  echo [INFO] Loading environment: %ROOT_DIR%\.env
  for /f "usebackq eol=# tokens=1* delims==" %%A in ("%ROOT_DIR%\.env") do (
    if not "%%A"=="" set "%%A=%%B"
  )
)

if not defined ALIYUN_OSS_ACCESS_KEY_ID (
  echo [INFO] No OSS key configured; images are stored in local uploads/
  set "ALIYUN_OSS_ACCESS_KEY_ID=none"
)
if not defined ALIYUN_OSS_ACCESS_KEY_SECRET (
  set "ALIYUN_OSS_ACCESS_KEY_SECRET=none"
)

where mvn >nul 2>nul
if errorlevel 1 (
  echo [ERROR] mvn not found. Install Maven 3.8+ and add it to PATH.
  exit /b 1
)
where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] java not found. Install JDK 21+ and set JAVA_HOME.
  exit /b 1
)

if /I "%MODE%"=="docker" (
  rem Docker MySQL is exposed on host port 3307.
  set "SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/chrono_bbs_dev?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
  echo [INFO] Middleware mode: Docker ^(MySQL localhost:3307^)
) else (
  echo [INFO] Middleware mode: local ^(MySQL per application-dev.yml^)
)

if "%SKIP_BUILD%"=="1" (
  echo [INFO] Skipping Maven build
) else (
  echo [1/2] Building backend ^(mvn clean install -DskipTests^)...
  pushd "%SERVER_DIR%"
  call mvn clean install -DskipTests
  if errorlevel 1 (
    popd
    exit /b 1
  )
  popd
)

echo [2/2] Starting backend http://localhost:8080 ^(Knife4j: /doc.html^)
pushd "%SERVER_DIR%"
call mvn -pl module-start spring-boot:run
popd
exit /b 0

rem ------------------------------------------------------------------
rem Frontend window entry point (internal).
rem ------------------------------------------------------------------
:frontend_window
where npm >nul 2>nul
if errorlevel 1 (
  echo [ERROR] npm not found. Install Node.js 18+ and add it to PATH.
  exit /b 1
)

pushd "%CLIENT_DIR%"
if exist "node_modules" goto run_frontend

echo [1/2] Installing frontend dependencies ^(npm install^)...
call npm install
if errorlevel 1 (
  popd
  exit /b 1
)

:run_frontend
echo [2/2] Starting frontend at http://localhost:5173
call npm run dev
popd
exit /b 0

rem ------------------------------------------------------------------
rem Port check helper (internal).
rem ------------------------------------------------------------------
:check_port
powershell -NoProfile -Command "if ((Test-NetConnection -ComputerName localhost -Port %1 -WarningAction SilentlyContinue).TcpTestSucceeded) { exit 0 } else { exit 1 }" >nul 2>nul
if errorlevel 1 (
  echo   [FAIL] %2  localhost:%1
  set "MISSING=1"
) else (
  echo   [OK]   %2  localhost:%1
)
exit /b 0