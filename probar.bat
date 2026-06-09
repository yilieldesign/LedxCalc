@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set "APP_ID=com.eliezercruz.ledxcalc"
set "MAIN_ACTIVITY=%APP_ID%/.MainActivity"
set "APK=app\build\outputs\apk\debug\app-debug.apk"
set "GRADLEW=%~dp0gradlew.bat"

if defined ANDROID_SDK_ROOT set "SDK=%ANDROID_SDK_ROOT%"
if defined ANDROID_HOME set "SDK=%ANDROID_HOME%"
if not defined SDK set "SDK=%LOCALAPPDATA%\Android\Sdk"
set "ADB=%SDK%\platform-tools\adb.exe"

goto MAIN_MENU

REM ============================================================
REM  Menu principal
REM ============================================================
:MAIN_MENU
cls
echo.
echo  ========================================
echo   LedxCalc - Probar app (Android)
echo  ========================================
echo   Carpeta: %CD%
echo   SDK:     %SDK%
echo.
if exist "%ADB%" (
    set "DEVICE_OK=0"
    for /f "tokens=1,2" %%A in ('"%ADB%" devices 2^>nul ^| findstr /r "device$"') do (
        set "DEVICE_OK=1"
        echo   Dispositivo: %%A  [conectado]
    )
    if "!DEVICE_OK!"=="0" echo   Dispositivo: ninguno ^(USB o emulador^)
) else (
    echo   ADB: no encontrado
)
echo.
if exist "%APK%" (echo   APK: listo) else (echo   APK: aun no compilado)
echo.
echo   [1] Compilar + instalar + abrir app
echo   [2] Solo compilar APK
echo   [3] Solo instalar APK ^(ya compilado^)
echo   [4] Abrir carpeta del APK
echo   [5] Ejecutar tests unitarios
echo   [6] Ver dispositivos ADB
echo   [0] Salir
echo.
set "OPCION="
set /p OPCION=  Elige opcion: 

if "%OPCION%"=="1" goto DO_FULL
if "%OPCION%"=="2" goto DO_BUILD
if "%OPCION%"=="3" goto DO_INSTALL
if "%OPCION%"=="4" goto DO_OPEN_APK
if "%OPCION%"=="5" goto DO_TESTS
if "%OPCION%"=="6" goto DO_DEVICES
if "%OPCION%"=="0" goto DO_EXIT
goto MAIN_MENU

REM ============================================================
REM  Acciones
REM ============================================================
:DO_FULL
call :SUB_BUILD
if errorlevel 1 goto DO_FAIL
call :SUB_INSTALL
if errorlevel 1 goto DO_FAIL
call :SUB_LAUNCH
goto DO_OK

:DO_BUILD
call :SUB_BUILD
if errorlevel 1 goto DO_FAIL
echo.
echo  *** BUILD SUCCESSFUL ***
echo  APK: %CD%\%APK%
echo  Sin telefono: usa opcion 4 para abrir la carpeta y copiar el APK.
goto DO_OK

:DO_INSTALL
call :SUB_INSTALL
if errorlevel 1 goto DO_FAIL
call :SUB_LAUNCH
goto DO_OK

:DO_OPEN_APK
if not exist "%APK%" (
    echo.
    echo  APK no encontrado. Usa opcion 2 para compilar primero.
    goto DO_OK
)
explorer /select,"%CD%\%APK%"
goto MAIN_MENU

:DO_TESTS
echo.
echo  Ejecutando tests...
call "%GRADLEW%" :shared:testDebugUnitTest --no-daemon
if errorlevel 1 goto DO_FAIL
echo.
echo  Reporte: shared\build\reports\tests\testDebugUnitTest\index.html
set "ABRIR="
set /p ABRIR=  Abrir reporte en navegador? [S/N]: 
if /i "!ABRIR!"=="S" start "" "%CD%\shared\build\reports\tests\testDebugUnitTest\index.html"
goto DO_OK

:DO_DEVICES
echo.
if not exist "%ADB%" (
    echo  adb no encontrado en: %ADB%
    goto DO_OK
)
"%ADB%" devices -l
goto DO_OK

:DO_OK
echo.
pause
goto MAIN_MENU

:DO_FAIL
echo.
echo  *** Hubo un error. Revisa los mensajes arriba. ***
pause
goto MAIN_MENU

:DO_EXIT
endlocal
exit /b 0

REM ============================================================
REM  Subrutinas (terminan con goto :eof)
REM ============================================================
:SUB_BUILD
echo.
echo  Compilando debug APK...
call "%GRADLEW%" :app:assembleDebug --no-daemon
if errorlevel 1 exit /b 1
exit /b 0

:SUB_INSTALL
if not exist "%APK%" (
    echo.
    echo  ERROR: No existe %APK%
    echo  Compila primero con opcion 2.
    exit /b 1
)
if not exist "%ADB%" (
    echo.
    echo  ERROR: adb no encontrado en %ADB%
    echo  Copia el APK manualmente: %CD%\%APK%
    exit /b 1
)
set "DEVICE_OK=0"
for /f "tokens=1" %%D in ('"%ADB%" devices 2^>nul ^| findstr /r "device$"') do set "DEVICE_OK=1"
if "!DEVICE_OK!"=="0" (
    echo.
    echo  ERROR: No hay dispositivo Android conectado.
    echo  - Conecta telefono con Depuracion USB, o
    echo  - Inicia emulador en Android Studio, o
    echo  - Opcion 2 + 4: compila y copia el APK al telefono.
    exit /b 1
)
echo.
echo  Instalando en dispositivo...
call "%GRADLEW%" :app:installDebug --no-daemon
if errorlevel 1 exit /b 1
exit /b 0

:SUB_LAUNCH
echo.
echo  Abriendo LedxCalc...
"%ADB%" shell am start -n %MAIN_ACTIVITY% >nul 2>&1
if errorlevel 1 (
    echo  Instalado. Abre la app manualmente en el dispositivo.
) else (
    echo  App iniciada.
)
exit /b 0
