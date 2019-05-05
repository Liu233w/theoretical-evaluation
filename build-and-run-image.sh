#!/usr/bin/env bash
# 构建并运行镜像，将使用当前目录作为结果目录
set -e

export DOCKER_BUILDKIT=0

IMAGE=theoretical-evaluation
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker build -t ${IMAGE} ${DIR}

DOCKER_ENV_PARAM=""
# un-comment the line below to disable parallel
#DOCKER_ENV_PARAM="${DOCKER_ENV_PARAM} -e DISABLE_PARALLEL=1"

docker run -m 5.5GB -it --rm -v ${DIR}:/app/target/outputs -v /var/run/docker.sock:/var/run/docker.sock ${DOCKER_ENV_PARAM} ${IMAGE} $1
