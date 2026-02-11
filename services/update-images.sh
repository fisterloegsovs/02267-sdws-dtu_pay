#!/bin/bash

for n in $*
do
  pushd $n
  docker compose build
  popd
done
