#!/bin/bash

git pull origin main

# Build image
docker build -t rsis-app .

# Hapus container lama jika ada
docker rm -f rsis-server

# Jalankan container baru dengan .env file
docker run -d \
    -p 8080:8080 \
    --name rsis-server \
    --restart always \
    --env-file .env \
    rsis-app

echo "Deploy selesai!"
