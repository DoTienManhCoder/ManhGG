#!/bin/sh
set -eu

checksum_sources() {
  find src/main pom.xml -type f \
    \( -name "*.java" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.xml" \) \
    -exec stat -c "%Y %n" {} \; | sort | sha256sum
}

mvn -q -DskipTests compile

(
  last_checksum="$(checksum_sources)"

  while true; do
    current_checksum="$(checksum_sources)"

    if [ "$current_checksum" != "$last_checksum" ]; then
      echo "Backend source changed. Compiling..."
      mvn -q -DskipTests compile || true
      last_checksum="$current_checksum"
    fi

    sleep 1
  done
) &

exec mvn spring-boot:run
