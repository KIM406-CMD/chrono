@echo off
setlocal EnableExtensions

rem Start backend (Spring Boot).
rem Default: Docker middleware (MySQL localhost:3307).
rem --local: use local middleware (application-dev.yml, MySQL 3306).
rem Usage: script\start-backend.bat [--local] [--skip-build]

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "SERVER_DIR=%ROOT_DIR%\chrono-bbs-server"
set "MODE=docker"
set "SKIP_BUILD=0"

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
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: start-backend.bat [--local] [--skip-build]
echo.
echo   --local       Use local middleware (MySQL 3306); default is Docker (3307)
echo   --skip-build  Skip Maven build and run existing classes
exit /b 0

:args_done
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
