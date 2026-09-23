# Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) Architecture
## Vedic Cosmic Ghadi: Android • iOS • Web (Wasm)

This document describes how this codebase is designed to run seamlessly across **Android**, **iOS**, and **Web (WebAssembly)** using 100% shared Kotlin logic and Compose Multiplatform UI.

---

## 1. Codebase Sharing Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       Compose Multiplatform UI                         │
│   (Screens: Dashboard, Panchang, Muhurta, Kundali, Vastu, Dincharya)    │
├─────────────────────────────────────────────────────────────────────────┤
│                   Pure Kotlin Astrological Engines                      │
│ (VedicEphemerisEngine, KundaliMatrix, DincharyaEngine, NumerologyEngine)│
├─────────────────────────────────────────────────────────────────────────┤
│                     Shared State & ViewModels                          │
│     (Kotlin Coroutines, StateFlow, Multiplatform Room / DataStore)      │
├──────────────────────┬──────────────────────┬───────────────────────────┤
│     androidMain      │       iosMain        │        wasmJsMain         │
│  • SensorManager     │  • CoreLocation      │  • DeviceOrientation API  │
│  • AlarmManager      │  • EventKit Calendar │  • Web iCal / Web Share   │
│  • WearOS Complic.   │  • Apple StoreKit    │  • CanvasKit Rendering    │
│  • Play Billing      │  • UNNotification    │  • Browser LocalStorage   │
└──────────────────────┴──────────────────────┴───────────────────────────┘
```

---

## 2. Shared Multiplatform Abstraction Layer (`com.example.core.platform`)

| Interface | Android Implementation | iOS Implementation | Web (Wasm) Implementation |
| :--- | :--- | :--- | :--- |
| **`ICompassSensorProvider`** | Android `SensorManager` (Rotation Vector) | iOS `CLLocationManager.startUpdatingHeading()` | Web `window.addEventListener('deviceorientation')` |
| **`ICalendarReminderProvider`** | Android `AlarmClock` & `CalendarContract` | iOS `EKEventStore` (Apple Calendar) | Web iCalendar (`.ics`) file generation & Browser Notifications |
| **`IAudioHapticsProvider`** | Android `VibratorManager` & `VibrationEffect` | iOS `UIImpactFeedbackGenerator` & `AudioServices` | Web `navigator.vibrate()` & Web Audio API (432Hz sine synthesis) |
| **`INativeShareProvider`** | Android `Intent.ACTION_SEND` | iOS `UIActivityViewController` | Web `navigator.share()` & Clipboard API |

---

## 3. How to Run Each Platform

### A. Android (Mobile & Wear OS)
Run via standard Gradle:
```bash
gradle :app:assembleDebug
```

### B. iOS (iPhone, iPad)
The shared module exports an **XCFramework** consumed by Xcode:
1. In `composeApp/build.gradle.kts`, the iOS targets (`iosArm64`, `iosSimulatorArm64`, `iosX64`) export `composeApp.xcframework`.
2. In Xcode (`iosApp.xcodeproj`), the main view controller simply renders:
```swift
import SwiftUI
import composeApp

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### C. Web (Kotlin / WebAssembly - Wasm)
Compose Multiplatform compiles Kotlin to high-performance WebAssembly rendered on HTML5 Canvas using Skiko/CanvasKit at 60/120 FPS:
```bash
gradle :composeApp:wasmJsBrowserDevelopmentRun
```
The output produces an `index.html` + `composeApp.wasm` distribution package that can be hosted on Cloud Run, Firebase Hosting, Netlify, or AWS S3.

---

## 4. Astrological Calculation Accuracy Across All Platforms
Because all core calculations (planetary longitudes, Choghadiya, Horas, Tithi, Nakshatras, Lagna Ascendant, Vastu quadrant degrees, and Dincharya circadian schedules) use pure mathematics and standard astronomical trigonometry, the calculation results are 100% bit-exact across Android ART, iOS Native ARM64, and WebAssembly.
