#!/usr/bin/env sh
set -eu

GRADLE_VERSION="9.6.0"
GRADLE_HOME_OVERRIDE="${GRADLE_HOME_OVERRIDE:-}"
CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/tradedharma-distributions"
INSTALL_DIR="$CACHE_ROOT/gradle-$GRADLE_VERSION"

if [ -n "$GRADLE_HOME_OVERRIDE" ] && [ -x "$GRADLE_HOME_OVERRIDE/bin/gradle" ]; then
  exec "$GRADLE_HOME_OVERRIDE/bin/gradle" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
  mkdir -p "$CACHE_ROOT"
  ARCHIVE="$CACHE_ROOT/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  echo "Gradle $GRADLE_VERSION is not installed. Downloading it once to $CACHE_ROOT..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 -o "$ARCHIVE" "$URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ARCHIVE" "$URL"
  else
    echo "ERROR: curl or wget is required to bootstrap Gradle." >&2
    exit 1
  fi
  rm -rf "$INSTALL_DIR" "$CACHE_ROOT/gradle-$GRADLE_VERSION"
  unzip -q "$ARCHIVE" -d "$CACHE_ROOT"
  if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
    echo "ERROR: Gradle distribution was downloaded but could not be located." >&2
    exit 1
  fi
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
