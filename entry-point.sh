#!/usr/bin/env bash

BASE_PACKAGE=edu.nwpu.machunyan.theoreticalEvaluation.application.

if [ $# -eq 0 ]; then
    echo "specify a class name. eg:"
    echo "sh run-image.sh RunTotInfo"
    echo "sh run-image.sh temporary.DiffMultipleFormulaSf"
    exit 1
fi

mvn exec:java -Dexec.mainClass="${BASE_PACKAGE}$1"
