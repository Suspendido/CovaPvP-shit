@echo off
echo 🔧 Configurando Gradle Wrapper...
echo.

if not exist "gradlew" (
    echo ❌ gradlew no encontrado
    goto :end
)

if not exist "gradlew.bat" (
    echo ❌ gradlew.bat no encontrado
    goto :end
)

if not exist "gradle\wrapper\gradle-wrapper.properties" (
    echo ❌ gradle-wrapper.properties no encontrado
    goto :end
)

echo ✅ Archivos de wrapper encontrados
echo 🚀 Generando gradle-wrapper.jar...

REM Ejecutar gradle wrapper para generar el JAR
gradle wrapper --gradle-version 8.10

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ✅ gradle-wrapper.jar generado exitosamente
    echo 🎉 Wrapper configurado correctamente
    echo.
    echo 📋 Puedes usar:
    echo   - gradlew.bat build (Windows)
    echo   - ./gradlew build (Linux/Mac)
) else (
    echo ❌ Error generando gradle-wrapper.jar
    echo 💡 Asegúrate de tener Gradle instalado globalmente
)

:end
echo.
pause
