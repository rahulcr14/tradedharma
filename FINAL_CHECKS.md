# Final source-package checks

This source package was rebuilt around the final requested UX and feature set.

## Static checks completed

- XML resources parse successfully.
- Kotlin source has balanced braces.
- Kotlin compiler frontend reports no syntax-like errors; Android/Compose dependency resolution is intentionally not available in this packaging environment.
- No duplicate test class remains.
- No `com.tradelog.app` references remain.
- No MCX or NCDEX exchange defaults remain.
- No `fallbackToDestructiveMigration()` remains.
- No trailing whitespace was found in source/config/docs files.
- Known NIFTY P&L calculation independently rechecked: gross ₹2,720.25; net ₹2,643.78.
- GitHub Actions is configured to install Android SDK 37, run unit tests, assemble the debug APK, and upload the APK artifact.

## Runtime/build limitation

A full Android Gradle build and physical-device UI test could not be executed in this packaging environment because the Android Gradle toolchain/distribution is not locally available here and no Android device/emulator is attached. The repository therefore deliberately does not claim a runtime PASS from this environment.

After uploading to GitHub, the included workflow performs the real Gradle test/build on GitHub Actions. The first local build can also be run with `./gradlew test` and `./gradlew assembleDebug`; the included bootstrap script downloads Gradle 9.6.0 when needed.
