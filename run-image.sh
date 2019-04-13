#!/usr/bin/env bash
# 运行镜像，将使用当前目录作为结果目录

IMAGE=liu233w/private-project:theoretical-evaluation

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker pull ${IMAGE}
docker run -m 600MB -it --rm -v ${DIR}:/app/target/outputs ${IMAGE} $1
