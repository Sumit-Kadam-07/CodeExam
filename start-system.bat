

@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Starting Smart Coding Exam System
echo ========================================
echo.

:: ═══════════════════════════════════════════
:: Port Conflict Detection (Backend :5000)
:: ═══════════════════════════════════════════
echo Checking port 5000...
set BEPID=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /c:":5000 " ^| findstr LISTENING') do (
    if not defined BEPID set BEPID=%%p
)

if defined BEPID (
    echo Existing backend detected (PID !BEPID!). Verifying process...
    tasklist /FI "PID eq !BEPID!" 2>nul | findstr /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo This process belongs to this project. Stopping previous backend...
        taskkill /PID !BEPID! /T /F >nul 2>&1
        if !errorlevel! equ 0 (
            echo Previous backend stopped.
        ) else (
            echo WARNING: Could not terminate PID !BEPID!. It may belong to another process.
        )
    ) else (
        echo WARNING: Port 5000 is occupied by a non-Java process (PID !BEPID!). Proceeding anyway...
    )
    timeout /t 2 /nobreak >nul
) else (
    echo Port 5000 is free.
)
echo.

:: ═══════════════════════════════════════════
:: Step 1: Start Backend (Spring Boot on port 5000)
:: ═══════════════════════════════════════════
echo [1/2] Starting Backend...
start "Backend" cmd /c "cd /d %~dp0backend && ..\mvnw.cmd spring-boot:run"

:: Wait for backend to initialize
echo Waiting for backend to start...
timeout /t 20 /nobreak >nul

:: Verify backend is running on port 5000
set BEPID2=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /c:":5000 " ^| findstr LISTENING') do (
    if not defined BEPID2 set BEPID2=%%p
)
if defined BEPID2 (
    echo Backend started successfully on port 5000 (PID !BEPID2!).
) else (
    echo ERROR: Backend failed to start. Check backend/startup.log for details.
    exit /b 1
)
echo.

:: ═══════════════════════════════════════════
:: Port Conflict Detection (Frontend :5173)
:: ═══════════════════════════════════════════
echo Checking port 5173...
set FEPID=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /c:":5173 " ^| findstr LISTENING') do (
    if not defined FEPID set FEPID=%%p
)

if defined FEPID (
    echo Existing frontend detected (PID !FEPID!). Verifying process...
    tasklist /FI "PID eq !FEPID!" 2>nul | findstr /i "node.exe" >nul
    if !errorlevel! equ 0 (
        echo This process belongs to this project. Stopping previous frontend...
        taskkill /PID !FEPID! /F >nul 2>&1
        if !errorlevel! equ 0 (
            echo Previous frontend stopped.
        ) else (
            echo WARNING: Could not terminate PID !FEPID!. It may belong to another process.
        )
    ) else (
        echo WARNING: Port 5173 is occupied by a non-Node process (PID !FEPID!). Proceeding anyway...
    )
    timeout /t 2 /nobreak >nul
) else (
    echo Port 5173 is free.
)
echo.

:: ═══════════════════════════════════════════
:: Step 2: Start Frontend (Vite on port 5173)
:: ═══════════════════════════════════════════
echo [2/2] Starting Frontend...
start "Frontend" cmd /c "cd /d %~dp0frontend\frontend && npm run dev"

:: Wait for frontend to initialize
echo Waiting for frontend to start...
timeout /t 10 /nobreak >nul

:: Verify frontend is running on port 5173
set FEPID2=
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /c:":5173 " ^| findstr LISTENING') do (
    if not defined FEPID2 set FEPID2=%%p
)
if defined FEPID2 (
    echo Frontend started successfully on port 5173 (PID !FEPID2!).
) else (
    echo WARNING: Frontend may not have started. Check console for details.
)
echo.

echo ========================================
echo  System Started!
echo.
echo  Backend:  http://localhost:5000
echo  Frontend: http://localhost:5173
echo.
echo  Admin Login: admin / adminpass
echo ========================================
echo.

:: Open browser
timeout /t 3 /nobreak >nul
start http://localhost:5173

endlocal
