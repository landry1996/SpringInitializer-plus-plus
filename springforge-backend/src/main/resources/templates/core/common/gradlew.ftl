#!/bin/sh
# Gradle Wrapper script
set -e
APP_NAME="Gradle"
GRADLE_VERSION="8.10"
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p gradle/wrapper
  DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  echo "Downloading Gradle Wrapper..."
  if command -v curl > /dev/null 2>&1; then
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" -o "$WRAPPER_JAR"
  elif command -v wget > /dev/null 2>&1; then
    wget -q "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" -O "$WRAPPER_JAR"
  fi
  cat > "$WRAPPER_PROPERTIES" << EOF2
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF2
fi

exec java $JAVA_OPTS -jar "$WRAPPER_JAR" "$@"
