# TradeDharma V0.1 implementation status

## Implemented

- Offline Room database with trade + review tables
- CRUD for trades
- Equity, futures, options and other instruments
- Buy/sell P&L, charges, risk, reward, planned R:R and actual R multiple
- Add/edit trade form with validation
- Search + instrument filtering
- Dashboard KPIs + equity curve + win/loss distribution
- Insights for strategy, instrument, time of day and tags
- Chart screenshot attachment stored in app-internal storage
- Daily/weekly review forms
- CSV/JSON export and CSV/JSON import
- Dark/light/system themes
- Material 3 UI
- Basic unit tests
- GitHub Actions workflow

## Known V0.1 constraints

- Historical trade date/time entry is not yet exposed in the form; new/manual trades use the current timestamp.
- CSV import expects the TradeDharma export column names.
- Screenshots are device-local paths and should be re-attached when moving backups to another device.
- No cloud sync, broker APIs, live prices, signals or AI.
- APK compilation was not executed in this environment because Android SDK/Gradle artifacts were unavailable offline.

## Recommended next hardening

1. Add explicit historical date + time picker to Add Trade.
2. Persist theme mode with DataStore.
3. Add database migration instead of destructive fallback before public release.
4. Add screenshot export/import packaging for portable backups.
5. Add instrument-aware option/futures lot-value validation.
6. Add UI tests for the main user journey.
