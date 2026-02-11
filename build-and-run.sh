#!/bin/bash
set -e

echo "Running compile.sh..."
if ./compile.sh; then
    echo "compile.sh executed successfully."
else
    echo "compile.sh failed."
    echo "Stopping execution."
    exit 1
fi

echo "Running build-images.sh..."
if ./build-images.sh; then
    echo "build-images.sh executed successfully."
else
    echo "build-images.sh failed."
    echo "Stopping execution."
    exit 1
fi

echo "Running deploy.sh..."
if ./deploy.sh; then
    echo "deploy.sh executed successfully."
else
    echo "deploy.sh.sh failed."
    echo "Stopping execution."
    exit 1
fi

./run-tests.sh

echo "All scripts executed successfully."



#./run-tests.sh
