@echo off
setlocal
set "GRADLE_VERSION=9.6.0"
if defined GRADLE_HOME_OVERRIDE if exist "%GRADLE_HOME_OVERRIDE%\bin\gradle.bat" (
  call "%GRADLE_HOME_OVERRIDE%\bin\gradle.bat" %*
  exit /b %ERRORLEVEL%
)
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  call gradle %*
  exit /b %ERRORLEVEL%
)
set "CACHE_ROOT=%USERPROFILE%\.gradle\tradedharma-distributions"
set "INSTALL_DIR=%CACHE_ROOT%\gradle-%GRADLE_VERSION%"
if not exist "%INSTALL_DIR%\bin\gradle.bat" (
  set "ARCHIVE=%CACHE_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
  if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
  echo Gradle %GRADLE_VERSION% is not installed. Downloading it once...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ARCHIVE%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ARCHIVE%' '%CACHE_ROOT%'"
)
call "%INSTALL_DIR%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
