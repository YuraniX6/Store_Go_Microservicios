@echo off
REM Script utilities para auth-service (Windows)

setlocal enabledelayedexpansion

:menu
cls
echo.
echo ====== StoreGo Auth Service Utility ======
echo 1. Build (compilar)
echo 2. Test (ejecutar tests)
echo 3. Run (ejecutar localmente)
echo 4. Docker Build
echo 5. Docker Compose Up
echo 6. Docker Compose Down
echo 7. Clean (limpiar build)
echo 8. Reset Database
echo 9. View Swagger
echo 10. Health Check
echo 11. Exit
echo.
set /p choice="Seleccionar opcion: "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto test
if "%choice%"=="3" goto run
if "%choice%"=="4" goto docker_build
if "%choice%"=="5" goto docker_up
if "%choice%"=="6" goto docker_down
if "%choice%"=="7" goto clean
if "%choice%"=="8" goto reset_db
if "%choice%"=="9" goto view_swagger
if "%choice%"=="10" goto health_check
if "%choice%"=="11" goto end
echo Opcion invalida
goto menu

:build
echo Compilando...
call mvn clean install
if %errorlevel% equ 0 (
    echo Build completado!
) else (
    echo Error en build
)
pause
goto menu

:test
echo Ejecutando tests...
call mvn test
if %errorlevel% equ 0 (
    echo Tests completados!
) else (
    echo Error en tests
)
pause
goto menu

:run
echo Iniciando aplicacion...
call mvn spring-boot:run
pause
goto menu

:docker_build
echo Construyendo imagen Docker...
docker build -t auth-service:latest .
if %errorlevel% equ 0 (
    echo Imagen construida!
) else (
    echo Error en build Docker
)
pause
goto menu

:docker_up
echo Iniciando docker-compose...
docker-compose up -d
if %errorlevel% equ 0 (
    echo Docker compose iniciado!
    echo App disponible en: http://localhost:8080
    echo Swagger en: http://localhost:8080/swagger-ui.html
) else (
    echo Error iniciando docker-compose
)
pause
goto menu

:docker_down
echo Deteniendo docker-compose...
docker-compose down
if %errorlevel% equ 0 (
    echo Docker compose detenido!
) else (
    echo Error deteniendo docker-compose
)
pause
goto menu

:clean
echo Limpiando build...
if exist target (
    rmdir /s /q target
)
call mvn clean
echo Limpieza completada!
pause
goto menu

:reset_db
echo Reseteando base de datos...
docker-compose down -v
docker-compose up -d
timeout /t 10 /nobreak
echo BD reseteada!
pause
goto menu

:view_swagger
echo Abriendo Swagger...
start http://localhost:8080/swagger-ui.html
pause
goto menu

:health_check
echo Verificando salud de la aplicacion...
setlocal enabledelayedexpansion

for /f %%i in ('powershell -Command "try { $r = Invoke-WebRequest -Uri http://localhost:8080/swagger-ui.html -UseBasicParsing; Write-Host $r.StatusCode } catch { Write-Host '000' }"') do (
    set RESPONSE=%%i
)

if "!RESPONSE!"=="200" (
    echo [OK] API esta corriendo ^(HTTP !RESPONSE!^)
    
    echo Probando login...
    setlocal enabledelayedexpansion
    for /f %%i in ('powershell -Command "try { $r = Invoke-WebRequest -Uri http://localhost:8080/auth/login -Method POST -Body '{\"username\":\"admin\",\"password\":\"admin123\"}' -ContentType 'application/json' -UseBasicParsing; if ($r.Content -like '*token*') { Write-Host 'OK' } else { Write-Host 'FAIL' } } catch { Write-Host 'ERROR' }"') do (
        set LOGIN=%%i
    )
    
    if "!LOGIN!"=="OK" (
        echo [OK] Login funcionando
    ) else (
        echo [ERROR] Login fallo
    )
) else (
    echo [ERROR] API no esta disponible ^(HTTP !RESPONSE!^)
)
pause
goto menu

:end
endlocal
exit /b
