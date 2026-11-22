# StreetDrag

StreetDrag is an offline-first LibGDX drag-racing game for Android and desktop.

## Current gameplay

- 402 m drag races against a local AI rival.
- Local garage with 16 selectable catalogue entries.
- Performance upgrades for engine, turbo, gearbox and tires.
- Local money, vehicle selection and upgrade progress using LibGDX Preferences.
- Four race camera modes: chase, hood, side and wide.
- Touch controls on Android and keyboard controls on desktop.
- Procedural night track and garage environments.
- PBR rendering through gdx-gltf, image-based lighting and dynamic directional shadows.
- Imported GLB vehicle assets stored in `assets/models/cars`.

## Offline behavior

The Android manifest removes INTERNET and ACCESS_NETWORK_STATE permissions from the merged app manifest. No network login, matchmaking, remote profile or server API is used by the current game code. The race opponent is local AI and progress is stored on the device.

## Vehicle assets

All 12 supplied GLB files are included under `assets/models/cars`. The catalogue contains 16 vehicle entries; some entries intentionally share a GLB because the supplied pack has fewer unique meshes than catalogue entries. Two entries expose the remaining `luxury-suv.glb` and `retro-city-car.glb` assets.

The GLB files and their original attribution/license notes are also retained in `vehicle_source`. Keep those notices with the project when redistributing the game.

## Build Android APK

1. Install JDK 17 (or the JDK supported by the installed Android Gradle Plugin) and Android SDK.
2. Create a local-only `local.properties` in the repository root, for example:

   ```properties
   sdk.dir=D:/Android/Sdk
   ```

3. Build:

   ```powershell
   .\gradlew.bat :core:clean :core:compileJava :android:assembleDebug
   ```

4. APK output:

   `android/build/outputs/apk/debug/android-debug.apk`

The Android launcher requests OpenGL ES 3.0.

## Project modules

- `core`: game logic, screens, physics and rendering.
- `android`: Android launcher and packaging.
- `lwjgl3`: desktop launcher.
- `assets`: game art and 3D models.
