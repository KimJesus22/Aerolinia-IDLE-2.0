# ✈️ Airline Idle Tycoon

Juego idle tycoon de aerolínea para Android, al estilo "My Perfect Hotel" / "Idle Miner Tycoon".

## 🛠️ Stack Tecnológico

| Tecnología | Versión |
|-----------|---------|
| Kotlin | 2.0.21 |
| Jetpack Compose | BOM 2024.11.00 |
| Material 3 | Última estable |
| Room Database | 2.6.1 |
| DataStore Preferences | 1.1.1 |
| Compose Navigation | 2.8.4 |
| Min SDK | 26 (Android 8.0) |
| Target/Compile SDK | 35 |
| Gradle | 8.13 con Kotlin DSL |

## 🎮 Mecánicas del Juego

- **Mapa 2D del aeropuerto** dibujado con Canvas de Compose
- **Personaje Capitán mejorado**: bob al caminar, brazos con swing, manos, cuello, pelo, ojos que siguen la dirección, corbata roja en diamante, gorra con insignia y estrella dorada
- **8 estaciones** interactivas (Check-In, Nacional, Cafetería, Internacional, Sala VIP, Duty Free, Hangar, Espacial)
- **Sistema de tiers visuales**: Nv1-4 gris, Nv5-9 azul, Nv10-19 dorado, Nv20+ brillante animado
- **Estaciones bloqueadas** con candado dorado dibujado en Canvas
- **Ganancias idle** automáticas sincronizadas con `withFrameNanos`
- **Bonos recolectables** cada 8 segundos con barra de progreso y indicador "$" con bob animation
- **4 mejoras globales**: Combustible Premium, Sala VIP, Entrenamiento Pilotos, Marketing Global
- **Ganancias offline** al 50% de eficiencia con popup de bienvenida
- **Sistema de prestigio/rebirth** con tokens permanentes (+25% cada uno)
- **3 aviones volando** con distintas velocidades, escalas (parallax) y sombras en el suelo
- **Avión estacionado** en la pista de aterrizaje
- **Autoguardado** cada 30 segundos y en onPause

## ⚡ Optimización de Rendimiento (60 FPS)

- **Game loop con `withFrameNanos`**: sincronizado con el display, reemplazando `delay(100)`
- **Delta-time based**: movimiento, income, animaciones — todo normalizado a segundos
- **Canvas por capas**: fondo cacheado con `drawWithCache` (solo se recalcula al redimensionar)
- **Colores preallocados**: ~30 colores y strokes como `private val` globales, cero allocations en draw loop
- **StateFlows separados por frecuencia**:
  - `dynamicState` (cada frame): personaje, aviones, bonos, progreso
  - `economyState` (~1/seg): dinero, $/seg
  - `buildingState` (acción del jugador): estaciones, upgrades, prestigio
  - `uiFlags` (interacción): dialogs, sheets
- **`graphicsLayer`** para animaciones sin recomposición del contenido
- **60Hz lock** en `MainActivity` vía `preferredDisplayModeId`
- **`hardwareAccelerated="true"`** en AndroidManifest

## 🎨 Diseño Visual

### Tema
- Tema oscuro Material 3: `#0A1628` → `#142238`
- Acentos: azul cielo, dorado, verde, rojo

### Personaje Capitán
- Gorra azul con insignia dorada y estrella
- Ojos blancos con pupilas que siguen la dirección
- Uniforme azul con botones dorados y corbata roja
- Brazos con swing y bob vertical al caminar

### Estaciones (4 Tiers)
| Tier | Niveles | Gradiente | Borde | Efecto |
|------|---------|-----------|-------|--------|
| Básica | 1-4 | Gris | Gris claro | — |
| Mejorada | 5-9 | Azul | Azul cielo | Emoji grande |
| Premium | 10-19 | Azul oscuro | Dorado + glow | Borde grueso |
| Máxima | 20+ | Dorado | Brillante | Pulso animado |

### Decoraciones Canvas
- Palmeras con tronco café y hojas verdes (quadraticBezier)
- Sillas de espera en filas con respaldo y patas
- Maletas con asa y rueditas
- Banda de equipaje con rodillos animados
- Pista de aterrizaje con textura, luces azules, flechas y avión estacionado

## 📦 Cómo Compilar y Ejecutar

### Requisitos Previos
1. **Android Studio** Ladybug o superior
2. **JDK 11** o superior
3. **Android SDK 35** instalado

### Pasos
1. Clona o abre el proyecto en Android Studio:
   ```
   File → Open → selecciona la carpeta "juegoaerolinea"
   ```
2. Espera a que Gradle sincronice las dependencias
3. Conecta un dispositivo Android o inicia un emulador (API 26+)
4. Ejecuta la app:
   ```
   Run → Run 'app' (Shift + F10)
   ```

### Compilar desde Terminal
```bash
# Debug
./gradlew assembleDebug

# El APK se genera en:
# app/build/outputs/apk/debug/app-debug.apk
```

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/juegoaerolinea/
├── MainActivity.kt              (60Hz lock, onPause save)
├── AirlineApplication.kt
├── GameViewModelFactory.kt
├── Graph.kt                     (Inyección de dependencias)
├── ui/
│   ├── theme/                   (Color.kt, Theme.kt, Type.kt)
│   ├── screens/game/            (GameScreen.kt, GameViewModel.kt)
│   └── components/              (MapCanvas.kt, HudOverlay.kt, UpgradesSheet.kt,
│                                 PrestigeScreen.kt, OfflineEarningsDialog.kt)
├── domain/model/                (Station.kt, Upgrade.kt, CharacterState.kt)
├── data/
│   ├── local/                   (GameDatabase.kt, dao/, entity/)
│   ├── preferences/             (GamePreferences.kt)
│   └── repository/              (GameRepository.kt)
└── util/                        (NumberFormatter.kt, Constants.kt, SoundManager.kt)
```

## 📝 Notas

- Los sonidos están marcados como `TODO` en `SoundManager.kt`. Agrega archivos `.ogg` en `res/raw/` para activarlos.
- El juego funciona únicamente en orientación **portrait** (`screenOrientation="portrait"` en Manifest).
- El ViewModel sobrevive cambios de configuración gracias a `ViewModelProvider`.
- Los `Log.d("GAME_DEBUG")` están activos para depuración. Filtra en Logcat por `GAME_DEBUG`.
- Para limpiar la DB antigua, desinstala la app antes de instalar una versión nueva.
