#!/usr/bin/env bash
docker build -f Dockerfile -t health-checking .
docker build -f Dockerfile-without-health-checking -t no-health-checking .