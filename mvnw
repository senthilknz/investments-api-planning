#!/bin/sh
# Maven Wrapper script
# Automatically downloads and uses the correct Maven version

set -e

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

# Find project base directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Read distribution URL from properties
if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
else
    echo "Error: $MAVEN_WRAPPER_PROPERTIES not found"
    exit 1
fi

# Maven home directory
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"

# Download Maven if not present
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven..."
    mkdir -p "$MAVEN_HOME"
    MAVEN_ZIP="/tmp/apache-maven-3.9.9-bin.zip"
    curl -sL "$DISTRIBUTION_URL" -o "$MAVEN_ZIP"
    unzip -q "$MAVEN_ZIP" -d "$HOME/.m2/wrapper/dists/"
    MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"
fi

# Find Maven executable
if [ -f "$MAVEN_HOME/bin/mvn" ]; then
    MVN_CMD="$MAVEN_HOME/bin/mvn"
else
    MVN_CMD="$HOME/.m2/wrapper/dists/apache-maven-3.9.9/bin/mvn"
fi

# Execute Maven
exec "$MVN_CMD" "$@"
