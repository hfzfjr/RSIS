#!/bin/bash

# Load environment variables from .env
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "Error: .env file not found"
    exit 1
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
