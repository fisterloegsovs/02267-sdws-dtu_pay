#!/bin/bash
set -e

# The bank-client needs to be installed locally as
# it is used also by the end-to-end tests.

cd services
for n in dtu-pay account-manager payment-service token-manager report-service
do
  cd $n
  docker compose build
  cd ..
done
cd ..

docker image prune -f
docker system prune -f
