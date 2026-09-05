@echo off
setlocal
cd /d "%~dp0.."

if not exist "keystore.properties" (
    echo Falta keystore.properties
    echo.
    echo 1^) Ejecuta scripts\generate-upload-keystore.bat
    echo 2^) Copia keystore.properties.example a keystore.properties
    echo 3^) Completa las contrasenas en keystore.properties
    pause
    exit /b 1
)

echo Compilando AAB firmado para Google Play...
call gradlew.bat :app:bundleRelease --no-daemon
if errorlevel 1 (
    echo Error en la compilacion.
    pause
    exit /b 1
)

set "AAB=app\build\outputs\bundle\release\app-release.aab"
if not exist "%AAB%" (
    echo No se encontro el AAB en %AAB%
    pause
    exit /b 1
)

echo.
echo AAB listo para subir a Google Play:
echo   %CD%\%AAB%
echo.
echo En Play Console: Produccion o Prueba interna -^> Crear version -^> Subir
pause
