@echo off
setlocal
cd /d "%~dp0.."

if exist "ledxcalc-upload.jks" (
    echo Ya existe ledxcalc-upload.jks en la raiz del proyecto.
    echo Si quieres crear uno nuevo, renombra o mueve el archivo actual primero.
    pause
    exit /b 1
)

echo Creando llave de subida para Google Play...
echo.
echo Te pedira: contrasena del keystore, nombre, organizacion, etc.
echo Guarda las contrasenas en un lugar seguro.
echo.

keytool -genkeypair -v -keystore ledxcalc-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload

if errorlevel 1 (
    echo Error al crear el keystore. Verifica que Java/keytool este instalado.
    pause
    exit /b 1
)

if not exist "keystore.properties" (
    copy /Y keystore.properties.example keystore.properties >nul
    echo.
    echo Se creo keystore.properties desde el ejemplo.
    echo Editalo y pon las contrasenas que acabas de definir.
)

echo.
echo Listo: ledxcalc-upload.jks
echo Siguiente paso: edita keystore.properties y ejecuta scripts\bundle-release.bat
pause
