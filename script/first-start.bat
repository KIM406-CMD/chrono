@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ============================================================
rem Chrono BBS - first-time environment setup (Windows)
rem
rem Checks JDK 21+, Maven 3.8+, Node.js 18+ and Docker Desktop.
rem Missing tools are installed through winget after confirmation,
rem then the script delegates to start-all.bat.
rem
rem Usage: script\first-start.bat [--yes] [--check] [--local]
rem                               [--skip-build] [--docker-frontend]
rem ============================================================

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
set "ASSUME_YES=0"
set "CHECK_ONLY=0"
set "MODE=docker"
set "FORWARD_ARGS="
set "ENV_OK=1"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--yes" (
  set "ASSUME_YES=1"
  shift
  goto parse_args
)
if /I "%~1"=="-y" (
  set "ASSUME_YES=1"
  shift
  goto parse_args
)
if /I "%~1"=="--check" (
  set "CHECK_ONLY=1"
  shift
  goto parse_args
)
if /I "%~1"=="--local" (
  set "MODE=local"
  set "FORWARD_ARGS=!FORWARD_ARGS! --local"
  shift
  goto parse_args
)
if /I "%~1"=="--skip-build" (
  set "FORWARD_ARGS=!FORWARD_ARGS! --skip-build"
  shift
  goto parse_args
)
if /I "%~1"=="--docker-frontend" (
  set "FORWARD_ARGS=!FORWARD_ARGS! --docker-frontend"
  shift
  goto parse_args
)
if /I "%~1"=="-h" goto usage
if /I "%~1"=="--help" goto usage
echo Unknown argument: %~1
exit /b 1

:usage
echo Usage: first-start.bat [--yes] [--check] [--local] [--skip-build] [--docker-frontend]
echo.
echo   --yes              Install missing tools without asking
echo   --check            Only check the environment, do not install or start
echo   --local            Use local middleware instead of Docker
echo   --skip-build       Skip backend Maven build
echo   --docker-frontend  Start Docker Nginx container instead of Vite dev server
exit /b 0

:args_done
echo ============================================================
echo  Chrono BBS - first-time environment setup
echo ============================================================
echo.
echo  Required: JDK 21+, Maven 3.8+, Node.js 18+,
echo            Docker Desktop (Docker mode only).
echo  Missing tools can be installed with winget.
echo.

where winget >nul 2>nul
if errorlevel 1 (
  echo [WARN] winget not found. Install "App Installer" from Microsoft Store,
  echo        or install the required tools manually and rerun this script.
  set "WINGET_OK=0"
) else (
  set "WINGET_OK=1"
)

rem ---------- JDK ----------
call :check_java
if "!JAVA_OK!"=="1" (
  echo [OK]      Java 21+  ^(version !JAVA_VER!^)
) else (
  call :need_install "JDK 21" "EclipseAdoptium.Temurin.21.JDK"
)

rem ---------- Maven ----------
call :check_maven
if "!MAVEN_OK!"=="1" (
  echo [OK]      Maven 3.8+  ^(version !MAVEN_VER!^)
) else (
  call :need_install "Maven 3.8+" "Apache.Maven"
)

rem ---------- Node.js ----------
call :check_node
if "!NODE_OK!"=="1" (
  echo [OK]      Node.js 18+  ^(version !NODE_VER!^)
) else (
  call :need_install "Node.js 18+" "OpenJS.NodeJS.LTS"
)

rem ---------- Docker ----------
if /I "%MODE%"=="docker" (
  call :check_docker
  if "!DOCKER_OK!"=="1" (
    echo [OK]      Docker Compose
  ) else (
    call :check_wsl
    call :need_install "Docker Desktop" "Docker.DockerDesktop"
    call :refresh_env
    call :check_docker
  )
) else (
  echo [INFO]    --local mode: Docker is not required.
  set "PORTS_OK=1"
  call :check_port 3306 MySQL
  call :check_port 6379 Redis
  call :check_port 9200 Elasticsearch
  if "!PORTS_OK!"=="0" (
    echo [WARN]    Some local middleware ports are closed.
    echo           Start MySQL / Redis / Elasticsearch, or use Docker mode.
    set "ENV_OK=0"
  )
)

rem ---------- Re-check after installation ----------
if "!WINGET_OK!"=="1" call :refresh_env
call :check_java
call :check_maven
call :check_node
if /I "%MODE%"=="docker" call :check_docker

if not "!JAVA_OK!"=="1" set "ENV_OK=0"
if not "!MAVEN_OK!"=="1" set "ENV_OK=0"
if not "!NODE_OK!"=="1" set "ENV_OK=0"
if /I "%MODE%"=="docker" if not "!DOCKER_OK!"=="1" set "ENV_OK=0"

if "%ENV_OK%"=="0" (
  echo.
  echo [ERROR] Environment is not ready. Fix the items above and rerun:
  echo           script\first-start.bat
  echo         If a tool was just installed, close and reopen the terminal first.
  echo         Already have local MySQL / Redis / Elasticsearch? Use --local.
  exit /b 1
)

if "%CHECK_ONLY%"=="1" (
  echo.
  echo [OK] Environment check passed.
  exit /b 0
)

if /I "%MODE%"=="docker" (
  call :start_docker
  if errorlevel 1 (
    echo.
    echo [ERROR] Docker daemon is not running.
    echo         Start Docker Desktop, wait for "Engine running", then rerun this script.
    exit /b 1
  )
)

echo.
echo ============================================================
echo  Environment ready. Starting middleware, backend and frontend...
echo ============================================================
echo.
call "%SCRIPT_DIR%start-all.bat"!FORWARD_ARGS!
exit /b 0

rem ============================================================
rem Environment checks
rem ============================================================

:check_java
set "JAVA_OK=0"
set "JAVA_VER="
where java >nul 2>nul
if errorlevel 1 exit /b 0
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VER=%%~v"
if not defined JAVA_VER exit /b 0
echo(!JAVA_VER!| findstr /r /c:"^[0-9][0-9]*\." >nul
if errorlevel 1 exit /b 0
for /f "tokens=1 delims=." %%m in ("!JAVA_VER!") do set "JAVA_MAJOR=%%m"
if defined JAVA_MAJOR if !JAVA_MAJOR! GEQ 21 set "JAVA_OK=1"
exit /b 0

:check_maven
set "MAVEN_OK=0"
set "MAVEN_VER="
where mvn >nul 2>nul
if errorlevel 1 exit /b 0
for /f "tokens=3" %%v in ('mvn -v 2^>nul ^| findstr /b /i /c:"Apache Maven "') do set "MAVEN_VER=%%v"
if not defined MAVEN_VER exit /b 0
echo(!MAVEN_VER!| findstr /r /c:"^[0-9][0-9]*\.[0-9]" >nul
if errorlevel 1 exit /b 0
for /f "tokens=1,2 delims=." %%a in ("!MAVEN_VER!") do (
  set "MAVEN_MAJOR=%%a"
  set "MAVEN_MINOR=%%b"
)
if defined MAVEN_MAJOR if !MAVEN_MAJOR! GTR 3 set "MAVEN_OK=1"
if defined MAVEN_MAJOR if !MAVEN_MAJOR! EQU 3 if defined MAVEN_MINOR if !MAVEN_MINOR! GEQ 8 set "MAVEN_OK=1"
exit /b 0

:check_node
set "NODE_OK=0"
set "NODE_VER="
where node >nul 2>nul
if errorlevel 1 exit /b 0
where npm >nul 2>nul
if errorlevel 1 exit /b 0
for /f "delims=" %%v in ('node -v 2^>nul') do set "NODE_VER=%%v"
if not defined NODE_VER exit /b 0
set "NODE_VER=!NODE_VER:v=!"
echo(!NODE_VER!| findstr /r /c:"^[0-9][0-9]*\." >nul
if errorlevel 1 exit /b 0
for /f "tokens=1 delims=." %%m in ("!NODE_VER!") do set "NODE_MAJOR=%%m"
if defined NODE_MAJOR if !NODE_MAJOR! GEQ 18 set "NODE_OK=1"
exit /b 0

:check_docker
set "DOCKER_OK=0"
set "DOCKER_RUNNING=0"
where docker >nul 2>nul
if errorlevel 1 exit /b 0
docker compose version >nul 2>nul
if errorlevel 1 exit /b 0
set "DOCKER_OK=1"
docker info >nul 2>nul
if not errorlevel 1 set "DOCKER_RUNNING=1"
exit /b 0

:check_wsl
where wsl >nul 2>nul
if errorlevel 1 (
  echo [WARN]    WSL2 was not found. Docker Desktop requires WSL2.
  echo           Open an administrator terminal, run: wsl --install
  echo           Reboot, then run this script again.
  exit /b 1
)
wsl --status >nul 2>nul
if errorlevel 1 (
  echo [WARN]    WSL2 may not be fully configured. Docker Desktop setup may ask for it.
)
exit /b 0

rem ============================================================
rem Installation helpers
rem ============================================================

:need_install
rem %1 = display name, %2 = winget package id
echo [MISSING] %~1
if "%CHECK_ONLY%"=="1" (
  set "ENV_OK=0"
  exit /b 0
)
if "%WINGET_OK%"=="0" (
  echo           Install %~1 manually, then rerun this script.
  set "ENV_OK=0"
  exit /b 0
)
if "%ASSUME_YES%"=="0" (
  set "ANSWER="
  set /p "ANSWER=          Install %~1 with winget now? [Y/n] "
  if /I "!ANSWER!"=="n" (
    echo           Skipped %~1.
    set "ENV_OK=0"
    exit /b 0
  )
)
echo           Running: winget install --id %~2 -e --accept-package-agreements --accept-source-agreements
winget install --id %~2 -e --accept-package-agreements --accept-source-agreements
if errorlevel 1 (
  echo [ERROR]   Failed to install %~1. Install it manually and rerun.
  set "ENV_OK=0"
) else (
  echo [OK]      %~1 installed.
)
exit /b 0

:refresh_env
rem Re-read PATH/JAVA_HOME from the registry so freshly installed tools work
rem without closing this window.
for /f "usebackq delims=" %%P in (`powershell -NoProfile -Command "[Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [Environment]::GetEnvironmentVariable('Path','User')"`) do set "PATH=%%P"
set "JAVA_HOME="
if exist "%ProgramFiles%\Eclipse Adoptium" (
  for /d %%d in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%~d"
)
if not defined JAVA_HOME (
  for /f "usebackq delims=" %%J in (`powershell -NoProfile -Command "$v=[Environment]::GetEnvironmentVariable('JAVA_HOME','Machine'); if(-not $v){$v=[Environment]::GetEnvironmentVariable('JAVA_HOME','User')}; $v"`) do set "JAVA_HOME=%%J"
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
if exist "%ProgramFiles%\nodejs\node.exe" set "PATH=%ProgramFiles%\nodejs;%PATH%"
if exist "%LOCALAPPDATA%\Microsoft\WinGet\Links" set "PATH=%LOCALAPPDATA%\Microsoft\WinGet\Links;%PATH%"
exit /b 0

:start_docker
if "!DOCKER_RUNNING!"=="1" (
  echo [OK]      Docker daemon is running.
  exit /b 0
)
echo [INFO]    Starting Docker Desktop...
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
  start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
) else (
  echo [WARN]    Docker Desktop executable not found. Start it manually.
)
for /l %%i in (1,1,60) do (
  docker info >nul 2>nul
  if not errorlevel 1 (
    echo [OK]      Docker daemon is running.
    exit /b 0
  )
  >nul ping -n 3 127.0.0.1
)
exit /b 1

:check_port
powershell -NoProfile -Command "if ((Test-NetConnection -ComputerName localhost -Port %1 -WarningAction SilentlyContinue).TcpTestSucceeded) { exit 0 } else { exit 1 }" >nul 2>nul
if errorlevel 1 (
  echo   [FAIL]  %2 localhost:%1
  set "PORTS_OK=0"
) else (
  echo   [OK]    %2 localhost:%1
)
exit /b 0
