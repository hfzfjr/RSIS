#!/usr/bin/env bash
# Deploy RSIS: build image dulu, baru tukar container. Ada health check + rollback otomatis.
set -Eeuo pipefail
trap 'echo "[X] Deploy gagal di baris $LINENO" >&2' ERR

APP_NAME="rsis-server"
IMAGE="rsis-app"
HOST_PORT="8080"
HEALTH_TIMEOUT=180   # detik, menunggu container sehat (Dockerfile: start-period 60s)

log() { echo "[$(date +%H:%M:%S)] $*"; }

# ---------- 1. Load .env (aman untuk karakter & dan spasi) ----------
if [ ! -f .env ]; then
    echo "Error: file .env tidak ditemukan" >&2
    exit 1
fi

while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    if [[ -z "${line//[[:space:]]/}" || "$line" =~ ^[[:space:]]*# || "$line" != *=* ]]; then
        continue
    fi
    key="${line%%=*}"
    value="${line#*=}"
    key="${key#export }"
    key="${key//[[:space:]]/}"
    if [[ "$value" =~ ^\"(.*)\"$ ]] || [[ "$value" =~ ^\'(.*)\'$ ]]; then
        value="${BASH_REMATCH[1]}"
    fi
    export "$key=$value"
done < .env

for v in DB_URL DB_USERNAME DB_PASSWORD; do
    if [ -z "${!v:-}" ]; then
        echo "Error: variabel $v kosong di .env" >&2
        exit 1
    fi
done

# ---------- 2. Ambil kode terbaru ----------
log "Menarik kode terbaru..."
git pull origin main

# ---------- 3. Build image DULU (container lama tetap jalan) ----------
log "Build image baru..."
docker build -t "$IMAGE:new" .

# Simpan image lama sebagai cadangan rollback
if docker image inspect "$IMAGE:latest" >/dev/null 2>&1; then
    docker tag "$IMAGE:latest" "$IMAGE:previous"
fi
docker tag "$IMAGE:new" "$IMAGE:latest"

# ---------- Fungsi bantu ----------
remove_container() {
    if docker container inspect "$APP_NAME" >/dev/null 2>&1; then
        log "Menghentikan & menghapus container lama..."
        docker stop -t 30 "$APP_NAME" >/dev/null 2>&1 || true
        docker rm -f "$APP_NAME" >/dev/null 2>&1 || true
    fi
    # Tunggu sampai benar-benar hilang (menghindari "removal already in progress")
    for _ in $(seq 1 30); do
        if ! docker container inspect "$APP_NAME" >/dev/null 2>&1; then
            return 0
        fi
        sleep 1
    done
    echo "Error: container $APP_NAME tidak kunjung terhapus" >&2
    return 1
}

start_container() {
    docker run -d \
        -p "${HOST_PORT}:8080" \
        --name "$APP_NAME" \
        --restart always \
        -e DB_URL \
        -e DB_USERNAME \
        -e DB_PASSWORD \
        "$1" >/dev/null
}

wait_healthy() {
    local end=$((SECONDS + HEALTH_TIMEOUT)) status
    while [ "$SECONDS" -lt "$end" ]; do
        status=$(docker inspect -f '{{if .State.Running}}{{if .State.Health}}{{.State.Health.Status}}{{else}}running{{end}}{{else}}exited{{end}}' "$APP_NAME" 2>/dev/null || echo missing)
        case "$status" in
            healthy|running) return 0 ;;
            unhealthy|exited|missing) return 1 ;;
        esac
        sleep 3
    done
    return 1
}

# ---------- 4. Tukar container ----------
remove_container
log "Menjalankan container baru..."
start_container "$IMAGE:latest"

log "Menunggu aplikasi sehat (maks ${HEALTH_TIMEOUT}s)..."
if wait_healthy; then
    docker image prune -f >/dev/null 2>&1 || true
    log "Deploy selesai! Aplikasi berjalan di port ${HOST_PORT}."
    exit 0
fi

# ---------- 5. Gagal -> rollback ----------
echo "[X] Container baru tidak sehat. Log terakhir:" >&2
docker logs --tail 40 "$APP_NAME" >&2 || true

if docker image inspect "$IMAGE:previous" >/dev/null 2>&1; then
    log "Rollback ke image sebelumnya..."
    remove_container
    docker tag "$IMAGE:previous" "$IMAGE:latest"
    start_container "$IMAGE:latest"
    if wait_healthy; then
        log "Rollback berhasil. Aplikasi kembali ke versi sebelumnya."
    else
        echo "[X] Rollback juga gagal, cek: docker logs $APP_NAME" >&2
    fi
else
    echo "Tidak ada image sebelumnya untuk rollback." >&2
fi
exit 1