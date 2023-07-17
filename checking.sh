#!/usr/bin/env bash

if [ -n "$(curl -s http://localhost:8080/actuator/health | grep 'UP')" ];then
  exit 0
else
  exit 1
fi