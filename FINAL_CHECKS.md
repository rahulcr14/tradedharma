# Final source-package checks

This repository is prepared for the TradeDharma v0.1.0 public release.

## Verified

- `./gradlew test` passes locally.
- `./gradlew assembleDebug` produces `app/build/outputs/apk/debug/app-debug.apk`.
- The generated APK application ID is `com.tradedharma.app`.
- The NIFTY P&L regression remains gross INR 2,720.25 and net INR 2,643.78.
- No `fallbackToDestructiveMigration()` usage exists.
- The existing Room 2-to-3 migration remains non-destructive.
- No duplicate test class or legacy `com.tradelog.app` reference remains.
- GitHub Actions runs tests and builds the debug APK on pushes to `main` and pull requests.
- Version tags such as `v0.1.0` build and attach `TradeDharma.apk` to a GitHub Release.
- Build outputs, local configuration, signing files and secrets are ignored by Git.

## Release notes

- Current application version: `0.1.0` (`versionCode = 1`).
- Recommended first tag: `v0.1.0`.
- A `LICENSE` file is still intentionally absent pending maintainer approval; MIT is a reasonable candidate.
- Physical-device UI validation is not part of the automated unit/build checks and should be completed before publishing.
