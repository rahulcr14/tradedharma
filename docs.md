# TradeDharma v0.1.0 implementation notes

## Implemented

- Offline Room database with trade, review and saved catalog tables.
- Non-destructive Room migration from version 2 to version 3.
- CRUD for trades, including options, futures, equity and other instruments.
- Buy/sell P&L, charges, risk, reward, planned R:R and actual R multiple.
- Add/edit trade form with validation and screenshot attachments.
- Search and instrument filtering.
- Dashboard KPIs, equity curve and win/loss distribution.
- Insights for strategy, instrument, time of day and tags.
- Daily and weekly review forms.
- CSV/JSON export and CSV/JSON import.
- Persisted system, light, dark and AMOLED Black themes.
- Material 3 UI with floating navigation dock and safe system-bar insets.
- Unit tests for analytics and P&L calculations.
- GitHub Actions CI and tag-driven APK release workflow.

## Known constraints

- Historical trade date/time entry is not exposed in the form; new/manual trades use the current timestamp.
- CSV import expects the TradeDharma export column names.
- Screenshot attachments use device-local paths and should be re-attached when moving backups to another device.
- There is no cloud sync, broker API, live pricing, trading signal, AI prediction or automated execution.
- A license has not yet been selected for the repository.

## Release preparation

- Current version: `0.1.0` (`versionCode = 1`).
- Recommended tag: `v0.1.0`.
- Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.
- Public release asset name: `TradeDharma.apk`.
