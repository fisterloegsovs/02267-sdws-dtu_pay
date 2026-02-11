#!/bin/bash
set -e

cd end-to-end-tests
docker compose up -d
cd ..