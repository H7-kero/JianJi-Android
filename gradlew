#!/bin/sh

#
# Gradle wrapper script
# This script will automatically download gradle-wrapper.jar if needed
#

APP_HOME=$( cd "$( dirname "$0" )" && pwd )
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# Download gradle-wrapper.jar if it doesn't exist
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper JAR..."
    mkdir -p "$APP_HOME/gradle/wrapper"

    # Try to download from GitHub
    if command -v curl > /dev/null 2>&1; then
        curl -L --connect-timeout 10 --max-time 60 \
            -o "$WRAPPER_JAR" \
            "https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null
    elif command -v wget > /dev/null 2>&1; then
        wget --timeout=60 -O "$WRAPPER_JAR" \
            "https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null
    fi

    # Check if download succeeded
    if [ ! -f "$WRAPPER_JAR" ] || [ ! -s "$WRAPPER_JAR" ]; then
        echo ""
        echo "⚠️  Failed to download Gradle wrapper JAR automatically."
        echo ""
        echo "Please use one of the following methods:"
        echo ""
        echo "1. Android Studio (Recommended):"
        echo "   - Open this project in Android Studio"
        echo "   - Android Studio will automatically download the wrapper"
        echo ""
        echo "2. Command line:"
        echo "   - Install Gradle: brew install gradle (macOS) or apt install gradle (Linux)"
        echo "   - Run: gradle wrapper"
        echo ""
        exit 1
    fi

    echo "✓ Gradle wrapper downloaded successfully"
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Run Gradle
exec "$JAVACMD" -Xmx2048m -Dfile.encoding=UTF-8 -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
