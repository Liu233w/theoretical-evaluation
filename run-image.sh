#!/usr/bin/env bash
# 运行镜像，将使用当前目录作为结果目录

IMAGE=liu233w/private-project:theoretical-evaluation

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

DOCKER_ENV_PARAM=""
# un-comment the line below to disable parallel
#DOCKER_ENV_PARAM="${DOCKER_ENV_PARAM} -e DISABLE_PARALLEL=1"

docker pull ${IMAGE}
docker run -m 800MB -it --rm -v ${DIR}:/app/target/outputs ${DOCKER_ENV_PARAM} ${IMAGE} $1
