#!/usr/bin/env bash
set -euo pipefail
SERVICE_DIR="discovery-server"
if [ ! -d "$SERVICE_DIR" ]; then
  echo "Directory $SERVICE_DIR not found"
  exit 1
fi
cd "$SERVICE_DIR"
echo "Building $SERVICE_DIR..."
mvn clean install -DskipTests=true
JAR=$(ls target/*.jar 2>/dev/null | grep -v "\.original" | head -n1 || true)
if [ -z "$JAR" ]; then
  echo "No jar found in target/"
  exit 1
fi
echo "Starting $JAR..."
java -jar "$JAR"