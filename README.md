# CepDefteri

An offline Android expense tracker built with Kotlin, Jetpack Compose, Material 3, and Room. The app uses a Turkish interface and Turkish lira (TRY) formatting.

## Features

- Add an expense with an amount, category, and optional note, using today's date.
- List expenses by date and filter them by category.
- View the current month's total spending and expense count.
- Delete expenses with confirmation.
- Keep expenses locally with Room, including after restarting the app.
- Store money as integer kuruş (1 TRY = 100 kuruş) to avoid floating-point rounding errors.
- Use the app without an account or internet connection. Uninstalling the app deletes its local data.

## Requirements

- Android Studio with Android SDK Platform 35 installed.
- A compatible Gradle JDK. The project was successfully built and tested using JBR 21.0.11; Java/Kotlin compilation targets Java 17.
- An Android device or emulator running Android 8.0 (API 26) or later.
- Internet access for the initial Gradle sync and dependency downloads.

## Getting started

1. Install Android Studio and complete its SDK setup wizard.
2. Select **Open** and choose the `CepDefteri` project directory.
3. In **Settings > Build, Execution, Deployment > Build Tools > Gradle**, select a compatible **Gradle JDK**. Use JBR 21.0.11 if available; the bundled Java 25 runtime did not work with this project's build toolchain during verification.
4. Install Android SDK Platform 35 if prompted and run **Sync Project with Gradle Files**.
5. Start an emulator through **Tools > Device Manager**, or connect an Android device with USB debugging enabled.
6. Select the device and the `app` run configuration, then click **Run**.

Building the project produces an APK; use **Run** to install and launch it on the selected device.

## Build

From the project directory, with `JAVA_HOME` pointing to a compatible JDK and the Android SDK configured in `local.properties` or the environment:

```sh
./gradlew :app:assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Unit tests

Run the local JVM tests without an emulator:

```sh
./gradlew :app:testDebugUnitTest
```

In Android Studio, you can also right-click `ExpenseCalculationsTest.kt` and select **Run 'ExpenseCalculationsTest'**.

The 15 JUnit tests cover:

- Converting whole and decimal amounts to integer kuruş, including comma and dot separators, whitespace, and supported limits.
- Rejecting zero, negative, malformed, ambiguous, and overly precise amounts.
- Monthly totals and counts, including month/year boundaries, empty months, and leap days.
- Category filtering, preserving record order, and empty results.

HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`

These tests cover the calculation and validation logic. Automated UI, ViewModel, and Room integration tests have not been added.

## Project structure

```text
app/src/main/java/com/cepdefteri/app/
├── MainActivity.kt          # Compose screens, expense form, and display formatting
├── ExpenseViewModel.kt     # UI state and asynchronous save/delete operations
├── ExpenseDatabase.kt      # Room entity, DAO, and database
└── ExpenseCalculations.kt  # Amount validation, monthly summary, and filtering

app/src/test/java/com/cepdefteri/app/
└── ExpenseCalculationsTest.kt
```

## Toolchain

| Component | Version |
| --- | --- |
| Gradle Wrapper | 8.11.1 |
| Android Gradle Plugin | 8.9.1 |
| Kotlin / Compose compiler plugin | 2.1.20 |
| Compose BOM | 2025.03.00 |
| Room runtime / KTX / compiler | 2.7.1 |
| JUnit | 4.13.2 |
| Compile / target SDK | 35 |
| Minimum SDK | 26 |

## Metadata build error fix

The original Room 2.6.1 compiler could not read Kotlin 2.1 metadata, causing this build error:

```text
Provided Metadata instance has version 2.1.0, while maximum supported version is 2.0.0.
```

All three Room dependencies (`room-runtime`, `room-ktx`, and `room-compiler`) were updated together to **2.7.1**. This resolved the error, and both the debug APK build and unit tests passed. If an existing checkout still shows the error, confirm these versions in `app/build.gradle.kts` and sync Gradle again.

## Verification

- Debug APK build: **passed**.
- Local unit tests: **15 passed, 0 failures, 0 errors, 0 skipped**.
- The app was launched successfully by the user on their Android setup.
- Full automated UI and database integration verification remains outside the current test coverage.

### Manual checks

1. Add `125,50` and `0,01`; confirm the monthly total is `125,51` TRY when these are the only expenses for that month.
2. Confirm that zero, negative, alphabetic, and three-decimal inputs cannot be saved.
3. Filter by category and check the matching records.
4. Close and reopen the app to check that expenses are retained.
5. Check both **Vazgeç** (Cancel) and **Sil** (Delete) in the deletion dialog.
