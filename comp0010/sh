#!/bin/bash

JSH_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

JSH_JAR="$JSH_ROOT/target/shell-1.0-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$JSH_JAR" ]; then
    echo COMP0010 shell is not built. Run "'mvn package'" && exit 1
fi

java -jar "$JSH_JAR" "$@"
