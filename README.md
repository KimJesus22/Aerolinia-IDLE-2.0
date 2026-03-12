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
- **Personaje Capitán** animado con gorra, corbata, uniforme y zapatos
- **8 estaciones** interactivas (Check-In, Nacional, Cafetería, Internacional, Sala VIP, Duty Free, Hangar, Espacial)
- **Ganancias idle** automáticas cada 100ms
- **Bonos recolectables** cada 8 segundos
- **4 mejoras globales**: Combustible Premium, Sala VIP, Entrenamiento Pilotos, Marketing Global
- **Ganancias offline** al 50% de eficiencia
- **Sistema de prestigio/rebirth** con tokens permanentes (+25% cada uno)
- **Aviones animados** volando sobre la pista de aterrizaje
- **Autoguardado** cada 30 segundos y en onPause

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
├── MainActivity.kt
├── AirlineApplication.kt
├── Graph.kt (Inyección de dependencias)
├── ui/
│   ├── theme/ (Color.kt, Theme.kt, Type.kt)
│   ├── screens/game/ (GameScreen.kt, GameViewModel.kt)
│   └── components/ (MapCanvas.kt, HudOverlay.kt, UpgradesSheet.kt,
│                     PrestigeScreen.kt, OfflineEarningsDialog.kt)
├── domain/model/ (Station.kt, Upgrade.kt, CharacterState.kt)
├── data/
│   ├── local/ (GameDatabase.kt, dao/, entity/)
│   ├── preferences/ (GamePreferences.kt)
│   └── repository/ (GameRepository.kt)
└── util/ (NumberFormatter.kt, Constants.kt, SoundManager.kt)
```

## 🎨 Diseño Visual

- Tema oscuro: `#0A1628` → `#142238`
- Acentos: azul cielo, dorado, verde, rojo
- Personaje con gorra dorada, corbata roja, etiqueta "CAPITÁN"
- Estaciones activas con borde azul brillante
- Pista de aterrizaje con aviones animados

## 📝 Notas

- Los sonidos están marcados como `TODO` en `SoundManager.kt`. Agrega archivos `.ogg` en `res/raw/` para activarlos.
- El juego funciona únicamente en orientación **portrait**.
- El ViewModel sobrevive cambios de configuración gracias a `ViewModelProvider`.
