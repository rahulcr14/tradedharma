# TradeDharma

**Trade. Reflect. Improve.**

TradeDharma is an offline-first personal trade journal and performance analytics app for Android. It records trades, calculates P&L, stores notes/screenshots, and provides review/analytics tools. It does **not** execute trades, connect to brokers, provide trading signals, or provide financial advice.

## Final feature set

- Options, Futures, Equity and Other trade records
- NIFTY, BANK NIFTY and SENSEX default symbols
- NSE and BSE default exchanges
- Searchable symbol/exchange/lot-size selectors
- Custom symbols, exchanges and lot sizes persisted locally
- Protected default catalog entries; custom entries can be deleted without deleting historical trades
- Options strike + expiry calendar
- Lot size × lots quantity calculation
- Editable Entry, Exit, Stop Loss, Target and Charges fields
- Buy/Sell and Call/Put controls
- Live gross/net P&L preview
- Trade history, details, edit and delete
- Daily and weekly review
- Analytics including gross P&L, net P&L and profit factor
- CSV and JSON import/export
- Screenshot attachments
- Light, neutral Dark and pure-black AMOLED Black modes
- Floating modern navigation dock: active destination expands to icon + label; inactive destinations remain icon-only
- Offline-first Room database with non-destructive catalog migration

## Known P&L regression

NIFTY 24000 PE, Buy 65, Entry 109.55, Exit 151.40, Charges 76.47:

- Gross P&L: ₹2,720.25
- Net P&L: ₹2,643.78

## Build

Requirements:
- JDK 21
- Android SDK 37
- Android Studio with Android Gradle Plugin 9.4.0 support

The repository includes a small Gradle bootstrap script (`gradlew`) that downloads Gradle 9.6.0 if a local Gradle installation is not available.

```bash
./gradlew test
./gradlew assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

Pushing to `main` runs unit tests and builds the debug APK. The APK is uploaded as the `TradeDharma-debug-apk` workflow artifact.

## Privacy / scope

TradeDharma stores journal data locally until the user explicitly exports it. It is a recording and analytics tool, not an investment-advice or trade-execution product.
