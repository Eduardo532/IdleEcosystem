#!/bin/bash
# Uso: dl.sh id=uuid [id=uuid ...]  — descarga las imágenes generadas en Figma a ai/<id>.png
cd "$(dirname "$0")"; mkdir -p ai
for pair in "$@"; do id=${pair%%=*}; u=${pair#*=}
  echo "$id https://www.figma.com/api/mcp/asset/$u.png" >> ai/urls.txt
  curl -sf -L -o "ai/$id.png" "https://www.figma.com/api/mcp/asset/$u.png" && echo "ok $id" || echo "FAIL $id"
done
