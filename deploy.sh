#!/bin/bash

# Load variabel dari .env atau .env.local
if [ -f .env ]; then
    set -a
    source .env
    set +a
elif [ -f .env.local ]; then
    set -a
    source .env.local
    set +a
fi

git pull origin main

# Build image
docker build -t rsis-app .

# Hapus container lama jika ada
docker rm -f rsis-server

# Jalankan container baru dengan environment variables
docker run -d \
    -p 8080:8080 \
    --name rsis-server \
    --restart always \
    -e DB_URL="${DB_URL}" \
    -e DB_USERNAME="${DB_USERNAME}" \
    -e DB_PASSWORD="${DB_PASSWORD}" \
    rsis-app

echo "Deploy selesai!"
