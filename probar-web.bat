@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set "GRADLEW=%~dp0gradlew.bat"
set "DIST=webApp\build\dist\wasmJs\productionExecutable"

goto MAIN_MENU

:MAIN_MENU
cls
echo.
echo  ========================================
echo   LedxCalc - Version WEB (Wasm)
echo  ========================================
echo   Carpeta: %CD%
echo.
if exist "%DIST%\index.html" goto WEB_OK
echo   Build web: aun no compilado
goto WEB_STATUS_DONE
:WEB_OK
echo   Build web: listo
:WEB_STATUS_DONE
echo.
echo   [1] Compilar web (produccion)
echo   [2] Abrir en navegador (dev server)
echo   [3] Abrir carpeta del build
echo   [4] Servir build local (Python)
echo   [0] Salir
echo.
set /p OPCION=Elige opcion:
if "%OPCION%"=="1" goto BUILD
if "%OPCION%"=="2" goto DEV
if "%OPCION%"=="3" goto OPEN
if "%OPCION%"=="4" goto SERVE
if "%OPCION%"=="0" exit /b 0
goto MAIN_MENU

:BUILD
echo.
echo Compilando version web...
call "%GRADLEW%" :webApp:wasmJsBrowserDistribution --no-daemon
if errorlevel 1 goto BUILD_FAIL
echo.
echo OK. Archivos en: %DIST%
pause
goto MAIN_MENU
:BUILD_FAIL
echo ERROR: fallo la compilacion web.
pause
goto MAIN_MENU

:DEV
echo.
echo Iniciando servidor de desarrollo (Ctrl+C para detener)...
call "%GRADLEW%" :webApp:wasmJsBrowserDevelopmentRun --no-daemon
pause
goto MAIN_MENU

:OPEN
if not exist "%DIST%" goto NO_BUILD
start "" "%DIST%"
goto MAIN_MENU
:NO_BUILD
echo Aun no hay build. Usa opcion 1 primero.
pause
goto MAIN_MENU

:SERVE
if not exist "%DIST%\index.html" goto NO_BUILD
echo Sirviendo en http://localhost:8080
cd /d "%DIST%"
python -m http.server 8080
pause
goto MAIN_MENU