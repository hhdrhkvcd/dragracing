# StreetDrag 3D Offline

StreetDrag is an offline-first drag-racing game built with LibGDX for Android and desktop. It is an independent project with its own gameplay code and supplied GLB vehicle pack.

## Gameplay

- Quick drag races against a local AI rival.
- 402 m and 804 m race distances.
- Eight-event offline career with sequential event unlocks and increasing rival difficulty.
- Garage with 16 selectable catalogue entries.
- Four camera modes: chase, hood, side and wide.
- Manual or automatic transmission.
- Clutch-based manual shifting, revving during the start countdown and nitro.
- Race time tracking, best times for both distances, cash rewards and local career progress.
- Offline chassis dyno with animated RPM pull, horsepower and torque readings.

## Tuning and upgrades

Eight upgrade categories are saved separately for each car:
1. Engine
2. Turbo
3. Gearbox
4. Tyres
5. Intake
6. Exhaust
7. Clutch
8. Weight reduction

The gearbox screen also allows individual adjustment of all six gear ratios and the final-drive ratio. Those values are applied to the race physics. Weight reduction affects the simulated vehicle mass; engine, turbo, intake and exhaust upgrades affect output; gearbox and clutch upgrades affect shift behavior; tyre upgrades affect traction.

## Offline behavior

- Android manifest removes INTERNET and ACCESS_NETWORK_STATE permissions from the merged app.
- Current game source contains no online login, matchmaking, remote profile or server API.
- Opponents are local AI.
- Money, car selection, upgrades, gearbox settings, transmission mode, career progress and best times are stored locally using LibGDX Preferences.

## Vehicle assets

All 12 supplied GLB files are included under assets/models/cars. The catalogue contains 16 vehicle entries; some entries intentionally share a GLB because the supplied pack has fewer unique meshes than catalogue entries. The additional luxury-suv.glb and retro-city-car.glb are also mapped.

Keep the original attribution and license notes in vehicle_source when redistributing model assets.

## Controls

### Android
- Tap the on-screen pedals, clutch, shift and nitro controls during a race.
- Tap the transmission-mode label to switch between automatic and manual.
- Tap the camera label to cycle through camera views.
- Main menu buttons open Quick Race, Garage, Tuning, Dyno and Career.
- The main-menu distance panel switches between 402 m and 804 m.

### Desktop
- SPACE: throttle
- DOWN: brake
- SHIFT: clutch
- UP: shift up (manual)
- Z: shift down (manual)
- N: nitro
- M: toggle automatic/manual transmission
- C: change camera
- R: restart a race
- ESC: return to menu/career

In the garage, use LEFT / RIGHT to change cars, T for tuning and ENTER for a quick race. In the main menu, D switches race distance and K opens career. In the tuning screen, 1-8 buys the corresponding upgrade and D opens the dyno. In the dyno, SPACE / ENTER runs a pull and R resets the reading.

## Build Android APK

Requirements: JDK 17 and Android SDK with API 36.

Create a local-only local.properties in the repository root:

~~~properties
sdk.dir=D:/sdv
~~~

Build with Windows PowerShell:

~~~powershell
.\gradlew.bat :core:clean :core:compileJava :android:assembleDebug
~~~

APK output:

android/build/outputs/apk/debug/android-debug.apk

A GitHub Actions workflow also builds the debug APK on pushes to main and uploads it as the StreetDrag-3D-Offline-debug artifact.

## Project modules

- core: game logic, screens, physics and rendering.
- android: Android launcher and packaging.
- lwjgl3: desktop launcher.
- assets: game art and 3D models.
