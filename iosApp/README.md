# LedxCalc iOS

## Requisitos
- Mac con Xcode 15+
- Cuenta Apple Developer (para dispositivo físico / App Store)

## Compilar

1. Generar el framework Kotlin desde la raíz del proyecto:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

2. Abrir `iosApp/iosApp.xcodeproj` en Xcode (crear el proyecto si aún no existe apuntando a `LedxCalcApp.swift`).

3. En Build Settings del target iOS, agregar la ruta del framework `Shared.framework` generado en `shared/build/xcode-frameworks/`.

4. Seleccionar simulador o dispositivo y ejecutar (⌘R).

## Notas
- El video de fondo en iOS usa un gradiente animado (el MP4 permanece en Android).
- Compartir PDF en iOS exporta un resumen de texto vía Share Sheet; el PDF completo con boceto está en Android.
- Bundle ID recomendado: `com.eliezercruz.ledxcalc`
