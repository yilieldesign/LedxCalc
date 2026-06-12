@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set "GRADLEW=%~dp0gradlew.bat"
set "DIST=webApp\build\dist\wasmJs\productionExecutable"
set "REPO_NAME=LedxCalc"

goto MAIN_MENU

:MAIN_MENU
cls
echo.
echo  ====================================================
echo   LedxCalc - PUBLICAR WEB (iPhone / Android / PC)
echo  ====================================================
echo   Carpeta: %CD%
echo.
if exist ".git" goto GIT_OK
echo   Git: NO inicializado (usa opcion 1)
goto GIT_STATUS_DONE
:GIT_OK
echo   Git: inicializado
:GIT_STATUS_DONE
if exist "%DIST%\index.html" goto BUILD_OK
echo   Build local: pendiente
goto BUILD_STATUS_DONE
:BUILD_OK
echo   Build local: listo
:BUILD_STATUS_DONE
echo.
echo   METODO RECOMENDADO: GitHub Pages (gratis, sin Mac)
echo.
echo   [1] Paso 1 - Inicializar Git y primer commit
echo   [2] Paso 2 - Compilar web en tu PC
echo   [3] Paso 3 - Subir a GitHub
echo   [4] Paso 4 - Activar GitHub Pages (instrucciones)
echo   [5] Probar build local (http://localhost:8080)
echo   [6] Abrir carpeta del build
echo   [0] Salir
echo.
set /p OPCION=Elige opcion:
if "%OPCION%"=="1" goto GIT_INIT
if "%OPCION%"=="2" goto BUILD
if "%OPCION%"=="3" goto GITHUB_PUSH
if "%OPCION%"=="4" goto GITHUB_PAGES
if "%OPCION%"=="5" goto SERVE
if "%OPCION%"=="6" goto OPEN
if "%OPCION%"=="0" exit /b 0
goto MAIN_MENU

:GIT_INIT
echo.
echo --- Paso 1: Git ---
git --version >nul 2>&1
if errorlevel 1 goto GIT_MISSING
if not exist ".git" goto GIT_CREATE
echo Git ya estaba inicializado.
goto GIT_CONFIG
:GIT_CREATE
git init -b main
echo Repositorio Git creado con rama main.
:GIT_CONFIG
git config user.name >nul 2>&1
if not errorlevel 1 goto GIT_ADD
echo.
echo Git necesita tu nombre y email (solo una vez):
set /p GIT_NAME=Tu nombre:
set /p GIT_EMAIL=Tu email de GitHub:
git config user.name "!GIT_NAME!"
git config user.email "!GIT_EMAIL!"
:GIT_ADD
git add -A
git status
echo.
set /p CONFIRM=Crear commit inicial? (S/N):
if /i not "%CONFIRM%"=="S" goto MAIN_MENU
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "LedxCalc: calculadora LED + version web Wasm"
if errorlevel 1 goto COMMIT_FAIL
echo.
echo OK. Commit creado.
goto GIT_INIT_DONE
:COMMIT_FAIL
echo.
echo Si dice nothing to commit, ya estaba commiteado. Continua con paso 3.
:GIT_INIT_DONE
pause
goto MAIN_MENU
:GIT_MISSING
echo ERROR: Git no esta instalado.
echo Descargalo de: https://git-scm.com/download/win
echo.
echo Si ya lo instalaste, cierra esta ventana y abre una nueva.
pause
goto MAIN_MENU

:BUILD
echo.
echo --- Compilando version web ---
call "%GRADLEW%" :webApp:wasmJsBrowserDistribution --no-daemon
if errorlevel 1 goto BUILD_FAIL
echo.
echo OK. Archivos en: %DIST%
pause
goto MAIN_MENU
:BUILD_FAIL
echo ERROR: fallo la compilacion.
pause
goto MAIN_MENU

:GITHUB_PUSH
echo.
echo --- Paso 3: Subir a GitHub ---
if not exist ".git" goto NEED_GIT
echo.
echo  A) Abre: https://github.com/new
echo  B) Nombre del repo: %REPO_NAME%
echo  C) Publico, SIN marcar Add README
echo  D) Create repository
echo  E) Copia la URL .git que te da GitHub
echo.
set /p GIT_URL=Pega aqui la URL del repo:
if "%GIT_URL%"=="" goto MAIN_MENU
git remote remove origin 2>nul
git remote add origin "%GIT_URL%"
git push -u origin main
if errorlevel 1 goto PUSH_FAIL
echo.
echo OK. Codigo subido a GitHub.
echo Ahora usa la opcion 4.
pause
goto MAIN_MENU
:PUSH_FAIL
echo.
echo GitHub no acepta contrasena normal. Usa un token:
echo https://github.com/settings/tokens (permiso repo)
echo Usuario: tu usuario de GitHub
echo Contrasena: el token
pause
goto MAIN_MENU
:NEED_GIT
echo Primero ejecuta la opcion 1.
pause
goto MAIN_MENU

:GITHUB_PAGES
echo.
echo --- Paso 4: Activar GitHub Pages ---
echo.
echo  1. Abre tu repo en GitHub
echo  2. Settings - Pages
echo  3. Source = GitHub Actions
echo  4. Pesta?a Actions - espera Deploy Web (Wasm) en verde
echo  5. Tu URL: https://TU_USUARIO.github.io/%REPO_NAME%/
echo  6. Abre esa URL en Safari del iPhone
echo.
echo  NOTA: Vercel compila con scripts/vercel-build.sh al hacer push.
echo        GitHub Actions tambien publica en Pages y puede enviar a Vercel.
echo.
pause
goto MAIN_MENU

:SERVE
if not exist "%DIST%\index.html" goto NEED_BUILD
echo Sirviendo en http://localhost:8080
echo Ctrl+C para detener.
cd /d "%DIST%"
python -m http.server 8080
pause
goto MAIN_MENU
:NEED_BUILD
echo Compila primero con opcion 2.
pause
goto MAIN_MENU

:OPEN
if not exist "%DIST%" goto NEED_BUILD
start "" "%DIST%"
goto MAIN_MENU