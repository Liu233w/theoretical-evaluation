#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass="edu.nwpu.machunyan.theoreticalEvaluation.application.Main" \
    -Dexec.args="$1"
