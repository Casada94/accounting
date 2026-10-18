#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="${HOME}/accounting"
IMAGES_DIR="${DEPLOY_DIR}/images"
IMAGE_NAME="${ACCOUNTING_IMAGE:-accounting:latest}"
IMAGE_ARCHIVE="${IMAGES_DIR}/accounting.tar"

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is required to export ${IMAGE_NAME}." >&2
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Unable to connect to the Docker daemon." >&2
    exit 1
fi

if ! docker image inspect "${IMAGE_NAME}" >/dev/null 2>&1; then
    echo "Docker image ${IMAGE_NAME} was not found. Build it before deploying." >&2
    exit 1
fi

mkdir -p -- "${IMAGES_DIR}"

temporary_archive="${IMAGES_DIR}/.accounting.tar.$$"
trap 'rm -f -- "${temporary_archive}"' EXIT

echo "Exporting ${IMAGE_NAME} to ${IMAGE_ARCHIVE}..."
docker image save --output "${temporary_archive}" "${IMAGE_NAME}"

if [[ -f "${IMAGE_ARCHIVE}" ]]; then
    timestamp="$(date +%Y%m%d-%H%M%S)"
    backup_archive="${IMAGES_DIR}/accounting-${timestamp}.tar"

    if [[ -e "${backup_archive}" ]]; then
        backup_archive="${IMAGES_DIR}/accounting-${timestamp}-$$.tar"
    fi

    mv -- "${IMAGE_ARCHIVE}" "${backup_archive}"
    echo "Backed up the previous image to ${backup_archive}."
fi

mv -- "${temporary_archive}" "${IMAGE_ARCHIVE}"
cp -- "${PROJECT_DIR}/compose.yaml" "${DEPLOY_DIR}/compose.yaml"

echo "Deployed ${IMAGE_ARCHIVE} and ${DEPLOY_DIR}/compose.yaml."
