#!/bin/bash

# Load variabel dari .env.local jika ada
if [ -f .env.local ]; then
    set -a
    source .env.local
    set +a
fi

git pull origin main

# Build image
docker build -t rsis-app .

# Hapus container lama jika ada
docker rm -f rsis-server

# Jalankan container baru
docker run -d -p 8080:8080 --name rsis-server --restart always rsis-app

echo "Deploy selesai!"
