@echo off
setlocal
cd /d "%~dp0.."
call gradlew.bat :webApp:wasmJsBrowserDistribution --no-daemon
if errorlevel 1 exit /b 1
set "DIST=webApp\build\dist\wasmJs\productionExecutable"
if exist "%DIST%\.vercel" rmdir /s /q "%DIST%\.vercel"
xcopy /E /I /Y ".vercel" "%DIST%\.vercel" >nul
cd /d "%DIST%"
call npx vercel deploy . --prod --yes
echo.
echo Listo: https://ledx-calc.vercel.app/
echo iPhone: Safari -^> Compartir -^> Anadir a pantalla de inicio
pause
