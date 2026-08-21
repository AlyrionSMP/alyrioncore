#!/usr/bin/env bash
# Build AlyrionCore and install the jar into the Alyrion-indev-8.0.0 instance.
#
# Environment quirks handled here:
#  * No JDK on PATH -> uses the launcher's bundled JDK 21, copied to .tools/
#  * The sandbox blocks posix_spawn -> JVM must use the 'fork' launch
#    mechanism or Gradle cannot start its daemon/workers.
#  * GRADLE_USER_HOME is kept in-workspace (.gradle-home) for repeatable builds.
set -euo pipefail
cd "$(dirname "$0")"

JRE_SRC="${JRE_SRC:-$HOME/.minecraftx/jre/java-runtime-delta}"
TOOLS_JRE="$PWD/.tools/jre"
export GRADLE_USER_HOME="$PWD/.gradle-home"
export JAVA_TOOL_OPTIONS="-Djdk.lang.Process.launchMechanism=fork"

if [ ! -x "$TOOLS_JRE/bin/java" ] || [ ! -x "$TOOLS_JRE/bin/javac" ]; then
    echo "==> Copying bundled JDK into .tools/ (one-time)"
    mkdir -p "$PWD/.tools"
    rm -rf "$TOOLS_JRE"
    cp -r "$JRE_SRC" "$TOOLS_JRE"
    chmod +x "$TOOLS_JRE/bin/"*
fi
export JAVA_HOME="$TOOLS_JRE"

echo "==> Building (this can take a few minutes on the first run)"
"$PWD/gradlew" --console=plain --no-daemon build

JAR="$(ls build/libs/alyrioncore-*.jar | head -1)"
MODS_DIR="${MODS_DIR:-$HOME/.minecraftx/instances/Alyrion/mods}"
if [ ! -d "$MODS_DIR" ] && [ -d "$HOME/.minecraftx/instances/Alyrion-indev-8.0.0/mods" ]; then
    MODS_DIR="$HOME/.minecraftx/instances/Alyrion-indev-8.0.0/mods"
fi
mkdir -p "$MODS_DIR"
echo "==> Installing $JAR -> $MODS_DIR/"
cp "$JAR" "$MODS_DIR/"
echo "==> Done: $(basename "$JAR")"
