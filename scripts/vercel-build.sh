#!/usr/bin/env bash
set -euo pipefail

echo "==> LedxCalc: compilando web Wasm para Vercel"

if ! command -v java >/dev/null 2>&1; then
  echo "==> Instalando Java 17 (Temurin)..."
  ARCH="$(uname -m)"
  case "$ARCH" in
    x86_64) ARCH_TAG="x64" ;;
    aarch64|arm64) ARCH_TAG="aarch64" ;;
    *)
      echo "Arquitectura no soportada: $ARCH"
      exit 1
      ;;
  esac
  JDK_DIR="/tmp/jdk-17"
  curl -fsSL \
    "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_${ARCH_TAG}_linux_hotspot_17.0.13_11.tar.gz" \
    | tar -xz -C /tmp
  export JAVA_HOME="/tmp/jdk-17.0.13+11"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

java -version

chmod +x gradlew
./gradlew :webApp:wasmJsBrowserDistribution --no-daemon --stacktrace

DIST="webApp/build/dist/wasmJs/productionExecutable"
echo "==> Archivos generados:"
ls -lah "$DIST"

if ! ls "$DIST"/*.wasm >/dev/null 2>&1; then
  echo "ERROR: no se generaron archivos .wasm"
  exit 1
fi
