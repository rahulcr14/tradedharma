# TradeDharma

<p align="center">
  <img src="docs/images/tradedharma-logo.png" alt="TradeDharma logo" width="220">
</p>

<p align="center"><strong>Trade. Reflect. Improve.</strong></p>

TradeDharma is an offline-first Android trade journal and performance analytics
app designed to help traders record, review and understand their own trading
performance.

It supports manual trade journaling, P&L calculation, trade history, notes,
screenshots, reviews, analytics, CSV/JSON import and export, and offline-first
local storage.

<p>
  <a href="https://github.com/rahulcr14/tradedharma/releases/latest/download/TradeDharma.apk">
    <img src="https://img.shields.io/badge/Download-APK-2196F3?style=for-the-badge&logo=android&logoColor=white" alt="Download TradeDharma APK">
  </a>
</p>

**Android APK. Download and install the latest release directly on your Android device.**

[View all releases](https://github.com/rahulcr14/tradedharma/releases)

## Features

- Offline-first trade journal backed by a local Room database
- Options, Futures, Equity and Other trade records
- NIFTY, BANK NIFTY and SENSEX default symbols
- NSE and BSE default exchanges
- Searchable symbol, exchange and lot-size selectors
- Custom symbols, exchanges and lot sizes
- Options strike and expiry calendar
- Lot size x lots quantity calculation
- Entry, Exit, Stop Loss, Target and Charges fields
- Buy/Sell and Call/Put controls
- Live gross and net P&L preview
- Trade history with edit and delete
- Daily and weekly reviews
- Performance analytics including gross P&L, net P&L and profit factor
- CSV import/export and JSON backup/restore
- Screenshot attachments stored locally
- Light, Dark and AMOLED Black modes
- Floating navigation dock with animated active destinations

## P&L regression

This regression example is used to validate the P&L calculation:

| Input | Value |
| --- | ---: |
| Instrument | NIFTY 24000 PE |
| Direction | Buy |
| Quantity | 65 |
| Entry | 109.55 |
| Exit | 151.40 |
| Charges | INR 76.47 |
| Gross P&L | INR 2,720.25 |
| Net P&L | INR 2,643.78 |

## Tech stack

- Kotlin
- Jetpack Compose and Material 3
- Android Navigation Compose
- Room and Room KSP compiler
- Kotlin coroutines
- Gradle 9.7.0
- Android Gradle Plugin 9.4.0
- Android SDK 37

## Requirements

- JDK 21
- Android SDK Platform 37 and Android build tools
- Android Studio with support for Android Gradle Plugin 9.4.0

The repository wrapper bootstraps Gradle 9.7.0 when a system Gradle installation
is not available.

## Build from source

```bash
git clone https://github.com/rahulcr14/tradedharma.git
cd tradedharma

./gradlew test
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Testing and CI

Run the unit tests with:

```bash
./gradlew test
```

Build the debug APK with:

```bash
./gradlew assembleDebug
```

Pushes to `main` and pull requests run tests and build the debug APK in GitHub
Actions. CI may upload the APK as a development artifact. Public downloads are
published separately through GitHub Releases.

Pushing a version tag such as `v0.1.0` runs the release workflow, tests the
project, builds the APK, renames it to `TradeDharma.apk`, and attaches it to a
GitHub Release.

## Project structure

```text
tradedharma/
├── app/
│   ├── schemas/
│   └── src/
├── docs/
│   └── images/
├── .github/
│   └── workflows/
├── README.md
├── CHANGELOG.md
├── FINAL_CHECKS.md
├── docs.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

## Privacy

TradeDharma is offline-first and stores journal data locally unless the user
explicitly exports it. The project does not require a broker connection or
cloud account for its journal workflows.

## Disclaimer

TradeDharma is a personal trade-recording and performance analytics tool. It
does not provide investment, trading, or financial advice and does not execute
trades.

## License

No license file has been added yet. Until a license is selected and committed,
the repository should not be assumed to grant reuse rights. MIT is a reasonable
candidate for this project and should be added after maintainer approval.
