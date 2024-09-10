#!/bin/bash

if [ -n "$(git status --porcelain)" ]; then
    echo "There were generated resources. This is likely caused by not running ./gradlew generateResources before commiting"
    sleep 1
    echo "Please run ./gradlew generateResources, then re-commit. (If there are changes)"
    sleep 1
    printf "\n"
    echo "Changes":
    exec git status --porcelain
    exit 1
else
  echo "There were not generated resources. Check passed!"
  exit 0
fi
