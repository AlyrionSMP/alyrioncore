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
# Find the mods folder by locating where an alyrioncore jar is already
# installed, so instance renames don't break this script.
# Override with MODS_DIR=/path/to/mods if no jar is installed yet.
if [ -z "${MODS_DIR:-}" ]; then
    INSTALLED_JAR="$(ls -t "$HOME"/.minecraftx/instances/*/mods/alyrioncore-*.jar 2>/dev/null | head -1 || true)"
    if [ -n "$INSTALLED_JAR" ]; then
        MODS_DIR="$(dirname "$INSTALLED_JAR")"
    else
        echo "ERROR: no installed alyrioncore-*.jar found under $HOME/.minecraftx/instances/" >&2
        echo "       set MODS_DIR=<mods folder> and re-run" >&2
        exit 1
    fi
fi
mkdir -p "$MODS_DIR"
echo "==> Installing $JAR -> $MODS_DIR/"
# Atomic replace: copy next to the target, then mv. A running Minecraft keeps
# reading its old (consistent) inode; cp-in-place would corrupt it mid-session.
TMP="$MODS_DIR/.alyrioncore-install.$$"
cp "$JAR" "$TMP" && mv -f "$TMP" "$MODS_DIR/$(basename "$JAR")"
echo "==> Done: $(basename "$JAR")"
