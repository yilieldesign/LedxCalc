#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIST="$ROOT/webApp/build/dist/wasmJs/productionExecutable"

echo "==> Compilando web Wasm..."
cd "$ROOT"
chmod +x gradlew
./gradlew :webApp:wasmJsBrowserDistribution --no-daemon --stacktrace

if ! ls "$DIST"/*.wasm >/dev/null 2>&1; then
  echo "ERROR: build sin archivos .wasm"
  exit 1
fi

echo "==> Desplegando a Vercel (solo carpeta dist, ~25 MB)..."
rm -rf "$DIST/.vercel"
cp -r "$ROOT/.vercel" "$DIST/.vercel"
cd "$DIST"
npx vercel deploy . --prod --yes

echo "==> Listo: https://ledx-calc.vercel.app/"
echo "    iPhone: Safari -> Compartir -> Anadir a pantalla de inicio"
