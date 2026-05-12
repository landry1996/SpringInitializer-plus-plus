#!/bin/sh
# Maven Wrapper script — downloads and runs the specified Maven version
set -e
MAVEN_VERSION="3.9.9"
WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p .mvn/wrapper
  echo "Downloading Maven Wrapper..."
  if command -v curl > /dev/null 2>&1; then
    curl -sL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  elif command -v wget > /dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
  else
    echo "Error: curl or wget required" >&2
    exit 1
  fi
fi

exec java $MAVEN_OPTS -jar "$WRAPPER_JAR" "$@"
